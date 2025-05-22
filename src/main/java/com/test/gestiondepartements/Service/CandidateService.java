package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.CandidateVoteDetailsDTO;
import com.test.gestiondepartements.Entities.Candidate;
import java.util.List;

public interface CandidateService {
    List<CandidateVoteDetailsDTO> getAllCandidaciesWithVoteDetails();
    void deleteCandidacy(Long candidateId);
    List<Candidate> getCandidatesForVote(Long voteId);
}