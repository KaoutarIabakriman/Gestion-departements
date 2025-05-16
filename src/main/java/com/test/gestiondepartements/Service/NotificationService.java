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
public interface NotificationService {

    public default void createNewDepartmentNotification(Department department, String message) {

    }

    private boolean departmentMatchesSkills(Department department, Utilisateur enseignant) {

        return false;
    }
    public default void createNotification(Utilisateur user, Department department, String message) {

    }
    public default List<Notification> getUnreadNotifications(Utilisateur user) {
        return List.of();
    }

    public default void markAsRead(Long notificationId) {

    }
}
