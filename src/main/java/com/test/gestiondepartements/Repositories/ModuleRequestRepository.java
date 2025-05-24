// File: src/main/java/com/test/gestiondepartements/Repositories/ModuleRequestRepository.java
package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Entities.ModuleRequest;
import com.test.gestiondepartements.Entities.ModuleRequestStatus;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModuleRequestRepository extends JpaRepository<ModuleRequest, Long> {

    @Query("SELECT mr FROM ModuleRequest mr JOIN FETCH mr.module m JOIN FETCH mr.enseignant e WHERE m.department.id = :departmentId AND mr.status = :status ORDER BY m.name, mr.requestDate")

    List<ModuleRequest> findByModuleAndStatus(Module module, ModuleRequestStatus status);

    boolean existsByModuleAndEnseignantAndStatus(Module module, Utilisateur enseignant, ModuleRequestStatus status);

    @Query("SELECT mr FROM ModuleRequest mr JOIN FETCH mr.module m JOIN FETCH mr.enseignant e WHERE m.department.id = :departmentId ORDER BY mr.requestDate DESC")
    List<ModuleRequest> findByDepartmentIdWithDetails(@Param("departmentId") Long departmentId);
}