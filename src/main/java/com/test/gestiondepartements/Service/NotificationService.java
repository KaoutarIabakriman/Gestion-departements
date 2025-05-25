package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Notification;
import com.test.gestiondepartements.Security.Entities.Utilisateur;


import java.util.Arrays;
import java.util.List;


public interface NotificationService {
    void createNewDepartmentNotification(Department department, String message);
    boolean departmentMatchesSkills(Department department, Utilisateur enseignant);
    void createNotification(Utilisateur user, Department department, String message) ;
    List<Notification> getUnreadNotifications(Utilisateur user);
    void markAsRead(Long notificationId);

    void sendVoteNotification(Department department, String s);
}