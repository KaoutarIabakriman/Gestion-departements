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
public class ApprovedRequestState implements RequestState {

    private final ModuleRequestRepository moduleRequestRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    public ApprovedRequestState(ModuleRequestRepository moduleRequestRepository, UtilisateurRepository utilisateurRepository) {
        this.moduleRequestRepository = moduleRequestRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public void submit(ModuleRequest request) {
        throw new IllegalStateException("Cannot submit an approved request.");
    }

    @Override
    public void approve(ModuleRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur currentUser = utilisateurRepository.findByUsername(username);
        request.setStatus(ModuleRequestStatus.APPROVED);
        request.setApprovedAt(LocalDateTime.now());
        request.setApprovedBy(currentUser);
        moduleRequestRepository.save(request);
    }

    @Override
    public void reject(ModuleRequest request) {
        throw new IllegalStateException("Cannot reject an approved request.");

    }

    @Override
    public void cancel(ModuleRequest request) {
        throw new IllegalStateException("Cannot cancel an approved request.");
    }

    @Override
    public String getStatus() {
        return "Approved";
    }
}