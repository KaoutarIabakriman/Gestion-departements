package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Entities.VoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByStatus(VoteStatus status);

    // This is the critical method used in VoteController
    @Query("SELECT v FROM Vote v LEFT JOIN FETCH v.department d WHERE v.id = :voteId") // Fetch department
    // If department members are also needed by the caller through this vote object context:
    // @Query("SELECT v FROM Vote v LEFT JOIN FETCH v.department d LEFT JOIN FETCH d.members WHERE v.id = :voteId")
    Optional<Vote> findByIdWithDepartmentAndMembers(@Param("voteId") Long voteId);

    // Other custom methods used elsewhere in the project (based on other files):
    boolean existsByDepartmentAndStatusIn(Department department, List<VoteStatus> statuses); // Likely derivable by name

    Vote findByDepartmentAndStatus(Department department, VoteStatus status); // Likely derivable by name

    List<Vote> findByStatusAndEndDateBefore(VoteStatus status, LocalDateTime endDate); // Likely derivable by name
}