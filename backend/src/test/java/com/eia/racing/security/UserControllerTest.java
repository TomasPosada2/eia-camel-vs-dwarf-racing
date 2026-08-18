package com.eia.racing.security;

import com.eia.racing.model.User;
import com.eia.racing.model.UserRole;
import com.eia.racing.repository.UserRepository;
import com.eia.racing.support.TestAuthHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private String adminToken;

    @BeforeEach
    void setUpAdmin() throws Exception {
        admin = TestAuthHelper.createUser(userRepository, passwordEncoder, "admin-users@eia.edu.co", "Passw0rd!", UserRole.ADMIN);
        adminToken = TestAuthHelper.login(mockMvc, objectMapper, "admin-users@eia.edu.co", "Passw0rd!");
    }

    @Test
    void adminCanCreateUserWithSpecificRole() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {"email":"neworganizer@eia.edu.co","password":"Passw0rd!","fullName":"New Organizer","role":"RACE_ORGANIZER"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("RACE_ORGANIZER"));

        User saved = userRepository.findByEmailIgnoreCase("neworganizer@eia.edu.co").orElseThrow();
        assertThat(saved.getRole()).isEqualTo(UserRole.RACE_ORGANIZER);
        assertThat(passwordEncoder.matches("Passw0rd!", saved.getPassword())).isTrue();
    }

    @Test
    void adminCanDeleteAnotherUser() throws Exception {
        User target = TestAuthHelper.createUser(userRepository, passwordEncoder, "todelete@eia.edu.co", "Passw0rd!", UserRole.VIEWER);

        mockMvc.perform(delete("/api/users/{id}", target.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(target.getId())).isEmpty();
    }

    @Test
    void adminCannotDeleteTheirOwnAccount() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", admin.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());

        assertThat(userRepository.findById(admin.getId())).isPresent();
    }
}
