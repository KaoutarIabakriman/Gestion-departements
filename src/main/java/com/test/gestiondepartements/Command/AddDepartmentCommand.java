package com.test.gestiondepartements.Command;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.History;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;

public class AddDepartmentCommand implements Command {
    private final Department department;
    private final DepartmentRepository departmentRepository;
    private final HistoryRepository historyRepository;

    public AddDepartmentCommand(Department department,
                                DepartmentRepository departmentRepository,
                                HistoryRepository historyRepository) {
        this.department = department;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public History execute() {
        Department savedDept = departmentRepository.save(department);

        History history = new History();
        history.setAction("DEPARTMENT_ADDED");
        history.setEntityType("Department");
        history.setEntityId(savedDept.getId());
        history.setDetails("Added department: " + savedDept.getName());

        return historyRepository.save(history);
    }

    @Override
    public void undo() {
        departmentRepository.delete(department);
    }
}