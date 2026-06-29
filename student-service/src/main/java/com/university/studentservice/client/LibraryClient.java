package com.university.studentservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class LibraryClient {

    private final RestTemplate restTemplate;
    private final String libraryServiceUrl;

    public LibraryClient(RestTemplate restTemplate,
                         @Value("${library.service.url}") String libraryServiceUrl) {
        this.restTemplate = restTemplate;
        this.libraryServiceUrl = libraryServiceUrl;
    }

    public void createAccount(String studentId, String pin) {
        try {
            restTemplate.postForObject(
                libraryServiceUrl + "/api/accounts",
                Map.of("studentId", studentId, "pin", pin),
                Map.class
            );
        } catch (RestClientException e) {
            // Library service down — log and continue
        }
    }

    public boolean hasActiveLoans(String studentId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(
                libraryServiceUrl + "/api/accounts/" + studentId + "/loans/active-count",
                Map.class
            );
            if (response != null && response.containsKey("activeLoans")) {
                Object val = response.get("activeLoans");
                if (val instanceof Number n) {
                    return n.intValue() > 0;
                }
            }
        } catch (RestClientException e) {
            // Library service down — assume no active loans (fail open)
        }
        return false;
    }
}
