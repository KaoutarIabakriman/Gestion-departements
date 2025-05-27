package com.test.gestiondepartements.Mediator;

import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Entities.ModuleRequest;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Repositories.ModuleRequestRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Service.NotificationService;

import com.test.gestiondepartements.State.RequestState;
import com.test.gestiondepartements.State.RequestStateFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ModuleRequestMediatorImpl implements ModuleRequestMediator {

    private final ModuleRequestRepository requestRepository;
    private final NotificationService notificationService;
    private final RequestStateFactory requestStateFactory;


    @Transactional
    public void submitRequest(Module module, Utilisateur enseignant) {
        if (requestRepository.existsByModuleAndEnseignantAndStatus(module, enseignant, com.test.gestiondepartements.Entities.ModuleRequestStatus.PENDING)) {
            throw new IllegalStateException("Vous avez déjà une demande en attente pour ce module.");
        }

        if (module.getEnseignants().contains(enseignant)) {
            throw new IllegalStateException("Vous enseignez déjà ce module.");
        }

        ModuleRequest request = new ModuleRequest(module, enseignant);
        RequestState state = requestStateFactory.getState(request);
        state.submit(request);




        notificationService.createNotification(
                enseignant,
                module.getDepartment(),
                module,
                "Votre demande pour enseigner le module '" + module.getName() + "' a été soumise.",
                NotificationType.MODULE_REQUEST_SUBMITTED,
                null
        );


        List<Utilisateur> chefsDepartement = Collections.singletonList(module.getDepartment().getHeadOfDepartment());


        notificationService.createNotification(
                chefsDepartement.get(0),
                module.getDepartment(),
                module,
                "L'enseignant(e) "+enseignant.getFirstName() +" "+ enseignant.getLastName() +" a soumis une demande pour enseigner le module '" + module.getName() + "'.",
                NotificationType.MODULE_REQUEST_PENDING_HOD,
                null
        );


    }





}