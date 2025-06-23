package com.test.gestiondepartements.Strategy;

import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Security.Entities.Utilisateur;

import java.util.List;
import java.util.Map;

public interface WorkloadAssignmentStrategy {
    void assignWorkload(Module module, List<Utilisateur> enseignants, Map<Long, Integer> workloadMap);
}