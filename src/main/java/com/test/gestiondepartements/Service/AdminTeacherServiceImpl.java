package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.EnseignantWorkloadDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTeacherServiceImpl implements AdminTeacherService {

    private final UtilisateurRepository utilisateurRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EnseignantWorkloadDTO> getEnseignantsWithWorkload(String departmentNameFilter, Integer minWorkloadFilter, Integer maxWorkloadFilter) {
        List<Utilisateur> enseignants = utilisateurRepository.findByAppRoles_RoleName("ENSEIGNANT");
        List<EnseignantWorkloadDTO> dtos = new ArrayList<>();

        for (Utilisateur enseignant : enseignants) {
            int totalWorkload = 0;
            if (enseignant.getModules() != null) {
                for (Module module : enseignant.getModules()) {
                    totalWorkload += module.getWorkload();
                }
            }

            List<String> departmentNames = new ArrayList<>();
            if (enseignant.getDepartments() != null) {
                for (Department department : enseignant.getDepartments()) {
                    departmentNames.add(department.getName());
                }
            }
            if (departmentNames.isEmpty()) {
                departmentNames.add("N/A");
            }

            dtos.add(new EnseignantWorkloadDTO(
                    enseignant.getId(),
                    enseignant.getFirstName(),
                    enseignant.getLastName(),
                    enseignant.getUsername(),
                    departmentNames,
                    totalWorkload,
                    // Champs ajoutés
                    enseignant.getPhone(),
                    enseignant.getEducation(),
                    enseignant.getSkills(),
                    enseignant.getLanguages()
            ));
        }

        return dtos.stream()
                .filter(dto -> departmentNameFilter == null || departmentNameFilter.trim().isEmpty() ||
                        dto.getDepartmentNames().stream().anyMatch(dn -> dn.equalsIgnoreCase(departmentNameFilter)))
                .filter(dto -> minWorkloadFilter == null || dto.getTotalWorkload() >= minWorkloadFilter)
                .filter(dto -> maxWorkloadFilter == null || dto.getTotalWorkload() <= maxWorkloadFilter)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public Page<EnseignantWorkloadDTO> getEnseignantsWithWorkloadPaginated(String departmentNameFilter, Integer minWorkloadFilter, Integer maxWorkloadFilter, Pageable pageable) {
        List<Utilisateur> allEnseignants = utilisateurRepository.findByAppRoles_RoleName("ENSEIGNANT");
        List<EnseignantWorkloadDTO> allDtos = allEnseignants.stream().map(enseignant -> {
            int totalWorkload = enseignant.getModules().stream().mapToInt(Module::getWorkload).sum();
            List<String> departmentNames = enseignant.getDepartments().stream()
                    .map(Department::getName)
                    .collect(Collectors.toList());
            if (departmentNames.isEmpty()) {
                departmentNames.add("Aucun département");
            }
            return new EnseignantWorkloadDTO(
                    enseignant.getId(),
                    enseignant.getFirstName(),
                    enseignant.getLastName(),
                    enseignant.getUsername(),
                    departmentNames,
                    totalWorkload,
                    enseignant.getPhone(),
                    enseignant.getEducation(),
                    enseignant.getSkills(),
                    enseignant.getLanguages()
            );
        }).collect(Collectors.toList());

        List<EnseignantWorkloadDTO> filteredDtos = allDtos.stream()
                .filter(dto -> departmentNameFilter == null || departmentNameFilter.trim().isEmpty() ||
                        dto.getDepartmentNames().stream().anyMatch(dn -> dn.equalsIgnoreCase(departmentNameFilter)))
                .filter(dto -> minWorkloadFilter == null || dto.getTotalWorkload() >= minWorkloadFilter)
                .filter(dto -> maxWorkloadFilter == null || dto.getTotalWorkload() <= maxWorkloadFilter)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredDtos.size());

        List<EnseignantWorkloadDTO> pageContent = new ArrayList<>();
        if (start <= end) {
            pageContent = filteredDtos.subList(start, end);
        }

        return new PageImpl<>(pageContent, pageable, filteredDtos.size());
    }


    @Override
    @Transactional(readOnly = true)
    public List<String> getAllDepartmentNames() {
        return departmentRepository.findAll().stream()
                .map(Department::getName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}