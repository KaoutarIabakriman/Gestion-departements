package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.CandidateVoteDetailsDTO;

import java.util.List;

public interface CandidateService {
    List<CandidateVoteDetailsDTO> getAllCandidaciesWithVoteDetails();
}