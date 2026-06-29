package com.university.studentservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class FinanceClient {

    private final RestTemplate restTemplate;
    private final String financeServiceUrl;

    public FinanceClient(RestTemplate restTemplate,
                         @Value("${finance.service.url}") String financeServiceUrl) {
        this.restTemplate = restTemplate;
        this.financeServiceUrl = financeServiceUrl;
    }

    public void createAccount(String studentId) {
        try {
            restTemplate.postForObject(
                financeServiceUrl + "/api/accounts",
                Map.of("studentId", studentId),
                Map.class
            );
        } catch (RestClientException e) {
            // Finance service down — log and continue; account creation is best-effort
        }
    }

    public String createInvoice(String studentId, BigDecimal amount, String description) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                financeServiceUrl + "/api/invoices",
                Map.of(
                    "studentId", studentId,
                    "amount", amount,
                    "description", description
                ),
                Map.class
            );
            if (response != null && response.containsKey("referenceNumber")) {
                return (String) response.get("referenceNumber");
            }
        } catch (RestClientException e) {
            // Finance service down — enrolment still proceeds without invoice reference
        }
        return null;
    }

    public boolean hasOutstandingBalance(String studentId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(
                financeServiceUrl + "/api/accounts/" + studentId + "/balance",
                Map.class
            );
            if (response != null && response.containsKey("hasOutstanding")) {
                return Boolean.TRUE.equals(response.get("hasOutstanding"));
            }
        } catch (RestClientException e) {
            // Finance service down — assume no outstanding (fail open)
        }
        return false;
    }
}
