package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;  // <-- Ajoutez cette ligne
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query("SELECT COUNT(d) > 0 FROM Utilisateur u JOIN u.departments d WHERE u.id = :userId")
    boolean isUserInAnyDepartment(@Param("userId") Long userId);  // <-- @Param sera maintenant reconnu

    // ... autres méthodes existantes
}