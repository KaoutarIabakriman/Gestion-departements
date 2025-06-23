package com.test.gestiondepartements.Command;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.History;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;
import com.test.gestiondepartements.Repositories.ModuleRepository;

public class UpdateModuleCommand implements Command {

    private final Long moduleIdToUpdate;
    private final Module updatedModuleData;
    private final Long newDepartmentId;

    private final ModuleRepository moduleRepository;
    private final DepartmentRepository departmentRepository;
    private final HistoryRepository historyRepository;

    public UpdateModuleCommand(Long moduleIdToUpdate, Module updatedModuleData, Long newDepartmentId,
                               ModuleRepository moduleRepository,
                               DepartmentRepository departmentRepository,
                               HistoryRepository historyRepository) {
        this.moduleIdToUpdate = moduleIdToUpdate;
        this.updatedModuleData = updatedModuleData;
        this.newDepartmentId = newDepartmentId;
        this.moduleRepository = moduleRepository;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public History execute() {
        Module moduleToUpdate = moduleRepository.findById(moduleIdToUpdate)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec ID: " + moduleIdToUpdate + " pour la mise à jour."));

        Department department = departmentRepository.findById(newDepartmentId)
                .orElseThrow(() -> new RuntimeException("Nouveau département non trouvé avec ID: " + newDepartmentId + " pour la mise à jour du module."));

        String oldName = moduleToUpdate.getName();

        moduleToUpdate.setName(updatedModuleData.getName());
        moduleToUpdate.setDescription(updatedModuleData.getDescription());
        moduleToUpdate.setWorkload(updatedModuleData.getWorkload());
        moduleToUpdate.setDepartment(department);

        Module savedModule = moduleRepository.save(moduleToUpdate);

        createHistoryEntry(savedModule, oldName);
        return null;
    }

    private void createHistoryEntry(Module module, String oldName) {
        History history = new History();
        history.setAction("UPDATE_MODULE");
        history.setEntityType("Module");
        history.setEntityId(module.getId());
        history.setDetails("Module '" + oldName + "' (ID: " + module.getId() + ") mis à jour. Nouveau nom: '" + module.getName() + "'.");
        historyRepository.save(history);
    }
}