package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Candidate;

import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    @Query("SELECT c FROM Candidate c JOIN FETCH c.user JOIN FETCH c.vote v JOIN FETCH v.department")
    List<Candidate> findAllWithVoteAndUser();
    List<Candidate> findByVote(Vote vote);
    boolean existsByVoteAndUser(Vote vote, Utilisateur user);
}