package com.eia.racing.security;

import com.eia.racing.model.UserRole;
import com.eia.racing.repository.UserRepository;
import com.eia.racing.support.TestAuthHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_createsUserWithHashedPasswordAndViewerRoleByDefault() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"newracer@eia.edu.co","password":"Passw0rd!","fullName":"New Racer"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("VIEWER"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        var saved = userRepository.findByEmailIgnoreCase("newracer@eia.edu.co").orElseThrow();
        assertThat(saved.getRole()).isEqualTo(UserRole.VIEWER);
        assertThat(saved.getPassword()).isNotEqualTo("Passw0rd!");
        assertThat(passwordEncoder.matches("Passw0rd!", saved.getPassword())).isTrue();
    }

    @Test
    void login_withValidCredentials_returnsJwt() throws Exception {
        TestAuthHelper.createUser(userRepository, passwordEncoder, "valid@eia.edu.co", "Passw0rd!", UserRole.VIEWER);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"valid@eia.edu.co","password":"Passw0rd!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        TestAuthHelper.createUser(userRepository, passwordEncoder, "wrongpass@eia.edu.co", "Passw0rd!", UserRole.VIEWER);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"wrongpass@eia.edu.co","password":"NotTheRightOne!"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/competitors"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminOnlyEndpoint_asViewer_returns403() throws Exception {
        TestAuthHelper.createUser(userRepository, passwordEncoder, "vieweronly@eia.edu.co", "Passw0rd!", UserRole.VIEWER);
        String token = TestAuthHelper.login(mockMvc, objectMapper, "vieweronly@eia.edu.co", "Passw0rd!");

        mockMvc.perform(post("/api/competitors")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"name":"Byte","nickname":"ByteTheCamel","competitorType":"CAMEL","weight":480,"height":2.1}
                                """))
                .andExpect(status().isForbidden());
    }
}
