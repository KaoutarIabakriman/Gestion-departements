package com.test.gestiondepartements.Command;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.History;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;
import com.test.gestiondepartements.Repositories.ModuleRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Service.NotificationService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AddModuleCommand implements Command {

    private final Module moduleDataFromForm;
    private final Long departmentId;
    private final ModuleRepository moduleRepository;
    private final DepartmentRepository departmentRepository;
    private final HistoryRepository historyRepository;
    private final NotificationService notificationService;

    public AddModuleCommand(Module moduleDataFromForm, Long departmentId,
                            ModuleRepository moduleRepository, DepartmentRepository departmentRepository,
                            HistoryRepository historyRepository, NotificationService notificationService) {
        this.moduleDataFromForm = moduleDataFromForm;
        this.departmentId = departmentId;
        this.moduleRepository = moduleRepository;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public History execute() {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Département non trouvé avec ID: " + departmentId));

        Module newModule = new Module();
        newModule.setName(moduleDataFromForm.getName());
        newModule.setDescription(moduleDataFromForm.getDescription());
        newModule.setWorkload(moduleDataFromForm.getWorkload());
        newModule.setDepartment(department);

        Module savedModule = moduleRepository.save(newModule);

        History historyEntry = new History();
        historyEntry.setAction("CREATE");
        historyEntry.setEntityType("Module");
        historyEntry.setEntityId(savedModule.getId());
        historyEntry.setDetails("Module '" + savedModule.getName() + "' créé et assigné au département '" + department.getName() + "'.");
        historyEntry.setCreatedAt(LocalDateTime.now());
        historyRepository.save(historyEntry);

        List<Utilisateur> enseignantsInDepartment = department.getMembers().stream()
                .filter(member -> member.getAppRoles().stream().anyMatch(role -> "ENSEIGNANT".equals(role.getRoleName())))
                .collect(Collectors.toList());

        for (Utilisateur enseignant : enseignantsInDepartment) {

            notificationService.createNotification(
                    enseignant,
                    department,
                    savedModule,
                    "Un nouveau module '" + savedModule.getName() + "' (" + savedModule.getWorkload() + "h) a été ajouté au département '" + department.getName() + "'. Vous pouvez demander à l'enseigner.",
                    NotificationType.NEW_MODULE_ADDED,
                    null
            );
        }
        return historyEntry;
    }
}