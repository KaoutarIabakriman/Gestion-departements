package com.test.gestiondepartements.Security.Repositories;

import com.test.gestiondepartements.Security.Entities.Utilisateur;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Utilisateur findByUsername(String email);
    List<Utilisateur> findByAppRoles_RoleName(String enseignant);


    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.departments WHERE u.id = :id")
    Optional<Utilisateur> findByIdWithDepartments(@Param("id") Long id);
}