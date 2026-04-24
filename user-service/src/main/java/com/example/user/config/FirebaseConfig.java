package com.example.user.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Configuration class to initialize Firebase Admin SDK using the service account JSON file.
 * This ensures Firebase is initialized only once when the application starts.
 */
@Component
public class FirebaseConfig {

    /**
     * Initializes the FirebaseApp with credentials from the service account JSON file.
     * This method is executed after the bean is constructed due to the {@code @PostConstruct} annotation.
     *
     * @throws IOException if the service account file is not found or cannot be read
     */
    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }
}