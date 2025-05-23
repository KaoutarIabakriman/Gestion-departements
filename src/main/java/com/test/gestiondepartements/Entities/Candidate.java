package com.test.gestiondepartements.Entities;

import com.test.gestiondepartements.Security.Entities.Utilisateur;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "vote_candidates")
@Data
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur user;

    private LocalDateTime declaredAt;

}