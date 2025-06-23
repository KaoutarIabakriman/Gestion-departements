package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Entities.VoteChoice;
import com.test.gestiondepartements.Security.Entities.Utilisateur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VoteChoiceRepository extends JpaRepository<VoteChoice, Long> {

    @Query("SELECT vc FROM VoteChoice vc JOIN FETCH vc.voter JOIN FETCH vc.chosenCandidate")
    List<VoteChoice> findAllWithVoterAndChosenCandidate();
    boolean existsByVoteAndVoter(Vote vote, Utilisateur voter);
}