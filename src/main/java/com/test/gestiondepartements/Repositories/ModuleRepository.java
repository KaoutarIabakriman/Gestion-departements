package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    // ... méthodes spécifiques si nécessaire
}