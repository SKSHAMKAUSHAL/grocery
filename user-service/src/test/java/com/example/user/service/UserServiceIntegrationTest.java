package com.example.user.service;

import com.example.user.entity.User;
import com.example.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
    "org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration," +
    "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
})
@Testcontainers
@Transactional
public class UserServiceIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("userdb")
        .withUsername("postgres")
        .withPassword("password");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("testUid", null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testSaveAndGetUserProfile() {
        User user = new User();
        user.setId("testUid");
        user.setEmail("test@example.com");
        user.setRole("CONSUMER");
        userService.saveUser(user);

        User retrievedUser = userService.getUserProfile();

        assertNotNull(retrievedUser);
        assertEquals("testUid", retrievedUser.getId());
        assertEquals("test@example.com", retrievedUser.getEmail());
        assertEquals("CONSUMER", retrievedUser.getRole());
    }

    @Test
    void testGetUserProfileNotFound() {
        User retrievedUser = userService.getUserProfile();
        assertEquals(null, retrievedUser);
    }
}