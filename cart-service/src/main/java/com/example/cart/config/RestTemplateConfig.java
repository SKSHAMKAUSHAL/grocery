package com.example.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class to define beans related to HTTP communication.
 * Provides a singleton {@link RestTemplate} bean for making REST API calls.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates and returns a {@link RestTemplate} bean.
     * This bean can be used throughout the application to make HTTP requests to external services.
     *
     * @return a new instance of RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
