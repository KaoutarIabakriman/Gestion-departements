// VoteChoice.java
package com.test.gestiondepartements.Entities;

import com.test.gestiondepartements.Repositories.CandidateRepository;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import jakarta.persistence.*;
import java.time.LocalDateTime;


// VoteChoice.java
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