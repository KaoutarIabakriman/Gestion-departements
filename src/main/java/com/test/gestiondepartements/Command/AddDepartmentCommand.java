package com.test.gestiondepartements.Command;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.History;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;


@RequiredArgsConstructor
public class AddDepartmentCommand implements Command {
    private final Department department;
    private final DepartmentRepository departmentRepository;
    private final HistoryRepository historyRepository;
    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;

    public AddDepartmentCommand(Department department, DepartmentRepository departmentRepository, Department department1, DepartmentRepository departmentRepository1, HistoryRepository historyRepository, NotificationService notificationService, UtilisateurRepository utilisateurRepository) {
        this.department = department1;
        this.departmentRepository = departmentRepository1;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    @Transactional
    public History execute() {
        Department savedDepartment = departmentRepository.save(department);


        History history = new History();
        history.setAction("ADD_DEPARTMENT");
        history.setEntityType("DEPARTMENT");
        history.setEntityId(department.getId());
        history.setDetails("Département créé: " + savedDepartment.getName());
        historyRepository.save(history);

        // Notification logic
        List<Utilisateur> enseignants = utilisateurRepository.findByAppRoles_RoleName("ENSEIGNANT");
        for (Utilisateur enseignant : enseignants) {
            String skills = enseignant.getSkills();
            if (skills == null || skills.isEmpty()) continue;

            String[] skillList = skills.split("\\s*,\\s*");
            boolean match = Arrays.stream(skillList)
                    .anyMatch(skill -> department.getDescription().toLowerCase().contains(skill.toLowerCase()));

            if (match) {
                String message = "Nouveau département '" + department.getName() + "' correspond à vos compétences.";
                notificationService.createNotification(enseignant, department, message);
            }
        }

        return history;
    }

    @Override
    public void undo() {

    }
}