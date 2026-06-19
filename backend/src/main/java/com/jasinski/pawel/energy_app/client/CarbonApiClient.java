package com.jasinski.pawel.energy_app.client;


import com.jasinski.pawel.energy_app.dto.CarbonApiResponse;
import com.jasinski.pawel.energy_app.dto.IntervalData;
import com.jasinski.pawel.energy_app.exception.EnergyDataException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class CarbonApiClient {

    private final RestTemplate restTemplate;
    private static final String API_URL = "https://api.carbonintensity.org.uk/generation/{from}/{to}";

    public CarbonApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<IntervalData> fetchRawData(ZonedDateTime from, ZonedDateTime to) {
        String fromStr = from.toInstant().toString();
        String toStr = to.toInstant().toString();

        try {
            CarbonApiResponse response = restTemplate.getForObject(API_URL, CarbonApiResponse.class, fromStr, toStr);

            if (response == null || response.data() == null || response.data().isEmpty()) {
                throw new EnergyDataException("Zewnętrzne API nie zwróciło żadnych danych dla podanego okresu.");
            }

            return response.data();

        } catch (RestClientException e) {
            throw new EnergyDataException("Błąd podczas komunikacji z API Carbon Intensity.", e);
        }
    }
}
