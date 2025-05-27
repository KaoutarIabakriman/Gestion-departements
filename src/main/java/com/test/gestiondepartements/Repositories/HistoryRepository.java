package com.test.gestiondepartements.Repositories;

import com.test.gestiondepartements.Entities.History;

import org.springframework.data.jpa.repository.JpaRepository;


public interface HistoryRepository extends JpaRepository<History, Long> {
}