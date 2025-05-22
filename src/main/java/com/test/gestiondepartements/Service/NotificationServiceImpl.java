package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Notification;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Entities.Vote;
// import com.test.gestiondepartements.Entities.Module; // Ajouter si vous passez le module
import com.test.gestiondepartements.Repositories.NotificationRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
// import java.util.Optional; // Non utilisé ici

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository; // S'assurer qu'il est injecté

    @Override
    public void createNotification(Utilisateur user, @Nullable Department department, String message, NotificationType type, @Nullable Vote vote) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setDepartment(department);
        notification.setMessage(message);
        notification.setType(type);
        notification.setVote(vote);
        notification.setReadStatus(false); // Important
        notificationRepository.save(notification);
    }

    @Override
    public void createNewDepartmentNotification(Department department, String message) {
        List<Utilisateur> enseignants = utilisateurRepository.findByAppRoles_RoleName("ENSEIGNANT");
        for (Utilisateur enseignant : enseignants) {
            // Ici, on pourrait vouloir que le message soit plus spécifique ou que la logique de compétence
            // soit appelée explicitement si le 'message' ne l'inclut pas déjà.
            // Cette méthode notifie sur la base de la description du DÉPARTEMENT.
            if (this.skillsMatchDescription(enseignant.getSkills(), department.getDescription())) {
                createNotification(enseignant, department, message + " (Département : " + department.getName() + ")", NotificationType.NEW_DEPARTMENT, null);
            }
        }
    }

    // Nouvelle méthode publique pour vérifier la correspondance des compétences avec une description donnée
    // Peut être utilisée par d'autres services/contrôleurs.
    public boolean skillsMatchDescription(@Nullable String userSkills, @Nullable String entityDescription) {
        if (userSkills == null || userSkills.trim().isEmpty() ||
                entityDescription == null || entityDescription.trim().isEmpty()) {
            return false;
        }
        String[] skillsArray = userSkills.toLowerCase().split("\\s*,\\s*");
        String descriptionLower = entityDescription.toLowerCase();
        return Arrays.stream(skillsArray)
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .anyMatch(descriptionLower::contains);
    }


    @Override
    public void createGeneralNotification(List<Utilisateur> users, String message, Vote vote) {
        for (Utilisateur user : users) {
            createNotification(user, (vote != null ? vote.getDepartment() : null), message, NotificationType.GENERAL, vote);
        }
    }

    @Override
    public List<Notification> getUnreadNotifications(Utilisateur user) {
        return notificationRepository.findByUserAndReadStatusFalseOrderByCreatedAtDesc(user);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (!notification.isReadStatus()) {
                notification.setReadStatus(true);
                notificationRepository.save(notification);
            }
        });
    }

    // L'ancienne méthode privée `departmentMatchesSkills` peut être supprimée si remplacée par `skillsMatchDescription`
    // private boolean departmentMatchesSkills(Department department, Utilisateur enseignant) { ... }

}