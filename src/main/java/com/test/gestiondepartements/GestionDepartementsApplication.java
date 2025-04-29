package com.test.gestiondepartements;

import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Security.Service.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@Slf4j
public class GestionDepartementsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionDepartementsApplication.class, args);
    }

    @Bean
    CommandLineRunner saveUsersAndRoles(SecurityService securityService, UtilisateurRepository utilisateurRepository) {
        return args -> {
            createRoles(securityService);

            createAdminUser(securityService, "admin1@uae.ac.ma", "password1");

            List<Map.Entry<String, String>> enseignants = List.of(
                    Map.entry("enseignant.java@uae.ac.ma", "Java, Spring, DevOps"),
                    Map.entry("enseignant.math@uae.ac.ma", "Mathématiques, Algèbre"),
                    Map.entry("enseignant.cloud@uae.ac.ma", "AWS, Cloud, DevOps"),
                    Map.entry("enseignant.data@uae.ac.ma", "Python, Data Science, ML")
            );

            enseignants.forEach(entry -> {
                createTeacher(
                        securityService,
                        utilisateurRepository,
                        entry.getKey(),
                        "password",
                        entry.getValue()
                );
            });
        };
    }

    private void createRoles(SecurityService securityService) {
        try {
            securityService.saveRole("ADMIN", "Administrateur système");
            securityService.saveRole("CHEF_DEPARTEMENT", "Responsable département");
            securityService.saveRole("ENSEIGNANT", "Enseignant-chercheur");
            log.info("Rôles créés avec succès");
        } catch (Exception e) {
            log.warn("Erreur création rôles : {}", e.getMessage());
        }
    }

    private void createAdminUser(SecurityService securityService, String email, String password) {
        try {
            Utilisateur admin = securityService.saveNewUser(email, password, password);
            securityService.addRoleToUser(email, "ADMIN");
            log.info("Admin {} créé", email);
        } catch (Exception e) {
            log.warn("Admin existant : {}", email);
        }
    }

    private void createTeacher(SecurityService securityService,
                               UtilisateurRepository repository,
                               String email,
                               String password,
                               String skills) {
        try {
            Utilisateur teacher = securityService.saveNewUser(email, password, password);

            securityService.addRoleToUser(email, "ENSEIGNANT");

            teacher.setSkills(skills);
            repository.save(teacher);

            log.info("Enseignant {} créé avec compétences : {}", email, skills);

        } catch (Exception e) {
            log.warn("Erreur création enseignant {} : {}", email, e.getMessage());
        }
    }
}