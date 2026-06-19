package com.jasinski.pawel.energy_app.dto;

import java.util.List;

public record CarbonApiResponse(
        List<IntervalData> data
) {}
