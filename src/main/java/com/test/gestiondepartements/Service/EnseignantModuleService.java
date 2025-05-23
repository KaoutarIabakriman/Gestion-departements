package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Mediator.ModuleRequestMediator;
import com.test.gestiondepartements.Repositories.ModuleRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnseignantModuleService {
    private final ModuleRepository moduleRepository;
    private final ModuleRequestMediator moduleRequestMediator;

    public void requestModule(Long moduleId, Utilisateur enseignant) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec ID: " + moduleId));

        if (module.getDepartment() == null) {
            throw new RuntimeException("Le module demandé n'est assigné à aucun département.");
        }

        moduleRequestMediator.submitRequest(module, enseignant);
    }
}