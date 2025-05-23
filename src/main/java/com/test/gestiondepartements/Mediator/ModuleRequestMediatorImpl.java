package com.test.gestiondepartements.Mediator;

import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Entities.ModuleRequest;
import com.test.gestiondepartements.Entities.ModuleRequestStatus;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Repositories.ModuleRequestRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ModuleRequestMediatorImpl implements ModuleRequestMediator {

    private final ModuleRequestRepository moduleRequestRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void submitRequest(Module module, Utilisateur enseignant) {
        if (moduleRequestRepository.findByModuleAndEnseignantAndStatus(module, enseignant, ModuleRequestStatus.PENDING).isPresent()) {
            throw new IllegalStateException("Vous avez déjà une demande en attente pour ce module.");
        }
        if (moduleRequestRepository.findByModuleAndEnseignantAndStatus(module, enseignant, ModuleRequestStatus.APPROVED).isPresent()){
            throw new IllegalStateException("Vous êtes déjà approuvé pour enseigner ce module.");
        }

        ModuleRequest request = new ModuleRequest(module, enseignant);
        moduleRequestRepository.save(request);

        notificationService.createNotification(
                enseignant,
                module.getDepartment(),
                module,
                "Votre demande pour enseigner le module '" + module.getName() + "' a été soumise.",
                NotificationType.MODULE_REQUEST_SUBMITTED,
                null
        );

        Utilisateur hod = module.getDepartment().getHeadOfDepartment();
        if (hod != null) {
            notificationService.createNotification(
                    hod,
                    module.getDepartment(),
                    module,
                    "L'enseignant " + enseignant.getFirstName() + " " + enseignant.getLastName() +
                            " a demandé à enseigner le module '" + module.getName() + "'.",
                    NotificationType.MODULE_REQUEST_PENDING_HOD,
                    null
            );
        }
    }
}