package com.university.libraryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoderConfig — provides the PasswordEncoder @Bean in its own class.
 *
 * Extracted from SecurityConfig to break the circular dependency:
 *   SecurityConfig → LibraryAccountService → PasswordEncoder → SecurityConfig.
 * By isolating the bean here, both SecurityConfig and LibraryAccountService
 * can inject PasswordEncoder without a cycle.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
