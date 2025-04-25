package com.test.gestiondepartements;

import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Service.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@Slf4j
public class GestionDepartementsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionDepartementsApplication.class, args);
    }

    @Bean
    CommandLineRunner saveUsersAndRoles(SecurityService securityService) {
        return args -> {
            log.info("Starting data initialization from PatientMvcApplication...");
            try {
                securityService.saveRole("ADMIN", "Administrator Role");
                securityService.saveRole("CHEF_DEPARTEMENT", "Department Head Role");
                securityService.saveRole("ENSEIGNANT", "Teacher Role");
                log.info("Standard roles checked/created.");
            } catch (RuntimeException e) {
                log.warn("Error creating roles (they might already exist): {}", e.getMessage());
            }

            String adminEmail = "admin1@uae.ac.ma";
            try {
                Utilisateur adminUser = securityService.saveNewUser(adminEmail, "password1", "password1");
                log.info("Attempting to add ADMIN role to user {}", adminEmail);
                securityService.addRoleToUser(adminUser.getUsername(), "ADMIN");
                log.info("Admin user created and ADMIN role assigned successfully.");
            } catch (RuntimeException e) {
                if (e.getMessage().contains("already exists")) {
                    log.warn("Admin user {} likely already exists. Attempting to assign ADMIN role.", adminEmail);
                    try {
                        securityService.addRoleToUser(adminEmail, "ADMIN");
                        log.info("ADMIN role assigned successfully to existing user {}.", adminEmail);
                    } catch (RuntimeException e2) {
                        log.error("Failed to assign ADMIN role to existing user {}: {}", adminEmail, e2.getMessage());
                    }
                } else {
                    log.error("Error creating admin user {}: {}", adminEmail, e.getMessage());
                }
            }

            log.info("Data initialization from PatientMvcApplication finished.");

            String enseignantUsername = "enseignant.test@uae.ac.ma";
            try {
                Utilisateur enseignantUser = securityService.saveNewUser(enseignantUsername, "password", "password");
                log.info("Attempting to add ENSEIGNANT role to user {}", enseignantUsername);
                securityService.addRoleToUser(enseignantUser.getUsername(), "ENSEIGNANT");
                log.info("Enseignant user created and ENSEIGNANT role assigned successfully.");

            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                    log.warn("Enseignant user {} likely already exists. Attempting to assign ENSEIGNANT role.", enseignantUsername);
                    try {
                        securityService.addRoleToUser(enseignantUsername, "ENSEIGNANT");
                        log.info("ENSEIGNANT role assigned successfully to existing user {}.", enseignantUsername);
                    } catch (RuntimeException e2) {
                        log.error("Failed to assign ENSEIGNANT role to existing user {}: {}", enseignantUsername, e2.getMessage());
                    }
                } else {
                    log.error("Error creating enseignant user {}: {}", enseignantUsername, e.getMessage() != null ? e.getMessage() : "Unknown error");
                }
            }
            log.info("Data initialization from PatientMvcApplication finished.");
        };

    }
}