package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Entities.Candidate;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface DepartmentService {
    Page<Department> getAllDepartments(Pageable pageable);
    void addUserToDepartment(Long departmentId, Utilisateur user);
    boolean departmentMatchesSkills(Department department, Utilisateur user);
    void startVote(Long departmentId, LocalDateTime endDate);
    List<Candidate> getCandidatesForVote(Long voteId);
}