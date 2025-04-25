package com.test.gestiondepartements.Security.Service;

import com.test.gestiondepartements.Security.Entities.AppRole;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.AppRoleRepository;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
@Transactional
public class SecurityServiceImpl implements SecurityService {
    private UtilisateurRepository utilisateurRepository;
    private AppRoleRepository appRoleRepository;
    private PasswordEncoder passwordEncoder;


    @Override
    public Utilisateur saveNewUser(String username, String password, String rePassword) {
        if(!password.equals(rePassword)) throw new RuntimeException("Passwords don't match");
        Utilisateur existingUser = utilisateurRepository.findByUsername(username);
        if (existingUser != null) throw new RuntimeException("Username (Email) already exists!");

        String hashedPWD = passwordEncoder.encode(password);
        Utilisateur appUser = new Utilisateur();
        appUser.setUsername(username);
        appUser.setPassword(hashedPWD);
        Utilisateur savedAppUser = utilisateurRepository.save(appUser);
        log.info("New user saved: {}", username);
        return savedAppUser;
    }



    @Override
    public AppRole saveRole(String roleName, String description) {
        AppRole appRole = appRoleRepository.findByRoleName(roleName);
        if(appRole != null) {
            log.warn("Role {} already exists.", roleName);
            return appRole;
        }
        appRole = new AppRole();
        appRole.setRoleName(roleName);
        appRole.setDescription(description);
        AppRole savedAppRole= appRoleRepository.save(appRole);
        log.info("New role saved: {}", roleName);
        return savedAppRole;
    }

    @Override
    public void addRoleToUser(String username, String roleName) {
        Utilisateur appUser = utilisateurRepository.findByUsername(username);
        if(appUser == null) {
            log.error("User not found: {}", username);
            throw new RuntimeException("User not found");
        }
        AppRole appRole = appRoleRepository.findByRoleName(roleName);
        if(appRole == null) {
            log.error("Role not found: {}", roleName);
            throw new RuntimeException("Role not found");
        }
        if (!appUser.getAppRoles().contains(appRole)) {
            appUser.getAppRoles().add(appRole);
            log.info("Role {} added to user {}", roleName, username);
        } else {
            log.warn("User {} already has role {}", username, roleName);
        }
    }

    @Override
    public Utilisateur loadUserByUsername(String username) {
        Utilisateur user = utilisateurRepository.findByUsername(username);
        if (user == null) {
            log.error("User not found during loadUserByUsername: {}", username);
        }
        return user;
    }
}