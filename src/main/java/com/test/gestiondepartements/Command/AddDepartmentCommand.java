package com.test.gestiondepartements.Command;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.History;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;
import com.test.gestiondepartements.Service.NotificationService;

import java.time.LocalDateTime;

public class AddDepartmentCommand implements Command {
    private final Department department;
    private final DepartmentRepository departmentRepository;
    private final HistoryRepository historyRepository;
    private final NotificationService notificationService;

    public AddDepartmentCommand(Department department,
                                DepartmentRepository departmentRepository,
                                HistoryRepository historyRepository,
                                NotificationService notificationService) {
        this.department = department;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
    }

    @Override
    public History execute() {

        Department savedDepartment = departmentRepository.save(department);

        History historyEntry = new History();
        historyEntry.setAction("CREATE");
        historyEntry.setEntityType("Department");
        historyEntry.setEntityId(savedDepartment.getId());
        historyEntry.setDetails("Département '" + savedDepartment.getName() + "' créé.");
        historyEntry.setCreatedAt(LocalDateTime.now());
        historyRepository.save(historyEntry);
        notificationService.createNewDepartmentNotification(
                savedDepartment,
                "Un nouveau département '" + savedDepartment.getName() + "' a été créé."
        );

        return historyEntry;
    }
}