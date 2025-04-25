package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public String listDepartments(Model model) {
        List<Department> departments = departmentService.getAllDepartments();
        model.addAttribute("departments", departments);
        model.addAttribute("departmentDTO", new DepartmentDTO());
        return "admin/departments";
    }

    @PostMapping
    public String addDepartment(@Valid DepartmentDTO departmentDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "admin/departments";
        }

        try {
            departmentService.createDepartment(departmentDTO);
            return "redirect:/admin/departments?success";
        } catch (DataIntegrityViolationException e) {
            // Capturer l'erreur de duplication
            model.addAttribute("error", "Un département avec ce nom existe déjà.");
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "admin/departments";
        }
    }
    @PostMapping("/update")
    public String updateDepartment(@ModelAttribute DepartmentDTO departmentDTO) {
        departmentService.updateDepartment(departmentDTO.getId(), departmentDTO);
        return "redirect:/admin/departments";
    }
}