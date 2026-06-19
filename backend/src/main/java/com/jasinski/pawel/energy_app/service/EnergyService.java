package com.jasinski.pawel.energy_app.service;

import com.jasinski.pawel.energy_app.client.CarbonApiClient;
import com.jasinski.pawel.energy_app.dto.DailyEnergyMix;
import com.jasinski.pawel.energy_app.dto.GenerationMix;
import com.jasinski.pawel.energy_app.dto.IntervalData;
import com.jasinski.pawel.energy_app.exception.EnergyDataException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EnergyService {

    private final CarbonApiClient carbonApiClient;
    private static final Set<String> CLEAN_ENERGY_SOURCES = Set.of(
            "biomass", "nuclear", "hydro", "wind", "solar"
    );
    private static final ZoneId UK_ZONE = ZoneId.of("Europe/London");

    public EnergyService(CarbonApiClient carbonApiClient) {
        this.carbonApiClient = carbonApiClient;
    }

    public List<DailyEnergyMix> getEnergyMixForThreeDays() {
        ZonedDateTime nowInUk = ZonedDateTime.now(UK_ZONE);
        ZonedDateTime start = nowInUk.toLocalDate().atStartOfDay(UK_ZONE);
        ZonedDateTime end = start.plusDays(3);

        List<IntervalData> rawData = carbonApiClient.fetchRawData(start, end);
        Map<LocalDate, List<IntervalData>> groupedByDate = groupByDay(rawData);

        LocalDate today = nowInUk.toLocalDate();

        List<DailyEnergyMix> result = groupedByDate.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(today) && entry.getKey().isBefore(today.plusDays(3)))
                .map(this::createDailyMix)
                .sorted(Comparator.comparing(DailyEnergyMix::date))
                .toList();

        if (result.size() < 3) {
            throw new EnergyDataException("Niekompletne dane. Oczekiwano danych na 3 dni, otrzymano : " + result.size());
        }

        return result;
    }

    private Map<LocalDate, List<IntervalData>> groupByDay(List<IntervalData> rawData) {
        return rawData.stream()
                .collect(Collectors.groupingBy(interval ->
                        ZonedDateTime.parse(interval.from()).withZoneSameInstant(UK_ZONE).toLocalDate()
                ));
    }

    private DailyEnergyMix createDailyMix(Map.Entry<LocalDate, List<IntervalData>> entry) {
        LocalDate date = entry.getKey();

        Map<String, Double> averages = calculateDailyAverages(entry.getValue());

        double cleanEnergyPercentage = averages.entrySet().stream()
                .filter(e -> CLEAN_ENERGY_SOURCES.contains(e.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();
        double roundedCleanEnergyPercentage = round(cleanEnergyPercentage);

        Map<String, Double> roundedAverages = averages.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> round(e.getValue())
                ));

        return new DailyEnergyMix(date, roundedCleanEnergyPercentage, roundedAverages);
    }

    private Map<String, Double> calculateDailyAverages(List<IntervalData> intervals) {
        return intervals.stream()
                .flatMap(interval -> interval.generationmix().stream())
                .collect(Collectors.groupingBy(
                        GenerationMix::fuel,
                        Collectors.averagingDouble(GenerationMix::perc)
                ));
    }


    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}