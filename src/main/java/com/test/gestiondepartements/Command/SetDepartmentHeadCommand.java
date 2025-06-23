package com.test.gestiondepartements.Command;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.History;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Entities.VoteStatus;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;
import com.test.gestiondepartements.Repositories.VoteRepository;
import com.test.gestiondepartements.Security.Entities.AppRole;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.AppRoleRepository;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;

public class SetDepartmentHeadCommand implements Command {

    private final Long departmentId;
    private final Long candidateUserId;
    private final Long voteId;

    private final DepartmentRepository departmentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VoteRepository voteRepository;
    private final AppRoleRepository appRoleRepository;
    private final HistoryRepository historyRepository;

    public SetDepartmentHeadCommand(Long departmentId, Long candidateUserId, Long voteId,
                                    DepartmentRepository departmentRepository,
                                    UtilisateurRepository utilisateurRepository,
                                    VoteRepository voteRepository,
                                    AppRoleRepository appRoleRepository,
                                    HistoryRepository historyRepository) {
        this.departmentId = departmentId;
        this.candidateUserId = candidateUserId;
        this.voteId = voteId;
        this.departmentRepository = departmentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.voteRepository = voteRepository;
        this.appRoleRepository = appRoleRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public History execute() {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Département non trouvé avec ID: " + departmentId));

        Utilisateur newHead = utilisateurRepository.findById(candidateUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur candidat non trouvé avec ID: " + candidateUserId));

        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote non trouvé avec ID: " + voteId));

        validateVoteAssignment(department, newHead, vote);
        manageRoleTransition(newHead);

        Utilisateur oldHead = department.getHeadOfDepartment();
        department.setHeadOfDepartment(newHead);
        departmentRepository.save(department);

        createHistoryEntry(department, newHead, oldHead, vote);
        return null;
    }

    private void validateVoteAssignment(Department department, Utilisateur newHead, Vote vote) {
        if (!vote.getDepartment().equals(department)) {
            throw new IllegalArgumentException("Le vote ne correspond pas au département spécifié.");
        }
        if (vote.getStatus() != VoteStatus.COMPLETED) {
            throw new IllegalStateException("Impossible de désigner un chef, le vote ID: " + vote.getId() + " pour le département '" + department.getName() + "' n'est pas encore terminé. Statut actuel: " + vote.getStatus());
        }
        if (!vote.getCandidates().stream().anyMatch(c -> c.getUser().equals(newHead))) {
            throw new IllegalArgumentException("L'utilisateur sélectionné n'était pas candidat pour ce vote.");
        }
    }

    private void manageRoleTransition(Utilisateur newHead) {
        AppRole departmentHeadRole = appRoleRepository.findByRoleName("DEPARTMENT_HEAD");
        AppRole enseignantRole = appRoleRepository.findByRoleName("ENSEIGNANT");

        if (departmentHeadRole == null) {
            throw new RuntimeException("Le rôle 'DEPARTMENT_HEAD' n'a pas été trouvé. Veuillez le créer.");
        }

        if (enseignantRole != null && newHead.getAppRoles().contains(enseignantRole)) {
            newHead.getAppRoles().remove(enseignantRole);
        }

        if (!newHead.getAppRoles().contains(departmentHeadRole)) {
            newHead.getAppRoles().add(departmentHeadRole);
        }
        utilisateurRepository.save(newHead);
    }

    private void createHistoryEntry(Department department, Utilisateur newHead,
                                    Utilisateur oldHead, Vote vote) {
        History historyEntry = new History();
        historyEntry.setAction("UPDATE_HEAD");
        historyEntry.setEntityType("Department");
        historyEntry.setEntityId(department.getId());
        String details = buildHistoryDetails(department, newHead, oldHead, vote);
        historyEntry.setDetails(details);
        historyRepository.save(historyEntry);
    }

    private String buildHistoryDetails(Department department, Utilisateur newHead,
                                       Utilisateur oldHead, Vote vote) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chef du département '").append(department.getName()).append("' mis à jour. ");
        sb.append("Nouveau chef: ").append(newHead.getFirstName()).append(" ")
                .append(newHead.getLastName()).append(" (ID: ").append(newHead.getId()).append("). ");

        if (oldHead != null) {
            sb.append("Ancien chef: ").append(oldHead.getFirstName()).append(" ")
                    .append(oldHead.getLastName()).append(" (ID: ").append(oldHead.getId()).append("). ");
        } else {
            sb.append("Aucun chef précédent. ");
        }
        sb.append("Sélectionné suite au vote ID: ").append(vote.getId())
                .append(" (Statut: ").append(vote.getStatus()).append(").");
        return sb.toString();
    }
}