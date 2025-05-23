package com.test.gestiondepartements.Strategy;

import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Security.Entities.Utilisateur;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
@Component
public class SpecificWorkloadAssignmentStrategy implements WorkloadAssignmentStrategy {
    @Override
    public void assignWorkload(Module module, List<Utilisateur> enseignants, Map<Long, Integer> workloadMap) {
        enseignants.forEach(e -> {
            Integer charge = workloadMap.get(e.getId());
            if (charge != null) {
                e.getModules().add(module);
            }
        });
    }
}