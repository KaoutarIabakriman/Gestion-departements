package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Notification;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Repositories.NotificationRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;

import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public void createNotification(Utilisateur user,
                                   @Nullable Department department,
                                   String message,
                                   NotificationType type,
                                   @Nullable Vote vote) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setDepartment(department);
        notification.setMessage(message);
        notification.setType(type);
        notification.setVote(vote);
        notificationRepository.save(notification);
    }

    @Override
    public void createNewDepartmentNotification(Department department, String message) {
        List<Utilisateur> enseignants = utilisateurRepository.findByAppRoles_RoleName("ENSEIGNANT");
        for (Utilisateur enseignant : enseignants) {
            if (departmentMatchesSkills(department, enseignant)) {
                createNotification(enseignant, department, message, NotificationType.NEW_DEPARTMENT, null);
            }
        }
    }

    public boolean departmentMatchesSkills(Department department, Utilisateur enseignant) {
        if (enseignant.getSkills() == null || enseignant.getSkills().trim().isEmpty()) {
            return false;
        }
        if (department.getDescription() == null || department.getDescription().trim().isEmpty()) {
            return false;
        }
        String[] skills = enseignant.getSkills().toLowerCase().split("\\s*,\\s*");
        String departmentDescription = department.getDescription().toLowerCase();
        return Arrays.stream(skills)
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .anyMatch(departmentDescription::contains);
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
}