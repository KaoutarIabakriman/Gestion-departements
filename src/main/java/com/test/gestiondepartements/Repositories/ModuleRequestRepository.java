package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Entities.ModuleRequest;
import com.test.gestiondepartements.Entities.ModuleRequestStatus;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ModuleRequestRepository extends JpaRepository<ModuleRequest, Long> {

    @Query("SELECT mr FROM ModuleRequest mr WHERE mr.module = :module AND mr.status = :status")
    List<ModuleRequest> findByModuleAndStatus(@Param("module") Module module, @Param("status") ModuleRequestStatus status);
    boolean existsByModuleAndEnseignantAndStatus(Module module, Utilisateur enseignant, ModuleRequestStatus status);


    @Query("SELECT mr FROM ModuleRequest mr " +
            "WHERE mr.module.department = :department " +
            "AND (COALESCE(:keyword, '') = '' OR mr.module.name LIKE %:keyword% OR mr.enseignant.firstName LIKE %:keyword% OR mr.enseignant.lastName LIKE %:keyword%) " +
            "AND (COALESCE(:status, '') = '' OR mr.status = :status) " +
            "AND (COALESCE(:startDate, '') = '' OR mr.requestDate >= :startDate) " +
            "AND (COALESCE(:endDate, '') = '' OR mr.requestDate <= :endDate)")
    Page<ModuleRequest> findAllByCriteria(@Param("department") Department department,
                                          @Param("keyword") String keyword,
                                          @Param("status") ModuleRequestStatus status,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);
    @Query("SELECT e.id FROM ModuleRequest mr JOIN mr.enseignant e WHERE mr.module = :module AND mr.status = :status")
    List<Long> findEnseignantIdsByModuleAndStatus(@Param("module") Module module, @Param("status") ModuleRequestStatus status);
}