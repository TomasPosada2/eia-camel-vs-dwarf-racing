package com.eia.racing.config;

import com.eia.racing.model.Competitor;
import com.eia.racing.model.CompetitorType;
import com.eia.racing.model.User;
import com.eia.racing.model.UserRole;
import com.eia.racing.repository.CompetitorRepository;
import com.eia.racing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the minimum initial data required by the assignment for the Users and
 * Competitors modules (Persona 1's scope). Teams/Races/Registrations/Results
 * seeding belongs to Persona 2 and Persona 3's own initializers.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String DEFAULT_PASSWORD = "Passw0rd!";

    private final UserRepository userRepository;
    private final CompetitorRepository competitorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedCompetitors();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.save(buildUser("admin@eia.edu.co", "Mr. Abandonado", UserRole.ADMIN));
        userRepository.save(buildUser("organizer@eia.edu.co", "Race Organizer", UserRole.RACE_ORGANIZER));
        userRepository.save(buildUser("viewer@eia.edu.co", "Curious Viewer", UserRole.VIEWER));

        log.info("Seeded default users (admin/organizer/viewer) with password '{}'", DEFAULT_PASSWORD);
    }

    private User buildUser(String email, String fullName, UserRole role) {
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .fullName(fullName)
                .role(role)
                .enabled(true)
                .build();
    }

    private void seedCompetitors() {
        if (competitorRepository.count() > 0) {
            return;
        }

        String[] dwarfNicknames = {"Null Pointer", "Stack Overflow", "Little Lambda", "Captain Cache", "Tiny Docker"};
        for (String nickname : dwarfNicknames) {
            competitorRepository.save(buildCompetitor(nickname, nickname, CompetitorType.DWARF, 45.0, 1.1, "Colombia"));
        }

        competitorRepository.save(buildCompetitor("Byte", "Byte", CompetitorType.CAMEL, 480.0, 2.1, "Colombia"));
        competitorRepository.save(buildCompetitor("Segfault", "Segfault", CompetitorType.CAMEL, 500.0, 2.2, "Colombia"));

        competitorRepository.save(buildCompetitor("Medium Rare", "MediumRare", CompetitorType.MEDIUM, 150.0, 1.6, "Colombia"));
        competitorRepository.save(buildCompetitor("Half Stack", "HalfStack", CompetitorType.MEDIUM, 140.0, 1.55, "Colombia"));

        log.info("Seeded initial competitors: 5 dwarfs, 2 camels, 2 medium competitors");
    }

    private Competitor buildCompetitor(String name, String nickname, CompetitorType type,
                                        double weight, double height, String country) {
        return Competitor.builder()
                .name(name)
                .nickname(nickname)
                .competitorType(type)
                .weight(weight)
                .height(height)
                .countryOrigin(country)
                .build();
    }
}
