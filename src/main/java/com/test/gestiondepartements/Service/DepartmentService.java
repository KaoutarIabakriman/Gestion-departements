package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;

import java.util.List;

public interface DepartmentService {
    Department createDepartment(DepartmentDTO departmentDTO);
    List<Department> getAllDepartments();
    Department updateDepartment(Long id, DepartmentDTO departmentDTO);

}