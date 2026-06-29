package com.university.libraryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplateConfig — provides a RestTemplate @Bean for dependency injection.
 *
 * Declared as a @Bean so it can be @MockBean-ed in integration tests,
 * enabling Finance service calls to be stubbed without a real HTTP connection.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate used over WebClient — synchronous pattern suits the
     * blocking request/response flow used when posting library fines.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
