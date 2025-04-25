package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Repositories.DepartmentRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    Department createDepartment(DepartmentDTO departmentDTO);
    List<Department> getAllDepartments();
    Department updateDepartment(Long id, DepartmentDTO departmentDTO);

}