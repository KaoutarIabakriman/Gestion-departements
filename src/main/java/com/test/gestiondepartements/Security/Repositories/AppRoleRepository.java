package com.test.gestiondepartements.Security.Repositories;
import com.test.gestiondepartements.Security.Entities.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    AppRole findByRoleName(String roleName);

}
