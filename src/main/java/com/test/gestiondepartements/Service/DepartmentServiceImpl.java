package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public Department createDepartment(DepartmentDTO departmentDTO) {
        Department department = new Department();
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department.setHeadOfDepartment(departmentDTO.getHeadOfDepartment());
        return departmentRepository.save(department);
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Department updateDepartment(Long id, DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));

        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department.setHeadOfDepartment(departmentDTO.getHeadOfDepartment());

        return departmentRepository.save(department);
    }

    @Override
    public void addUserToDepartment(Long departmentId, Utilisateur user) {
        if (user.isInAnyDepartment()) {
            throw new IllegalStateException("L'utilisateur est déjà membre d'un département");
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));

        user.getDepartments().add(department);
        utilisateurRepository.save(user);
    }
    @Override
    public boolean departmentMatchesSkills(Department department, Utilisateur user) {
        if (user.getSkills() == null || user.getSkills().trim().isEmpty()) return false;
        if (department.getDescription() == null || department.getDescription().trim().isEmpty()) return false;

        String[] skills = user.getSkills().split("\\s*,\\s*");
        String description = department.getDescription().toLowerCase();

        return Arrays.stream(skills)
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .anyMatch(skill -> description.contains(skill.toLowerCase()));
    }


}