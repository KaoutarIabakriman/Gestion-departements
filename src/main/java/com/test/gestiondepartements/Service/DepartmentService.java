package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Security.Entities.Utilisateur;

import java.util.List;

public interface DepartmentService {
    List<Department> getAllDepartments();
    void addUserToDepartment(Long departmentId, Utilisateur user);
    boolean departmentMatchesSkills(Department department, Utilisateur user);

}