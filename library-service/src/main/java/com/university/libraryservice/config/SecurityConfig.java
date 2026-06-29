package com.university.libraryservice.config;

import com.university.libraryservice.service.LibraryAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * SecurityConfig — Spring Security configuration for the Library Portal.
 *
 * Authentication: studentId (username) + PIN (password), session-based.
 * The admin account (studentId="admin") has ROLE_ADMIN; students have ROLE_STUDENT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final LibraryAccountService libraryAccountService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor injection: PasswordEncoder injected from PasswordEncoderConfig
     * (separate class) to avoid the circular dependency that arises when
     * SecurityConfig defines PasswordEncoder and LibraryAccountService injects it.
     */
    public SecurityConfig(LibraryAccountService libraryAccountService,
                          PasswordEncoder passwordEncoder) {
        this.libraryAccountService = libraryAccountService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(libraryAccountService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Public: REST API endpoints called by Student service
                .requestMatchers("/api/**").permitAll()
                // Public: health check
                .requestMatchers("/health").permitAll()
                // Public: static resources
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // Public: login and error pages
                .requestMatchers("/login", "/error").permitAll()
                // Admin-only pages
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Everything else requires login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            // Disable CSRF for REST API endpoints; keep it for web form submissions
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            // Redirect to login with a clear message on 403 (e.g. stale CSRF token after restart)
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler())
            );

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            -> response.sendRedirect(request.getContextPath() + "/login?error=session");
    }
}
