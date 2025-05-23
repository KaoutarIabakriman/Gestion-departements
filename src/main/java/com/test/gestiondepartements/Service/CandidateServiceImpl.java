package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.CandidateVoteDetailsDTO;
import com.test.gestiondepartements.Entities.Candidate;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Entities.VoteChoice;
import com.test.gestiondepartements.Entities.VoteStatus;
import com.test.gestiondepartements.Repositories.CandidateRepository;
import com.test.gestiondepartements.Repositories.VoteChoiceRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final VoteChoiceRepository voteChoiceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CandidateVoteDetailsDTO> getAllCandidaciesWithVoteDetails() {
        List<Candidate> candidates = candidateRepository.findAllWithVoteAndUser();
        List<VoteChoice> allVoteChoices = voteChoiceRepository.findAllWithVoterAndChosenCandidate();

        Map<Long, Long> voteCounts = allVoteChoices.stream()
                .filter(vc -> vc.getChosenCandidate() != null)
                .collect(Collectors.groupingBy(vc -> vc.getChosenCandidate().getId(), Collectors.counting()));

        Map<Long, List<String>> votersByCandidateUser = allVoteChoices.stream()
                .filter(vc -> vc.getChosenCandidate() != null && vc.getVoter() != null)
                .collect(Collectors.groupingBy(
                        vc -> vc.getChosenCandidate().getId(),
                        Collectors.mapping(vc -> vc.getVoter().getUsername(), Collectors.toList())
                ));

        return candidates.stream()
                .map(candidateEntity -> {
                    Vote vote = candidateEntity.getVote();
                    Utilisateur candidateUser = candidateEntity.getUser();

                    if (vote == null || candidateUser == null || vote.getDepartment() == null) {
                        return new CandidateVoteDetailsDTO(
                                candidateEntity.getId(),
                                null,
                                null,
                                "N/A",
                                "N/A",
                                "N/A",
                                null,
                                candidateEntity.getDeclaredAt(),
                                0,
                                Collections.emptyList(),
                                null
                        );
                    }

                    String departmentName = vote.getDepartment().getName();
                    Long departmentId = vote.getDepartment().getId();
                    String candidateFullName = (candidateUser.getFirstName() != null ? candidateUser.getFirstName() : "") + " " +
                            (candidateUser.getLastName() != null ? candidateUser.getLastName() : "");
                    candidateFullName = candidateFullName.trim().isEmpty() ? "N/A" : candidateFullName.trim();

                    String candidateUsername = candidateUser.getUsername();
                    Long candidateUserId = candidateUser.getId();
                    long currentVoteCount = voteCounts.getOrDefault(candidateUserId, 0L);
                    List<String> currentVoters = votersByCandidateUser.getOrDefault(candidateUserId, Collections.emptyList());
                    VoteStatus currentVoteStatus = vote.getStatus();

                    return new CandidateVoteDetailsDTO(
                            candidateEntity.getId(),
                            vote.getId(),
                            departmentId,
                            departmentName,
                            candidateFullName,
                            candidateUsername,
                            candidateUserId,
                            candidateEntity.getDeclaredAt(),
                            currentVoteCount,
                            currentVoters,
                            currentVoteStatus
                    );
                })
                .collect(Collectors.toList());
    }

}