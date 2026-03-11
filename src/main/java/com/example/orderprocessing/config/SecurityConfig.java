package com.example.orderprocessing.config;

import com.example.orderprocessing.model.Role;
import com.example.orderprocessing.model.User;
import com.example.orderprocessing.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                            "/",
                            "/index.html",
                            "/signup.html",
                            "/static/**",
                            "/styles.css",
                            "/app.js",
                            "/favicon.ico",
                            "/api/auth/signup",
                            "/actuator/**",
                            // OpenAPI / Swagger UI
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/swagger-resources/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
                }));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public CommandLineRunner initializeUsers(PasswordEncoder passwordEncoder) {
        return args -> {
            // Log current users for debugging
            logger.info("=== Existing users in database ===");
            userRepository.findAll().forEach(u ->
                logger.info("user={}, role={}, passHash={}", u.getUsername(), u.getRole(), u.getPassword())
            );

            // Verify users exist (should be created by OrderProcessingApplication first)
            boolean adminExists = userRepository.findByUsername("admin").isPresent();
            boolean customerExists = userRepository.findByUsername("customer").isPresent();
            
            if (adminExists) {
                // reset admin password and email in case it was changed
                User admin = userRepository.findByUsername("admin").get();
                admin.setPassword(passwordEncoder.encode("password"));
                admin.setEmail("admin@example.com");
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
                logger.info("✓ Admin user exists; password reset to 'password'");
            } else {
                User admin = new User("admin", passwordEncoder.encode("password"), 
                        "admin@example.com", "Administrator", Role.ADMIN);
                userRepository.save(admin);
                logger.info("✓ Admin user created: username=admin, password=password");
            }
            if (customerExists) {
                User customer = userRepository.findByUsername("customer").get();
                customer.setPassword(passwordEncoder.encode("password"));
                customer.setEmail("customer@example.com");
                customer.setRole(Role.CUSTOMER);
                userRepository.save(customer);
                logger.info("✓ Customer user exists; password reset to 'password'");
            } else {
                User customer = new User("customer", passwordEncoder.encode("password"), 
                        "customer@example.com", "Test Customer", Role.CUSTOMER);
                userRepository.save(customer);
                logger.info("✓ Customer user created: username=customer, password=password");
            }
        };
    }
}

