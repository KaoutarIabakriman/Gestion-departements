package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Repositories.VoteRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;

    @Override
    public boolean isCandidateInVote(Long voteId, Utilisateur user) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new RuntimeException("Vote not found"));
        return vote.getCandidates().stream().anyMatch(c -> c.getUser().equals(user));
    }
}