package com.test.gestiondepartements.State;

import com.test.gestiondepartements.Entities.ModuleRequest;

public interface RequestState {
    void submit(ModuleRequest request);
    void approve(ModuleRequest request);
    void reject(ModuleRequest request);
    void cancel(ModuleRequest request);
    String getStatus();
}