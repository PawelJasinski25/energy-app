package com.jasinski.pawel.energy_app.dto;

public record ChargingWindow(
        String start,
        String end,
        double cleanEnergyPercentage
) {}
