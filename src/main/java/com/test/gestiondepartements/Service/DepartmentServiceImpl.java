package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;


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


}