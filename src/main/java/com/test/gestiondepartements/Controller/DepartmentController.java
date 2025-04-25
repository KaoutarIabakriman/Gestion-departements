package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Service.DepartmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String addDepartment(@ModelAttribute DepartmentDTO departmentDTO) {
        departmentService.createDepartment(departmentDTO);
        return "redirect:/admin/departments";
    }

}