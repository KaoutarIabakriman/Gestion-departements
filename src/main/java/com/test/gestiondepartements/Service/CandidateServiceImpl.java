// CandidateServiceImpl.java
package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.CandidateVoteDetailsDTO;
import com.test.gestiondepartements.Entities.Candidate;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Entities.VoteChoice;
import com.test.gestiondepartements.Repositories.CandidateRepository;
import com.test.gestiondepartements.Repositories.VoteChoiceRepository;
import com.test.gestiondepartements.Repositories.VoteRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// CandidateServiceImpl.java
@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final VoteChoiceRepository voteChoiceRepository;
    private final VoteRepository voteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CandidateVoteDetailsDTO> getAllCandidaciesWithVoteDetails() {
        List<Candidate> candidates = candidateRepository.findAll();
        List<VoteChoice> voteChoices = voteChoiceRepository.findAll();

        Map<Utilisateur, Candidate> candidateCache = new HashMap<>();

        Map<Candidate, List<String>> votersByCandidate = voteChoices.stream()
                .collect(Collectors.groupingBy(
                        vc -> {
                            Candidate c = candidateCache.computeIfAbsent(
                                    vc.getChosenCandidate(),
                                    user -> candidateRepository.findByUser(user)
                            );
                            if (c == null) {
                                throw new RuntimeException("Candidate not found for user: " + vc.getChosenCandidate().getUsername());
                            }
                            return c;

                        },
                        Collectors.mapping(vc -> vc.getVoter().getUsername(), Collectors.toList())


                ));


        return candidates.stream()
                .map(candidate -> {
                    Vote vote = candidate.getVote();
                    Utilisateur candidateUser = candidate.getUser();
                    String departmentName = (vote != null && vote.getDepartment() != null) ? vote.getDepartment().getName() : "N/A";
                    String candidateFullName = (candidateUser != null) ? (candidateUser.getFirstName() + " " + candidateUser.getLastName()) : "N/A";
                    String candidateUsername = (candidateUser != null) ? candidateUser.getUsername() : "N/A";
                    Long voteId = (vote != null) ? vote.getId() : null;

                    List<String> voters = votersByCandidate.get(candidate);
                    int voteCount = (voters != null) ? voters.size() : 0;

                    return new CandidateVoteDetailsDTO(
                            candidate.getId(), voteId, departmentName, candidateFullName, candidateUsername,
                            candidate.getDeclaredAt(), voteCount, voters
                    );
                })
                .toList();
    }


    @Override
    @Transactional
    public void deleteCandidacy(Long candidateId) {
        if (!candidateRepository.existsById(candidateId)) {
            throw new RuntimeException("Candidacy not found with ID: " + candidateId);
        }
        candidateRepository.deleteById(candidateId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Candidate> getCandidatesForVote(Long voteId) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote not found with ID: " + voteId));
        return vote.getCandidates();
    }
}