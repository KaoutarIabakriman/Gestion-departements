package com.test.gestiondepartements.Mediator;

import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Security.Entities.Utilisateur;

public interface ModuleRequestMediator {
    void submitRequest(Module module, Utilisateur enseignant);
}