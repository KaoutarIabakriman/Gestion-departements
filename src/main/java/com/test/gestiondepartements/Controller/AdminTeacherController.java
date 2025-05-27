package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Dto.EnseignantWorkloadDTO;
import com.test.gestiondepartements.Service.AdminTeacherService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/teachers")
@RequiredArgsConstructor
public class AdminTeacherController {

    private final AdminTeacherService adminTeacherService;


    @GetMapping
    public String listTeachers(Model model, HttpServletRequest request,
                               @RequestParam(name = "departmentName", required = false) String departmentNameFilter,
                               @RequestParam(name = "minWorkload", required = false) Integer minWorkloadFilter,
                               @RequestParam(name = "maxWorkload", required = false) Integer maxWorkloadFilter,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<EnseignantWorkloadDTO> teacherPage = adminTeacherService.getEnseignantsWithWorkloadPaginated(
                departmentNameFilter, minWorkloadFilter, maxWorkloadFilter, pageable
        );
        List<String> allDepartmentNames = adminTeacherService.getAllDepartmentNames();

        model.addAttribute("teacherPage", teacherPage);
        model.addAttribute("allDepartmentNames", allDepartmentNames);
        model.addAttribute("currentURI", request.getRequestURI());

        model.addAttribute("currentDepartmentNameFilter", departmentNameFilter);
        model.addAttribute("currentMinWorkloadFilter", minWorkloadFilter);
        model.addAttribute("currentMaxWorkloadFilter", maxWorkloadFilter);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        model.addAttribute("pageTitle", "Liste des Enseignants");


        return "admin/teachers";    }
}