package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Security.Entities.Utilisateur;

public interface VoteService {
    boolean isCandidateInVote(Long voteId, Utilisateur user);
}
