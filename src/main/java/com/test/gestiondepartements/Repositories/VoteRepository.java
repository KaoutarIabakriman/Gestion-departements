package com.test.gestiondepartements.Repositories;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Entities.VoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Vote findByDepartmentAndStatus(Department department, VoteStatus status);
    boolean existsByDepartmentAndStatusIn(Department department, List<VoteStatus> statuses);
    @Query("SELECT v FROM Vote v LEFT JOIN FETCH v.department d LEFT JOIN FETCH d.members WHERE v.id = :voteId")
    Optional<Vote> findByIdWithDepartmentAndMembers(@Param("voteId") Long voteId);

}