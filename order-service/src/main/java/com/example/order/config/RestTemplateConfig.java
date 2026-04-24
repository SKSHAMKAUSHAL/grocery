package com.example.order.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 * Configuration class for RestTemplate with Firebase admin JWT authentication.
 */
@Configuration
public class RestTemplateConfig {
    private String adminJwt;

    @PostConstruct
    public void initializeFirebase() throws IOException {
        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-service-account.json");
        if (serviceAccount == null) {
            throw new IOException("Firebase service account file not found");
        }
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase"));
        credentials.refreshIfExpired();
        adminJwt = credentials.getAccessToken().getTokenValue();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            // Only add Authorization header for cart-service requests
            if (request.getURI().getHost().contains("cart-service")) {
                request.getHeaders().add("Authorization", "Bearer " + adminJwt);
            }
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}