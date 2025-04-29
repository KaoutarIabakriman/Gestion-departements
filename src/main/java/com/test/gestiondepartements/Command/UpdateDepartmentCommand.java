package com.test.gestiondepartements.Command;

import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.History;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;

public class UpdateDepartmentCommand implements Command {

    private final DepartmentDTO departmentDTO;
    private final DepartmentRepository departmentRepository;
    private final HistoryRepository historyRepository;
    private Department originalDepartment;

    public UpdateDepartmentCommand(DepartmentDTO departmentDTO,
                                   DepartmentRepository departmentRepository,
                                   HistoryRepository historyRepository) {
        this.departmentDTO = departmentDTO;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public History execute() {
        originalDepartment = departmentRepository.findById(departmentDTO.getId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Department updatedDept = new Department();
        updatedDept.setId(departmentDTO.getId());
        updatedDept.setName(departmentDTO.getName());
        updatedDept.setDescription(departmentDTO.getDescription());
        updatedDept.setHeadOfDepartment(departmentDTO.getHeadOfDepartment());

        departmentRepository.save(updatedDept);

        History history = new History();
        history.setAction("DEPARTMENT_UPDATED");
        history.setEntityType("DEPARTMENT");
        history.setEntityId(updatedDept.getId());
        history.setDetails(String.format(
                "Updated department from: %s/%s to: %s/%s",
                originalDepartment.getName(),
                originalDepartment.getDescription(),
                updatedDept.getName(),
                updatedDept.getDescription()
        ));

        return historyRepository.save(history);
    }

    @Override
    public void undo() {
        if (originalDepartment != null) {
            departmentRepository.save(originalDepartment);
        }
    }
}