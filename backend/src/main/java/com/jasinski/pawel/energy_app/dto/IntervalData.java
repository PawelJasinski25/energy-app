package com.jasinski.pawel.energy_app.dto;

import java.util.List;

public record IntervalData(
        String from,
        String to,
        List<GenerationMix> generationmix
) {}
