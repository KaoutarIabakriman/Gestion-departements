package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Security.Entities.Utilisateur;

import org.springframework.data.jpa.repository.JpaRepository;


public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Department findByHeadOfDepartment(Utilisateur chef);
}