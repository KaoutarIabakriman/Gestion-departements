package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Entities.VoteChoice;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoteChoiceRepository extends JpaRepository<VoteChoice, Long> {
    Optional<VoteChoice> findByVoteAndVoter(Vote vote, Utilisateur voter);
}