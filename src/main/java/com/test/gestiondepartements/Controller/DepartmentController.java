package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Command.AddDepartmentCommand;
import com.test.gestiondepartements.Command.Command;
import com.test.gestiondepartements.Command.CommandInvoker;
import com.test.gestiondepartements.Command.UpdateDepartmentCommand;
import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;
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
    private final CommandInvoker commandInvoker;
    private final DepartmentRepository departmentRepository;
    private final HistoryRepository historyRepository;
    public DepartmentController(DepartmentService departmentService, CommandInvoker commandInvoker, DepartmentRepository departmentRepository, HistoryRepository historyRepository) {
        this.departmentService = departmentService;
        this.commandInvoker = commandInvoker;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
    }

    @GetMapping
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
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
            Department department = new Department();
            department.setName(departmentDTO.getName());
            department.setDescription(departmentDTO.getDescription());

            Command command = new AddDepartmentCommand(
                    department,
                    departmentRepository,
                    historyRepository // Remplacer par le repository approprié si nécessaire
            );

            commandInvoker.executeCommand(command);

            return "redirect:/admin/departments?success";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Un département avec ce nom existe déjà.");
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "admin/departments";
        }
    }
    @PostMapping("/update")
    public String updateDepartment(@ModelAttribute @Valid DepartmentDTO departmentDTO,
                                   BindingResult result,
                                   Model model) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "admin/departments";
        }

        try {
            Command command = new UpdateDepartmentCommand(
                    departmentDTO,
                    departmentRepository,
                    historyRepository
            );

            commandInvoker.executeCommand(command);

            return "redirect:/admin/departments?success";
        } catch (Exception e) {
            model.addAttribute("error", "Error updating department: " + e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "admin/departments";
        }
    }
}