package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Notification;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndReadStatusFalseOrderByCreatedAtDesc(Utilisateur user);
}