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
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
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
        if (user.getSkills() == null || department.getDescription() == null) return false;

        String[] userSkills = user.getSkills().split("\\s*,\\s*");
        String departmentDescription = department.getDescription().toLowerCase();

        return Arrays.stream(userSkills)
                .anyMatch(skill -> departmentDescription.contains(skill.toLowerCase()));
    }


}