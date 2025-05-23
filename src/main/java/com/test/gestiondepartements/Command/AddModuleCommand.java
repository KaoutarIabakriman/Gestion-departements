package com.test.gestiondepartements.Command;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.History;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;
import com.test.gestiondepartements.Repositories.ModuleRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.NotificationService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddModuleCommand implements Command {

    private final Module moduleData;
    private final Long departmentId;

    private final ModuleRepository moduleRepository;
    private final DepartmentRepository departmentRepository;
    private final HistoryRepository historyRepository;
    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;

    public AddModuleCommand(Module moduleData, Long departmentId,
                            ModuleRepository moduleRepository,
                            DepartmentRepository departmentRepository,
                            HistoryRepository historyRepository,
                            NotificationService notificationService,
                            UtilisateurRepository utilisateurRepository) {
        this.moduleData = moduleData;
        this.departmentId = departmentId;
        this.moduleRepository = moduleRepository;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public History execute() {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Département non trouvé avec ID: " + departmentId + " pour l'ajout du module."));

        moduleData.setDepartment(department);
        Module savedModule = moduleRepository.save(moduleData);

        createHistoryEntry(savedModule, department);
        notifyTeachersAboutNewModule(savedModule, department);
        return null;
    }

    private void createHistoryEntry(Module module, Department department) {
        History history = new History();
        history.setAction("CREATE_MODULE");
        history.setEntityType("Module");
        history.setEntityId(module.getId());
        history.setDetails("Module '" + module.getName() + "' créé dans le département '" + department.getName() + "'.");
        historyRepository.save(history);
    }

    private void notifyTeachersAboutNewModule(Module module, Department department) {
        String messageNotificationAjoutBase = "Un nouveau module '" + module.getName() +
                "' a été ajouté au département '" + department.getName() + "'.";

        List<Utilisateur> enseignantsDuDepartement = department.getMembers().stream()
                .filter(membre -> membre.getAppRoles().stream().anyMatch(role -> "ENSEIGNANT".equals(role.getRoleName())))
                .collect(Collectors.toList());

        for (Utilisateur enseignant : enseignantsDuDepartement) {
            boolean matchesSkills = false;
            if (enseignant.getSkills() != null && !enseignant.getSkills().trim().isEmpty() &&
                    module.getDescription() != null && !module.getDescription().trim().isEmpty()) {
                String[] userSkillsArray = enseignant.getSkills().toLowerCase().split("\\s*,\\s*");
                String moduleDescription = module.getDescription().toLowerCase();
                matchesSkills = Arrays.stream(userSkillsArray)
                        .map(String::trim)
                        .filter(skill -> !skill.isEmpty())
                        .anyMatch(moduleDescription::contains);
            }

            String finalMessage = messageNotificationAjoutBase;
            if (matchesSkills) {
                finalMessage += " Il pourrait correspondre à vos compétences.";
            }

            notificationService.createNotification(
                    enseignant,
                    department,
                    finalMessage,
                    NotificationType.NEW_MODULE_ADDED,
                    null
            );
        }
    }
}