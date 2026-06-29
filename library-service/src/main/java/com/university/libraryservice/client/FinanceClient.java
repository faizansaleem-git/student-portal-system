package com.university.libraryservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * FinanceClient — HTTP client for calling the Finance microservice.
 *
 * Used when a student returns a book late: a fine invoice is posted to
 * Finance so the student can pay via the Finance Portal.
 *
 * RestTemplate used over WebClient — synchronous pattern suits this
 * blocking request/response use case and keeps the service simple.
 *
 * Constructor injection: makes the dependency explicit and allows tests
 * to supply a mock without Spring context.
 */
@Component
@Slf4j
public class FinanceClient {

    private final RestTemplate restTemplate;
    private final String financeServiceUrl;

    /**
     * Constructor injection — RestTemplate is a @Bean defined in RestTemplateConfig.
     * financeServiceUrl is read from application.properties / environment variable.
     */
    public FinanceClient(RestTemplate restTemplate,
                         @Value("${finance.service.url}") String financeServiceUrl) {
        this.restTemplate = restTemplate;
        this.financeServiceUrl = financeServiceUrl;
    }

    /**
     * Post a fine invoice to the Finance service.
     * Returns the invoiceReference string, or null if the Finance service is unavailable.
     * Caller always checks for null and shows a graceful error.
     *
     * @param studentId   e.g. "STU-00001"
     * @param amount      fine in GBP
     * @param description e.g. "Library fine - Clean Code"
     * @return invoiceReference UUID string, or null on failure
     */
    public String postFineInvoice(String studentId, BigDecimal amount, String description) {
        String url = financeServiceUrl + "/api/invoices";
        Map<String, Object> body = Map.of(
            "studentId", studentId,
            "amount", amount,
            "description", description
        );
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object ref = response.getBody().get("referenceNumber");
                return ref != null ? ref.toString() : null;
            }
        } catch (RestClientException ex) {
            log.error("[FinanceClient] Failed to post fine invoice to {}: {}", url, ex.getMessage());
        }
        return null;
    }
}
