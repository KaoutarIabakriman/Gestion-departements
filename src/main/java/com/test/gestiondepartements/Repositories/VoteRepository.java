package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Entities.VoteStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    List<Vote> findByStatus(VoteStatus status);
    @Query("SELECT v FROM Vote v LEFT JOIN FETCH v.department d WHERE v.id = :voteId")


    boolean existsByDepartmentAndStatusIn(Department department, List<VoteStatus> statuses);

    Vote findByDepartmentAndStatus(Department department, VoteStatus status);
    List<Vote> findByDepartmentIdAndStatus(Long departmentId, VoteStatus status);
}