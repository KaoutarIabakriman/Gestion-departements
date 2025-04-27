package com.test.gestiondepartements.Security.Repositories;


import com.test.gestiondepartements.Security.Entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Utilisateur findByUsername(String email);

    List<Utilisateur> findByAppRoles_RoleName(String enseignant);
}