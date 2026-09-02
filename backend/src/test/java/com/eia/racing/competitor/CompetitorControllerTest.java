package com.eia.racing.competitor;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CompetitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUpAdmin() throws Exception {

        TestAuthHelper.createUser(
                userRepository,
                passwordEncoder,
                "admin-test@eia.edu.co",
                "Passw0rd!",
                UserRole.ADMIN
        );

        adminToken = TestAuthHelper.login(
                mockMvc,
                objectMapper,
                "admin-test@eia.edu.co",
                "Passw0rd!"
        );
    }

    @Test
    void createCompetitor_withValidData_returns201() throws Exception {

        mockMvc.perform(post("/api/competitors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "name":"Byte",
                                    "nickname":"ByteCamel",
                                    "competitorType":"CAMEL",
                                    "approximateAge":8,
                                    "weight":480,
                                    "height":2.1,
                                    "countryOrigin":"Colombia"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").value("ByteCamel"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createCompetitor_withNegativeWeight_returns400() throws Exception {

        mockMvc.perform(post("/api/competitors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "name":"Byte",
                                    "nickname":"BadWeight",
                                    "competitorType":"CAMEL",
                                    "approximateAge":8,
                                    "weight":-10,
                                    "height":2.1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void createCompetitor_withDuplicateNickname_returns409() throws Exception {

        String payload = """
                {
                    "name":"Null Pointer",
                    "nickname":"NullPointer",
                    "competitorType":"DWARF",
                    "approximateAge":25,
                    "weight":45,
                    "height":1.1
                }
                """;

        mockMvc.perform(post("/api/competitors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/competitors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void listCompetitors_withTypeFilterAndPagination_returnsFilteredPage() throws Exception {

        createCompetitor(
                "Stack Overflow",
                "StackOverflow",
                "DWARF",
                44,
                1.05
        );

        createCompetitor(
                "Segfault",
                "Segfault",
                "CAMEL",
                500,
                2.2
        );

        mockMvc.perform(get("/api/competitors")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("type", "DWARF")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.content[?(@.competitorType == 'CAMEL')]"
                        ).isEmpty()
                )
                .andExpect(
                        jsonPath(
                                "$.content[?(@.nickname == 'StackOverflow')]"
                        ).exists()
                );
    }

    @Test
    void updateStatusThenDelete_softDeletesCompetitorAsRetired()
            throws Exception {

        String id = createCompetitor(
                "Little Lambda",
                "LittleLambda",
                "DWARF",
                40,
                1.0
        );

        mockMvc.perform(patch("/api/competitors/{id}/status", id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "status":"INJURED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INJURED"));

        mockMvc.perform(delete("/api/competitors/{id}", id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/competitors/{id}", id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETIRED"));
    }

    @Test
    void getCompetitor_whenMissing_returns404() throws Exception {

        mockMvc.perform(get(
                        "/api/competitors/{id}",
                        999_999
                )
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private String createCompetitor(
            String name,
            String nickname,
            String type,
            double weight,
            double height
    ) throws Exception {

        String response = mockMvc.perform(post("/api/competitors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "name":"%s",
                                    "nickname":"%s",
                                    "competitorType":"%s",
                                    "approximateAge":20,
                                    "weight":%s,
                                    "height":%s
                                }
                                """.formatted(
                                name,
                                nickname,
                                type,
                                weight,
                                height
                        )))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper
                .readTree(response)
                .get("id")
                .asText();
    }
}