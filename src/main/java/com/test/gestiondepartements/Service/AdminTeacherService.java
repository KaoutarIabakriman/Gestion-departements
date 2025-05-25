package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.EnseignantWorkloadDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminTeacherService {
    List<EnseignantWorkloadDTO> getEnseignantsWithWorkload(String departmentNameFilter, Integer minWorkloadFilter, Integer maxWorkloadFilter);
    Page<EnseignantWorkloadDTO> getEnseignantsWithWorkloadPaginated(String departmentNameFilter, Integer minWorkloadFilter, Integer maxWorkloadFilter, Pageable pageable);
    List<String> getAllDepartmentNames();
}