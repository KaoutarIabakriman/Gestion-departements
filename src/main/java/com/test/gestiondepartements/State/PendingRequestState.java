package com.test.gestiondepartements.State;

import com.test.gestiondepartements.Entities.ModuleRequest;
import com.test.gestiondepartements.Entities.ModuleRequestStatus;
import com.test.gestiondepartements.Repositories.ModuleRequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PendingRequestState implements RequestState {

    private final ModuleRequestRepository moduleRequestRepository;

    @Autowired
    public PendingRequestState(ModuleRequestRepository moduleRequestRepository) {
        this.moduleRequestRepository = moduleRequestRepository;
    }

    @Override
    public void submit(ModuleRequest request) {
        request.setStatus(ModuleRequestStatus.PENDING);
        moduleRequestRepository.save(request);
    }

    @Override
    public void approve(ModuleRequest request) {
        request.setStatus(ModuleRequestStatus.APPROVED);
        moduleRequestRepository.save(request);
    }

    @Override
    public void reject(ModuleRequest request) {
        request.setStatus(ModuleRequestStatus.REJECTED);
        moduleRequestRepository.save(request);
    }

    @Override
    public void cancel(ModuleRequest request) {
        request.setStatus(ModuleRequestStatus.CANCELLED);
        moduleRequestRepository.save(request);
    }

    @Override
    public String getStatus() {
        return "Pending";
    }
}