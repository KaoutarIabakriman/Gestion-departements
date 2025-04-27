package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Security.Entities.Utilisateur;

import java.util.List;

public interface DepartmentService {
    Department createDepartment(DepartmentDTO departmentDTO);
    List<Department> getAllDepartments();
    Department updateDepartment(Long id, DepartmentDTO departmentDTO);
    void addUserToDepartment(Long departmentId, Utilisateur user);
    boolean departmentMatchesSkills(Department department, Utilisateur user);

}