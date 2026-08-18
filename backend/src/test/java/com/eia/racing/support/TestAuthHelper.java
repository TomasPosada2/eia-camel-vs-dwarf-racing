package com.eia.racing.support;

import com.eia.racing.model.User;
import com.eia.racing.model.UserRole;
import com.eia.racing.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Shared helpers for security-flavored MockMvc tests: seed a user and obtain a real access token. */
public final class TestAuthHelper {

    private TestAuthHelper() {
    }

    public static User createUser(UserRepository userRepository, PasswordEncoder encoder,
                                    String email, String rawPassword, UserRole role) {
        User user = User.builder()
                .email(email)
                .password(encoder.encode(rawPassword))
                .fullName("Test " + role.name())
                .role(role)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    public static String login(MockMvc mockMvc, ObjectMapper objectMapper, String email, String password) throws Exception {
        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(responseBody);
        return json.get("accessToken").asText();
    }
}
