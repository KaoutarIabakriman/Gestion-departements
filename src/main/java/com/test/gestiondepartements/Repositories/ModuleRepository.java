package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Module;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByDepartment(Department department);
}