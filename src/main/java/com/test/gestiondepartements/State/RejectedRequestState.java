package com.test.gestiondepartements.State;

import com.test.gestiondepartements.Entities.ModuleRequest;
import com.test.gestiondepartements.Entities.ModuleRequestStatus;
import com.test.gestiondepartements.Repositories.ModuleRequestRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RejectedRequestState implements RequestState {

    private final ModuleRequestRepository moduleRequestRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    public RejectedRequestState(ModuleRequestRepository moduleRequestRepository, UtilisateurRepository utilisateurRepository) {
        this.moduleRequestRepository = moduleRequestRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public void submit(ModuleRequest request) {
        throw new IllegalStateException("Cannot resubmit a rejected request.");
    }

    @Override
    public void approve(ModuleRequest request) {
        throw new IllegalStateException("Cannot approve a rejected request.");
    }

    @Override
    public void reject(ModuleRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur currentUser = utilisateurRepository.findByUsername(username);
        request.setStatus(ModuleRequestStatus.REJECTED);
        request.setRejectedAt(LocalDateTime.now());
        request.setRejectedBy(currentUser);
        moduleRequestRepository.save(request);
    }

    @Override
    public void cancel(ModuleRequest request) {
        throw new IllegalStateException("A rejected request cannot be canceled.");
    }

    @Override
    public String getStatus() {
        return "Rejected";
    }
}