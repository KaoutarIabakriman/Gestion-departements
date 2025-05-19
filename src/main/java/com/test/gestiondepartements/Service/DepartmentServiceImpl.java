package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Entities.Candidate;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Entities.VoteStatus;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.VoteRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final VoteRepository voteRepository;

    @Override
    public Page<Department> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void startVote(Long departmentId, LocalDateTime endDate) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));
        if (voteRepository.existsByDepartmentAndStatusIn(department, List.of(VoteStatus.ACTIVE, VoteStatus.PENDING))) {
            throw new IllegalStateException("An active or pending vote already exists for this department.");
        }
        Vote vote = new Vote();
        vote.setDepartment(department);
        vote.setStatus(VoteStatus.ACTIVE);
        vote.setEndDate(endDate);
        Vote savedVote = voteRepository.save(vote);

        List<Utilisateur> members = department.getMembers();
        if (members.isEmpty()) {
            savedVote.setStatus(VoteStatus.COMPLETED);
            voteRepository.save(savedVote);
            return;
        }

        for (Utilisateur member : members) {
            notificationService.createNotification(
                    member,
                    department,
                    "Un vote pour le chef du département '" + department.getName() + "' a commencé. Veuillez participer.",
                    NotificationType.VOTE,
                    savedVote
            );
        }
    }

    @Override
    public List<Candidate> getCandidatesForVote(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote not found"));
        return vote.getCandidates();
    }

    @Override
    public void addUserToDepartment(Long departmentId, Utilisateur user) {
        if (user.isInAnyDepartment()) {
            throw new IllegalStateException("L'utilisateur est déjà membre d'un département");
        }
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));
        user.getDepartments().add(department);
        utilisateurRepository.save(user);
    }

    @Override
    public boolean departmentMatchesSkills(Department department, Utilisateur user) {
        if (user.getSkills() == null || user.getSkills().trim().isEmpty() ||
                department.getDescription() == null || department.getDescription().trim().isEmpty()) {
            return false;
        }
        String[] userSkills = user.getSkills().toLowerCase().split("\\s*,\\s*");
        String departmentDescription = department.getDescription().toLowerCase();
        return Arrays.stream(userSkills)
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .anyMatch(departmentDescription::contains);
    }
}