package com.test.gestiondepartements.State;

import com.test.gestiondepartements.Entities.ModuleRequest;
import com.test.gestiondepartements.Repositories.ModuleRequestRepository;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestStateFactory {

    private final ModuleRequestRepository moduleRequestRepository;
    private final UtilisateurRepository utilisateurRepository;

    public RequestState getState(ModuleRequest request) {
        switch (request.getStatus()) {
            case PENDING:
                return new PendingRequestState(moduleRequestRepository);
            case APPROVED:
                return new ApprovedRequestState(moduleRequestRepository, utilisateurRepository);
            case REJECTED:
                return new RejectedRequestState(moduleRequestRepository, utilisateurRepository);
            case CANCELLED:
                return new CancelledRequestState(moduleRequestRepository, utilisateurRepository);
            default:
                throw new IllegalArgumentException("Invalid request status: " + request.getStatus());
        }
    }
}