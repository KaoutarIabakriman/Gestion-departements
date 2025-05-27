
package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Module; // Import Module
import com.test.gestiondepartements.Entities.Notification;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Repositories.NotificationRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;

import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional
    public void createNotification(Utilisateur user, @Nullable Department department, @Nullable Module module, String message, NotificationType type, @Nullable Vote vote) {
        Notification notification = new Notification();
        notification.setUser(user);

        if (module != null && module.getDepartment() != null) {
            notification.setDepartment(module.getDepartment());
        } else {
            notification.setDepartment(department);
        }
        notification.setModule(module);
        notification.setMessage(message);
        notification.setType(type);
        notification.setVote(vote);
        notification.setReadStatus(false);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createNewDepartmentNotification(Department department, String message) {
        List<Utilisateur> enseignants = utilisateurRepository.findByAppRoles_RoleName("ENSEIGNANT");
        for (Utilisateur enseignant : enseignants) {
            if (this.skillsMatchDescription(enseignant.getSkills(), department.getDescription())) {
                createNotification(enseignant, department, null, message + " (Département : " + department.getName() + ")", NotificationType.NEW_DEPARTMENT, null);
            }
        }
    }

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
    @Transactional
    public void createGeneralNotification(List<Utilisateur> users, String message, @Nullable Vote vote, @Nullable Module module) {
        for (Utilisateur user : users) {
            Department deptForNotification = null;
            if (module != null && module.getDepartment() != null) {
                deptForNotification = module.getDepartment();
            } else if (vote != null && vote.getDepartment() != null) {
                deptForNotification = vote.getDepartment();
            }
            createNotification(user, deptForNotification, module, message, NotificationType.GENERAL, vote);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Utilisateur user) {
        return notificationRepository.findByUserAndReadStatusFalseOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (!notification.isReadStatus()) {
                notification.setReadStatus(true);
                notificationRepository.save(notification);
            }
        });
    }
}