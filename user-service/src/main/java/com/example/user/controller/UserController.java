package com.example.user.controller;

import com.example.user.entity.User;
import com.example.user.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing user authentication and profile retrieval.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${firebase.api-key}")
    private String firebaseApiKey;

    private final WebClient webClient = WebClient.create();

    private static final List<String> ALLOWED_ROLES = Arrays.asList("CONSUMER", "ADMIN");

    /**
     * Signs up a new user.
     */
    @Operation(summary = "Sign up a new user", description = "Creates a user in Firebase and database")
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String role = request.getOrDefault("role", "CONSUMER");

        // Input validation
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email format"));
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
        }
        if (!ALLOWED_ROLES.contains(role)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role. Allowed: " + String.join(", ", ALLOWED_ROLES)));
        }

        try {
            // Create user in Firebase
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password);
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);

            // Set custom claim for role
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role);
            FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), claims);

            // Save user to database
            User user = new User();
            user.setId(userRecord.getUid());
            user.setEmail(email);
            user.setRole(role);
            userService.saveUser(user);

            // Response
            Map<String, Object> response = new HashMap<>();
            response.put("uid", userRecord.getUid());
            response.put("email", email);
            response.put("role", role);
            return ResponseEntity.ok(response);
        } catch (FirebaseAuthException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("EMAIL_EXISTS")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            } else if (errorMessage.contains("INVALID_EMAIL")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid email format"));
            } else if (errorMessage.contains("WEAK_PASSWORD")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is too weak"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Firebase error: " + e.getMessage()));
            }
        }
    }

    /**
     * Signs in an existing user.
     */
    @Operation(summary = "Sign in a user", description = "Authenticates user and returns UID and JWT token")
    @PostMapping("/signin")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> signIn(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        // Input validation
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email format"));
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
        }

        try {
            // Call Firebase REST API to verify credentials
            Map<String, String> body = Map.of(
                "email", email,
                "password", password,
                "returnSecureToken", "true"
            );
            Map<String, Object> response = webClient.post()
                .uri("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseApiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null && response.containsKey("localId") && response.containsKey("idToken")) {
                Map<String, Object> result = new HashMap<>();
                result.put("uid", response.get("localId"));
                result.put("jwtToken", response.get("idToken"));
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Authentication failed"));
            }
        } catch (WebClientResponseException e) {
            String errorMessage = "Authentication failed";
            try {
                Map<String, Object> errorResponse = e.getResponseBodyAs(Map.class);
                if (errorResponse != null && errorResponse.containsKey("error")) {
                    Map<String, Object> errorDetails = (Map<String, Object>) errorResponse.get("error");
                    String firebaseError = (String) errorDetails.get("message");
                    errorMessage = switch (firebaseError) {
                        case "INVALID_PASSWORD" -> "Incorrect password";
                        case "EMAIL_NOT_FOUND" -> "User not found";
                        case "USER_DISABLED" -> "User account is disabled";
                        default -> "Authentication failed: " + firebaseError;
                    };
                }
            } catch (Exception ignored) {
            }
            return ResponseEntity.badRequest().body(Map.of("error", errorMessage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     */
    @Operation(summary = "Get user profile", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        User user = userService.getUserProfile();
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.status(404).body(Map.of("error", "User not found"));
    }
}