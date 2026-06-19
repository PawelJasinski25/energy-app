package com.jasinski.pawel.energy_app.service;

import com.jasinski.pawel.energy_app.client.CarbonApiClient;
import com.jasinski.pawel.energy_app.dto.ChargingWindow;
import com.jasinski.pawel.energy_app.dto.DailyEnergyMix;
import com.jasinski.pawel.energy_app.dto.GenerationMix;
import com.jasinski.pawel.energy_app.dto.IntervalData;
import com.jasinski.pawel.energy_app.exception.EnergyDataException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
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
        if (rawData == null) {
            throw new EnergyDataException("API zwróciło pustą odpowiedź.");
        }

        Map<LocalDate, List<IntervalData>> groupedByDate = groupByDay(rawData);

        LocalDate today = nowInUk.toLocalDate();

        List<DailyEnergyMix> result = groupedByDate.entrySet().stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isBefore(today) && entry.getKey().isBefore(today.plusDays(3)))
                .map(this::createDailyMix)
                .sorted(Comparator.comparing(DailyEnergyMix::date))
                .toList();

        if (result.size() < 3) {
            throw new EnergyDataException("Niekompletne dane. Oczekiwano danych na 3 dni, otrzymano : " + result.size());
        }

        return result;
    }

    public ChargingWindow getOptimalChargingWindow(int hours) {
        if (hours < 1 || hours > 6) {
            throw new IllegalArgumentException("Długość okna musi wynosić od 1 do 6 godzin.");
        }

        int requiredIntervals = hours * 2;

        ZonedDateTime nowInUk = ZonedDateTime.now(UK_ZONE);
        ZonedDateTime start = nowInUk.toLocalDate().plusDays(1).atStartOfDay(UK_ZONE);
        ZonedDateTime end = start.plusDays(2);

        List<IntervalData> rawData = carbonApiClient.fetchRawData(start, end);

        if (rawData == null || rawData.size() < requiredIntervals) {
            throw new EnergyDataException("Brak wystarczającej ilości danych prognozowanych z API.");
        }

        List<IntervalData> sortedData = rawData.stream()
                .filter(interval -> interval != null && interval.from() != null)
                .sorted(Comparator.comparing(this::parseDateTime))
                .toList();

        double maxCleanAvg = -1.0;
        int bestStartIndex = -1;

        for (int i = 0; i <= sortedData.size() - requiredIntervals; i++) {
            List<IntervalData> window = sortedData.subList(i, i + requiredIntervals);

            double currentWindowCleanSum = 0;
            boolean isWindowValid = true;

            for (IntervalData interval : window) {
                if (interval.generationmix() == null || interval.generationmix().isEmpty()) {
                    isWindowValid = false;
                    break;
                }

                double intervalCleanPercent = interval.generationmix().stream()
                        .filter(mix -> mix != null && mix.fuel() != null && CLEAN_ENERGY_SOURCES.contains(mix.fuel()))
                        .mapToDouble(GenerationMix::perc)
                        .sum();
                currentWindowCleanSum += intervalCleanPercent;
            }

            if (!isWindowValid) {
                continue;
            }

            double currentWindowCleanAvg = currentWindowCleanSum / requiredIntervals;

            if (currentWindowCleanAvg > maxCleanAvg) {
                maxCleanAvg = currentWindowCleanAvg;
                bestStartIndex = i;
            }
        }

        if (bestStartIndex == -1) {
            throw new EnergyDataException("Nie udało się znaleźć ani jednego pełnego okna ładowania z poprawnymi danymi.");
        }

        IntervalData startInterval = sortedData.get(bestStartIndex);
        IntervalData endInterval = sortedData.get(bestStartIndex + requiredIntervals - 1);

        return new ChargingWindow(
                startInterval.from(),
                endInterval.to(),
                round(maxCleanAvg)
        );
    }

    private Map<LocalDate, List<IntervalData>> groupByDay(List<IntervalData> rawData) {
        return rawData.stream()
                .filter(interval -> interval != null && interval.from() != null)
                .collect(Collectors.groupingBy(interval -> parseDateTime(interval).withZoneSameInstant(UK_ZONE).toLocalDate()));
    }

    private DailyEnergyMix createDailyMix(Map.Entry<LocalDate, List<IntervalData>> entry) {
        LocalDate date = entry.getKey();

        Map<String, Double> averages = calculateDailyAverages(entry.getValue());

        double cleanEnergyPercentage = averages.entrySet().stream()
                .filter(e -> e.getKey() != null && CLEAN_ENERGY_SOURCES.contains(e.getKey()))
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
                .filter(interval -> interval != null && interval.generationmix() != null)
                .flatMap(interval -> interval.generationmix().stream())
                .filter(mix -> mix != null && mix.fuel() != null)
                .collect(Collectors.groupingBy(
                        GenerationMix::fuel,
                        Collectors.averagingDouble(GenerationMix::perc)
                ));
    }

    private ZonedDateTime parseDateTime(IntervalData interval) {
        try {
            return ZonedDateTime.parse(interval.from());
        } catch (DateTimeParseException e) {
            throw new EnergyDataException("Błąd parsowania daty z zewnętrznego API: " + interval.from(), e);
        }
    }


    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}