package com.test.gestiondepartements.Security.Service;

import com.test.gestiondepartements.Security.Entities.AppRole;
import com.test.gestiondepartements.Security.Entities.Utilisateur;


public interface SecurityService {
    Utilisateur saveNewUser(String username, String password, String rePassword);
    AppRole saveRole(String roleName, String description);
    void addRoleToUser(String email, String roleName);
    Utilisateur loadUserByUsername(String username);
}
