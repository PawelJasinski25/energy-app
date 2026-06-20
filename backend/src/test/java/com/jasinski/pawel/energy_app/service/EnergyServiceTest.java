package com.jasinski.pawel.energy_app.service;

import com.jasinski.pawel.energy_app.client.CarbonApiClient;
import com.jasinski.pawel.energy_app.dto.ChargingWindow;
import com.jasinski.pawel.energy_app.dto.DailyEnergyMix;
import com.jasinski.pawel.energy_app.dto.GenerationMix;
import com.jasinski.pawel.energy_app.dto.IntervalData;
import com.jasinski.pawel.energy_app.exception.EnergyDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyServiceTest {

    @Mock
    private CarbonApiClient carbonApiClient;

    @InjectMocks
    private EnergyService energyService;

    private static final ZoneId UK_ZONE = ZoneId.of("Europe/London");

    private List<IntervalData> mockRawData;

    @BeforeEach
    void setUp() {
        mockRawData = new ArrayList<>();
    }


    @Test
    void getOptimalChargingWindow_shouldReturnBestWindow() {
        ZonedDateTime now = ZonedDateTime.now(UK_ZONE);
        ZonedDateTime start = now.toLocalDate().plusDays(1).atStartOfDay(UK_ZONE);

        // 0% czystej energii
        mockRawData.add(createInterval(start, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        mockRawData.add(createInterval(start.plusMinutes(30), 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        // 45.5% czystej energii
        mockRawData.add(createInterval(start.plusMinutes(60), 54.5, 0.0, 10.0, 10.0, 5.5, 0.0, 0.0, 10.0, 10.0));
        //  80.5% czystej energii
        mockRawData.add(createInterval(start.plusMinutes(90), 19.5, 0.0, 20.0, 20.0, 20.0, 0.0, 0.0, 10.0, 10.5));

        // 0% czystej energii
        mockRawData.add(createInterval(start.plusMinutes(120), 80.0, 10.0, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0));

        when(carbonApiClient.fetchRawData(any(), any())).thenReturn(mockRawData);

        ChargingWindow result = energyService.getOptimalChargingWindow(1);

        assertNotNull(result);
        assertEquals(start.plusMinutes(60).toInstant().toString(), result.start());
        assertEquals(start.plusMinutes(120).toInstant().toString(), result.end());
        assertEquals(63.0, result.cleanEnergyPercentage());

        ArgumentCaptor<ZonedDateTime> startCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        ArgumentCaptor<ZonedDateTime> endCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        verify(carbonApiClient).fetchRawData(startCaptor.capture(), endCaptor.capture());

        assertEquals(start, startCaptor.getValue());
        assertEquals(start.plusDays(2), endCaptor.getValue());
    }

    @Test
    void getOptimalChargingWindow_shouldHandleWindowCrossingMidnight() {
        ZonedDateTime now = ZonedDateTime.now(UK_ZONE);
        ZonedDateTime start = now.toLocalDate().plusDays(1).atStartOfDay(UK_ZONE);
        ZonedDateTime lateNight = start.plusHours(23); // Godzina 23:00

        mockRawData.add(createInterval(lateNight, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        mockRawData.add(createInterval(lateNight.plusMinutes(30), 0.0, 0.0, 20.0, 20.0, 20.0, 0.0, 0.0, 20.0, 20.0));
        mockRawData.add(createInterval(lateNight.plusMinutes(60), 0.0, 0.0, 20.0, 20.0, 20.0, 0.0, 0.0, 20.0, 20.0));

        when(carbonApiClient.fetchRawData(any(), any())).thenReturn(mockRawData);

        ChargingWindow result = energyService.getOptimalChargingWindow(1);

        assertNotNull(result);
        assertEquals(lateNight.plusMinutes(30).toInstant().toString(), result.start());
        assertEquals(lateNight.plusMinutes(90).toInstant().toString(), result.end());
        assertEquals(100.0, result.cleanEnergyPercentage());
    }

    @Test
    void getOptimalChargingWindow_shouldThrowExceptionWhenNoValidDataFound() {
        ZonedDateTime start = ZonedDateTime.now(UK_ZONE).plusDays(1).toLocalDate().atStartOfDay(UK_ZONE);
        mockRawData.add(new IntervalData(start.toInstant().toString(), start.plusMinutes(30).toInstant().toString(), null));
        mockRawData.add(new IntervalData(start.plusMinutes(30).toInstant().toString(), start.plusMinutes(60).toInstant().toString(), null));

        when(carbonApiClient.fetchRawData(any(), any())).thenReturn(mockRawData);

        EnergyDataException exception = assertThrows(EnergyDataException.class, () -> energyService.getOptimalChargingWindow(1));
        assertTrue(exception.getMessage().contains("Nie udało się znaleźć ani jednego pełnego okna"));
    }

    @Test
    void getOptimalChargingWindow_shouldThrowExceptionWhenHoursInvalid() {
        assertThrows(IllegalArgumentException.class, () -> energyService.getOptimalChargingWindow(0));
        assertThrows(IllegalArgumentException.class, () -> energyService.getOptimalChargingWindow(7));
    }


    @Test
    void getEnergyMixForThreeDays_shouldReturnMixForValidData() {
        ZonedDateTime today = ZonedDateTime.now(UK_ZONE).toLocalDate().atStartOfDay(UK_ZONE);

        mockRawData.add(createInterval(today.plusHours(12), 43.6, 0.7, 4.2, 17.6, 2.2, 6.5, 0.3, 6.8, 18.1));

        mockRawData.add(createInterval(today.plusDays(1).plusHours(12), 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 20.0));
        mockRawData.add(createInterval(today.plusDays(2).plusHours(12), 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        when(carbonApiClient.fetchRawData(any(), any())).thenReturn(mockRawData);

        List<DailyEnergyMix> result = energyService.getEnergyMixForThreeDays();

        assertNotNull(result);
        assertEquals(3, result.size());

        assertEquals(today.toLocalDate(), result.get(0).date());
        assertEquals(48.9, result.get(0).cleanEnergyPercentage());
        assertEquals(18.1, result.get(0).sourceAverages().get("solar"));
    }

    @Test
    void getEnergyMixForThreeDays_shouldHandleUnorderedIntervals() {
        ZonedDateTime today = ZonedDateTime.now(UK_ZONE).toLocalDate().atStartOfDay(UK_ZONE);

        mockRawData.add(createInterval(today.plusDays(2).plusHours(12), 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        mockRawData.add(createInterval(today.plusHours(12), 0.0, 0.0, 20.0, 20.0, 20.0, 0.0, 0.0, 20.0, 20.0));
        mockRawData.add(createInterval(today.plusDays(1).plusHours(12), 50.0, 0.0, 10.0, 10.0, 10.0, 0.0, 0.0, 10.0, 10.0));

        when(carbonApiClient.fetchRawData(any(), any())).thenReturn(mockRawData);

        List<DailyEnergyMix> result = energyService.getEnergyMixForThreeDays();

        assertEquals(3, result.size());

        assertEquals(today.toLocalDate(), result.get(0).date());
        assertEquals(100.0, result.get(0).cleanEnergyPercentage());

        assertEquals(today.plusDays(1).toLocalDate(), result.get(1).date());
        assertEquals(50.0, result.get(1).cleanEnergyPercentage());

        assertEquals(today.plusDays(2).toLocalDate(), result.get(2).date());
        assertEquals(0.0, result.get(2).cleanEnergyPercentage());
    }

    @Test
    void getEnergyMixForThreeDays_shouldThrowExceptionWhenNotEnoughDays() {
        ZonedDateTime today = ZonedDateTime.now(UK_ZONE).toLocalDate().atStartOfDay(UK_ZONE);

        mockRawData.add(createInterval(today.plusHours(1), 50.0, 0.0, 0.0, 0.0, 0.0, 50.0, 0.0, 0.0, 0.0));
        mockRawData.add(createInterval(today.plusDays(1).plusHours(1), 50.0, 0.0, 0.0, 0.0, 0.0, 50.0, 0.0, 0.0, 0.0));

        when(carbonApiClient.fetchRawData(any(), any())).thenReturn(mockRawData);

        EnergyDataException exception = assertThrows(EnergyDataException.class, () -> energyService.getEnergyMixForThreeDays());
        assertTrue(exception.getMessage().contains("Oczekiwano danych na 3 dni"));
    }

    @Test
    void getEnergyMixForThreeDays_shouldHandleNullsGracefully() {
        ZonedDateTime today = ZonedDateTime.now(UK_ZONE).toLocalDate().atStartOfDay(UK_ZONE);

        mockRawData.add(createInterval(today, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        mockRawData.add(createInterval(today.plusDays(1), 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));


        List<GenerationMix> brokenMix = new ArrayList<>();
        brokenMix.add(new GenerationMix(null, 50.0));
        brokenMix.add(null);
        brokenMix.add(new GenerationMix("wind", 50.0));

        mockRawData.add(new IntervalData(today.plusDays(2).toInstant().toString(), today.plusDays(2).plusMinutes(30).toInstant().toString(), brokenMix));

        when(carbonApiClient.fetchRawData(any(), any())).thenReturn(mockRawData);

        List<DailyEnergyMix> result = energyService.getEnergyMixForThreeDays();

        assertEquals(3, result.size());
        assertEquals(50.0, result.get(2).cleanEnergyPercentage());
    }


    private IntervalData createInterval(ZonedDateTime from, double gas, double coal, double biomass, double nuclear, double hydro, double imports, double other, double wind, double solar) {
        ZonedDateTime to = from.plusMinutes(30);
        List<GenerationMix> mix = List.of(
                new GenerationMix("gas", gas),
                new GenerationMix("coal", coal),
                new GenerationMix("biomass", biomass),
                new GenerationMix("nuclear", nuclear),
                new GenerationMix("hydro", hydro),
                new GenerationMix("imports", imports),
                new GenerationMix("other", other),
                new GenerationMix("wind", wind),
                new GenerationMix("solar", solar)
        );
        return new IntervalData(from.toInstant().toString(), to.toInstant().toString(), mix);
    }
}
