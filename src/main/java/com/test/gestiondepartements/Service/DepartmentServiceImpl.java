package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Entities.*;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;
import com.test.gestiondepartements.Repositories.VoteRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final VoteRepository voteRepository;
    private final HistoryRepository historyRepository;
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
        History historyEntry = new History();
        historyEntry.setAction("CREATE");
        historyEntry.setEntityType("Vote");
        historyEntry.setEntityId(savedVote.getId());
        historyEntry.setDetails("Vote started for department: " + department.getName() + ", ending on: " + endDate);
        historyRepository.save(historyEntry);
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
                    null,
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
            throw new IllegalStateException("L'utilisateur est déjà membre d'un département.");
        }
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Département non trouvé avec ID: " + departmentId));
        if (user.getDepartments() == null) {
            user.setDepartments(new ArrayList<>());
        }

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

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void checkAndFinalizeVotes() {
        System.out.println("SCHEDULER: Exécution de checkAndFinalizeVotes à " + LocalDateTime.now());
        List<Vote> activeVotes = voteRepository.findByStatus(VoteStatus.ACTIVE);
        System.out.println("SCHEDULER: Nombre de votes actifs trouvés: " + activeVotes.size());

        LocalDateTime now = LocalDateTime.now();

        for (Vote vote : activeVotes) {
            System.out.println("SCHEDULER: Traitement du vote ID: " + vote.getId() + ", EndDate: " + vote.getEndDate());
            if (vote.getEndDate() != null && now.isAfter(vote.getEndDate())) {
                System.out.println("SCHEDULER: Le vote ID: " + vote.getId() + " est terminé. Changement de statut...");
                vote.setStatus(VoteStatus.COMPLETED);
                voteRepository.save(vote);

                List<Utilisateur> admins = utilisateurRepository.findByAppRoles_RoleName("ADMIN");
                String departmentName = vote.getDepartment() != null ? vote.getDepartment().getName() : "N/A";

                String message;
                if (vote.getDepartment() != null) {
                    message = "Le vote (ID: " + vote.getId() + ") pour le chef du département '" + departmentName + "' est terminé. " +
                            "Veuillez <a href='/admin/candidates?voteId=" + vote.getId() + "'>cliquer ici</a> pour consulter les candidats et désigner le chef.";
                } else {
                    message = "Un vote (ID: " + vote.getId() +") est terminé, mais les détails du département sont indisponibles. Action manuelle requise pour vérifier les candidats.";
                }
                notificationService.createGeneralNotification(admins, message, vote, null);
                System.out.println("SCHEDULER: Notification envoyée pour le vote ID: " + vote.getId());


                History historyEntry = new History();
                historyEntry.setAction("COMPLETE");
                historyEntry.setEntityType("Vote");
                historyEntry.setEntityId(vote.getId());
                historyEntry.setDetails("Vote (ID: " + vote.getId() + ") pour le département '" + departmentName + "' est automatiquement marqué comme terminé.");
                historyRepository.save(historyEntry);
                System.out.println("SCHEDULER: Entrée d'historique ajoutée pour le vote ID: " + vote.getId());

                System.out.println("SCHEDULER: Vote ID: " + vote.getId() + " pour le département '" + departmentName + "' finalisé.");
            } else {
                if (vote.getEndDate() == null) {
                    System.out.println("SCHEDULER: Vote ID: " + vote.getId() + " n'a pas de EndDate définie.");
                } else {
                    System.out.println("SCHEDULER: Vote ID: " + vote.getId() + " n'est pas encore terminé. EndDate: " + vote.getEndDate() + ", Now: " + now);
                }
            }
        }
        System.out.println("SCHEDULER: Fin de checkAndFinalizeVotes.");
    }

}