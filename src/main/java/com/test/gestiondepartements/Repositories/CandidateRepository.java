package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Candidate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

}