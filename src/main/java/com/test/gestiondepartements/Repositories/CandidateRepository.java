// CandidateRepository.java
package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Candidate;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    @Query("SELECT c FROM Candidate c WHERE c.user = :user") // Corrected query
    Candidate findByUser(Utilisateur user);


    @Query("SELECT c FROM Candidate c JOIN FETCH c.user u JOIN FETCH c.vote v JOIN FETCH v.department d")
    List<Candidate> findAllWithDetails();




}