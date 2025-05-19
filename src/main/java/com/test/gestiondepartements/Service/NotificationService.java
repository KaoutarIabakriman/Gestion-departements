package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Notification;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Repositories.NotificationRepository;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    public void createNewDepartmentNotification(Department department, String message) {
        List<Utilisateur> enseignants = utilisateurRepository.findByAppRoles_RoleName("ENSEIGNANT");

        for (Utilisateur enseignant : enseignants) {
            if (departmentMatchesSkills(department, enseignant)) {
                Notification notification = new Notification();
                notification.setUser(enseignant);
                notification.setDepartment(department);
                notification.setMessage(message);
                notification.setType(NotificationType.NEW_DEPARTMENT);
                notificationRepository.save(notification);
            }
        }
    }

    private boolean departmentMatchesSkills(Department department, Utilisateur enseignant) {
        if (enseignant.getSkills() == null || enseignant.getSkills().trim().isEmpty()) return false;
        if (department.getDescription() == null || department.getDescription().trim().isEmpty()) return false;

        String[] skills = enseignant.getSkills().split("\\s*,\\s*");
        String description = department.getDescription().toLowerCase();

        return Arrays.stream(skills)
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .anyMatch(skill -> description.contains(skill.toLowerCase()));
    }
    public void createNotification(Utilisateur user, Department department, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setDepartment(department);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }
    public List<Notification> getUnreadNotifications(Utilisateur user) {
        return notificationRepository.findByUserAndReadStatusFalseOrderByCreatedAtDesc(user);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setReadStatus(true);
            notificationRepository.save(notification);
        });
    }
}