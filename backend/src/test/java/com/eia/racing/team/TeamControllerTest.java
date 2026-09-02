package com.eia.racing.team;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TeamControllerTest {

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
                "admin-teams@eia.edu.co",
                "Passw0rd!",
                UserRole.ADMIN
        );

        adminToken = TestAuthHelper.login(
                mockMvc,
                objectMapper,
                "admin-teams@eia.edu.co",
                "Passw0rd!"
        );
    }

    @Test
    void adminCanCreateTeam() throws Exception {

        mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Binary Racers",
                                  "description": "EIA racing team",
                                  "coach": "Ada Lovelace"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Binary Racers"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void duplicateTeamNameReturns409() throws Exception {

        String payload = """
                {
                  "name": "Code Runners",
                  "description": "First team",
                  "coach": "Alan Turing"
                }
                """;

        mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void viewerCannotCreateTeam() throws Exception {

        TestAuthHelper.createUser(
                userRepository,
                passwordEncoder,
                "viewer-teams@eia.edu.co",
                "Passw0rd!",
                UserRole.VIEWER
        );

        String viewerToken = TestAuthHelper.login(
                mockMvc,
                objectMapper,
                "viewer-teams@eia.edu.co",
                "Passw0rd!"
        );

        mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Viewer Team",
                                  "description": "Should not be created",
                                  "coach": "Grace Hopper"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicateMemberInSameTeam_returns409() throws Exception {

        // 1. Crear un equipo
        String teamResponse = mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                            {
                                "name": "Duplicate Member Team",
                                "description": "Team for duplicate member test",
                                "coach": "Coach Test"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long teamId = objectMapper
                .readTree(teamResponse)
                .get("id")
                .asLong();

        // 2. Crear un competidor
        String competitorResponse = mockMvc.perform(post("/api/competitors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                            {
                                "name": "Duplicate Camel",
                                "nickname": "DuplicateCamel",
                                "competitorType": "CAMEL",
                                "weight": 480,
                                "height": 2.1,
                                "countryOrigin": "Colombia"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long competitorId = objectMapper
                .readTree(competitorResponse)
                .get("id")
                .asLong();

        // 3. Agregarlo por primera vez: debe funcionar
        mockMvc.perform(post(
                        "/api/teams/{teamId}/members/{competitorId}",
                        teamId,
                        competitorId
                )
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1));

        // 4. Intentar agregar el MISMO competidor otra vez
        mockMvc.perform(post(
                        "/api/teams/{teamId}/members/{competitorId}",
                        teamId,
                        competitorId
                )
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    @Test
    void competitorCannotBelongToTwoActiveTeams_returns409() throws Exception {

        // 1. Crear el primer equipo
        String firstTeamResponse = mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                            {
                                "name": "First Active Team",
                                "description": "First team",
                                "coach": "Coach One"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long firstTeamId = objectMapper
                .readTree(firstTeamResponse)
                .get("id")
                .asLong();

        // 2. Crear el segundo equipo
        String secondTeamResponse = mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                            {
                                "name": "Second Active Team",
                                "description": "Second team",
                                "coach": "Coach Two"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long secondTeamId = objectMapper
                .readTree(secondTeamResponse)
                .get("id")
                .asLong();

        // 3. Crear un competidor
        String competitorResponse = mockMvc.perform(post("/api/competitors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                            {
                                "name": "Shared Camel",
                                "nickname": "SharedCamel",
                                "competitorType": "CAMEL",
                                "weight": 470,
                                "height": 2.0,
                                "countryOrigin": "Colombia"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long competitorId = objectMapper
                .readTree(competitorResponse)
                .get("id")
                .asLong();

        // 4. Agregar el competidor al primer equipo
        mockMvc.perform(post(
                        "/api/teams/{teamId}/members/{competitorId}",
                        firstTeamId,
                        competitorId
                )
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 5. Intentar agregarlo al segundo equipo activo
        mockMvc.perform(post(
                        "/api/teams/{teamId}/members/{competitorId}",
                        secondTeamId,
                        competitorId
                )
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }
}