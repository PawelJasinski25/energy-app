package com.jasinski.pawel.energy_app.dto;

import java.time.LocalDate;
import java.util.Map;

public record DailyEnergyMix(
        LocalDate date,
        double cleanEnergyPercentage,
        Map<String, Double> sourceAverages
) {}
