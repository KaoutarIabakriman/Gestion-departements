package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Notification;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Security.Entities.Utilisateur;

import io.micrometer.common.lang.Nullable;
import java.util.List;

public interface NotificationService {

    void createNotification(Utilisateur user,
                            @Nullable Department department,
                            String message,
                            NotificationType type,
                            @Nullable Vote vote);

    List<Notification> getUnreadNotifications(Utilisateur user);

    void markAsRead(Long notificationId);

    void createNewDepartmentNotification(Department department, String message);
}