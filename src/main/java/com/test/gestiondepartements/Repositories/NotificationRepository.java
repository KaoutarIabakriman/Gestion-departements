package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Notification;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN FETCH n.user u " +
            "LEFT JOIN FETCH n.department d " +
            "LEFT JOIN FETCH n.module m " +
            "LEFT JOIN FETCH n.vote v " +
            "WHERE n.user = :user AND n.readStatus = false ORDER BY n.createdAt DESC")
    List<Notification> findByUserAndReadStatusFalseOrderByCreatedAtDesc(@Param("user") Utilisateur user);

    Page<Notification> findByUser(Utilisateur user, Pageable pageable);
    long countByUserAndReadStatusFalse(Utilisateur user);

}