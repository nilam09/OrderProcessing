package com.example.orderprocessing;

import com.example.orderprocessing.model.Product;
import com.example.orderprocessing.model.Role;
import com.example.orderprocessing.model.User;
import com.example.orderprocessing.repository.ProductRepository;
import com.example.orderprocessing.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@SpringBootApplication
@EnableScheduling
public class OrderProcessingApplication {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(OrderProcessingApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, Environment environment) {
        return args -> {
            if(productRepository.count() == 0) {
                productRepository.save(new Product("Laptop", "High-performance laptop", BigDecimal.valueOf(999.99), "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=300&fit=crop"));
                productRepository.save(new Product("Mouse", "Wireless mouse", BigDecimal.valueOf(29.99), "https://images.unsplash.com/photo-1527814050087-3793815479db?w=400&h=300&fit=crop"));
                productRepository.save(new Product("Keyboard", "Mechanical keyboard", BigDecimal.valueOf(79.99), "https://images.unsplash.com/photo-1587829191301-996ec6d2e999?w=400&h=300&fit=crop"));
                productRepository.save(new Product("Monitor", "4K monitor", BigDecimal.valueOf(399.99), "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400&h=300&fit=crop"));
                productRepository.save(new Product("Headphones", "Noise-cancelling headphones", BigDecimal.valueOf(149.99), "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=300&fit=crop"));
            }

            if (userRepository.findByUsername("admin").isEmpty()) {
                userRepository.save(new User("admin", passwordEncoder.encode("password"), "admin@example.com", "Administrator", Role.ADMIN));
                logger.info("Admin user created: username=admin, password=password");
            }

            // Always ensure customer user exists
            if (userRepository.findByUsername("customer").isEmpty()) {
                userRepository.save(new User("customer", passwordEncoder.encode("password"), "customer@example.com", "Test Customer", Role.CUSTOMER));
                logger.info("Customer user created: username=customer, password=password");
            }
        };
    }
}

