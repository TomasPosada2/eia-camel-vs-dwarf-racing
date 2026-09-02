package com.eia.racing.race;

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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RaceControllerTest {

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
                "admin-races@eia.edu.co",
                "Passw0rd!",
                UserRole.ADMIN
        );

        adminToken = TestAuthHelper.login(
                mockMvc,
                objectMapper,
                "admin-races@eia.edu.co",
                "Passw0rd!"
        );
    }

    @Test
    void adminCanCreateValidRace_returns201() throws Exception {

        LocalDateTime raceDate = LocalDateTime.now().plusDays(10);
        LocalDateTime deadline = LocalDateTime.now().plusDays(5);

        mockMvc.perform(post("/api/races")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "name": "EIA Grand Race",
                                    "description": "Test race",
                                    "scheduledDateTime": "%s",
                                    "startLocation": "EIA Main Gate",
                                    "endLocation": "EIA Stadium",
                                    "distanceMeters": 1500,
                                    "maxParticipants": 10,
                                    "type": "INDIVIDUAL",
                                    "registrationDeadline": "%s"
                                }
                                """.formatted(raceDate, deadline)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("EIA Grand Race"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void createRaceInPast_returns400() throws Exception {

        LocalDateTime raceDate = LocalDateTime.now().minusDays(1);
        LocalDateTime deadline = LocalDateTime.now().plusHours(1);

        mockMvc.perform(post("/api/races")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "name": "Past Race",
                                    "description": "Invalid race",
                                    "scheduledDateTime": "%s",
                                    "startLocation": "Start",
                                    "endLocation": "Finish",
                                    "distanceMeters": 1000,
                                    "maxParticipants": 10,
                                    "type": "INDIVIDUAL",
                                    "registrationDeadline": "%s"
                                }
                                """.formatted(raceDate, deadline)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void viewerCannotCreateRace_returns403() throws Exception {

        TestAuthHelper.createUser(
                userRepository,
                passwordEncoder,
                "viewer-races@eia.edu.co",
                "Passw0rd!",
                UserRole.VIEWER
        );

        String viewerToken = TestAuthHelper.login(
                mockMvc,
                objectMapper,
                "viewer-races@eia.edu.co",
                "Passw0rd!"
        );

        LocalDateTime raceDate = LocalDateTime.now().plusDays(10);
        LocalDateTime deadline = LocalDateTime.now().plusDays(5);

        mockMvc.perform(post("/api/races")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "name": "Viewer Race",
                                    "description": "Should not be created",
                                    "scheduledDateTime": "%s",
                                    "startLocation": "Start",
                                    "endLocation": "Finish",
                                    "distanceMeters": 1000,
                                    "maxParticipants": 10,
                                    "type": "INDIVIDUAL",
                                    "registrationDeadline": "%s"
                                }
                                """.formatted(raceDate, deadline)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateTeamRace_returns201() throws Exception {

        LocalDateTime raceDate = LocalDateTime.now().plusDays(15);
        LocalDateTime deadline = LocalDateTime.now().plusDays(10);

        mockMvc.perform(post("/api/races")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "name": "Team Challenge",
                                    "description": "Team race test",
                                    "scheduledDateTime": "%s",
                                    "startLocation": "North Gate",
                                    "endLocation": "South Gate",
                                    "distanceMeters": 2500,
                                    "maxParticipants": 12,
                                    "type": "TEAM",
                                    "registrationDeadline": "%s"
                                }
                                """.formatted(raceDate, deadline)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TEAM"));
    }

    @Test
    void registrationDeadlineAfterRaceStart_returns409() throws Exception {

        LocalDateTime raceDate = LocalDateTime.now().plusDays(5);
        LocalDateTime invalidDeadline = LocalDateTime.now().plusDays(10);

        mockMvc.perform(post("/api/races")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                            {
                                "name": "Invalid Deadline Race",
                                "description": "Race with invalid registration deadline",
                                "scheduledDateTime": "%s",
                                "startLocation": "EIA Main Gate",
                                "endLocation": "EIA Stadium",
                                "distanceMeters": 1500,
                                "maxParticipants": 10,
                                "type": "INDIVIDUAL",
                                "registrationDeadline": "%s"
                            }
                            """.formatted(raceDate, invalidDeadline)))
                .andExpect(status().isConflict());
    }

    @Test
    void raceCannotGoDirectlyFromDraftToCompleted_returns409() throws Exception {

        LocalDateTime raceDate = LocalDateTime.now().plusDays(10);
        LocalDateTime deadline = LocalDateTime.now().plusDays(5);

        // Crear carrera: queda automáticamente en DRAFT
        String response = mockMvc.perform(post("/api/races")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                            {
                                "name": "Invalid Transition Race",
                                "description": "Race for status transition test",
                                "scheduledDateTime": "%s",
                                "startLocation": "Start",
                                "endLocation": "Finish",
                                "distanceMeters": 1200,
                                "maxParticipants": 10,
                                "type": "INDIVIDUAL",
                                "registrationDeadline": "%s"
                            }
                            """.formatted(raceDate, deadline)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long raceId = objectMapper
                .readTree(response)
                .get("id")
                .asLong();

        // Intentar saltar directamente de DRAFT a COMPLETED
        mockMvc.perform(patch("/api/races/{id}/status", raceId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("""
                            {
                                "status": "COMPLETED"
                            }
                            """))
                .andExpect(status().isConflict());
    }
}