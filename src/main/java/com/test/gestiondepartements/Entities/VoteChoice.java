package com.test.gestiondepartements.Entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "vote_choices")
@Data
public class VoteChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @ManyToOne
    @JoinColumn(name = "voter_id", nullable = false)
    private Utilisateur voter;

    @ManyToOne
    @JoinColumn(name = "chosen_candidate_id", nullable = false)
    private Utilisateur chosenCandidate;

    @CreationTimestamp
    private LocalDateTime votedAt;

}