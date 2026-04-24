/**
 * Main application class for the User service.
 * This class serves as the entry point for the Spring Boot application.
 */
package com.example.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Configures and starts the Spring Boot application.
 * The @SpringBootApplication annotation enables auto-configuration, component scanning,
 * and configuration for the application.
 */
@SpringBootApplication
public class Application {
    /**
     * Main method to launch the Spring Boot application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}