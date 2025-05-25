package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Command.AddDepartmentCommand;
import com.test.gestiondepartements.Command.Command;
import com.test.gestiondepartements.Command.CommandInvoker;
import com.test.gestiondepartements.Command.UpdateDepartmentCommand;
import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.DepartmentService;
import com.test.gestiondepartements.Service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CommandInvoker commandInvoker;
    private final DepartmentRepository departmentRepository;
    private final HistoryRepository historyRepository;
    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;

    public DepartmentController(DepartmentService departmentService,
                                CommandInvoker commandInvoker,
                                DepartmentRepository departmentRepository,
                                HistoryRepository historyRepository,
                                NotificationService notificationService,
                                UtilisateurRepository utilisateurRepository) {
        this.departmentService = departmentService;
        this.commandInvoker = commandInvoker;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("departmentDTO", new DepartmentDTO());
        return "admin/departments";
    }

    @PostMapping
    public String addDepartment(@Valid @ModelAttribute DepartmentDTO departmentDTO,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
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
                    historyRepository,
                    notificationService,
                    utilisateurRepository
            );

            commandInvoker.executeCommand(command);

            notificationService.createNewDepartmentNotification(
                    department,
                    "Nouveau département '" + department.getName() + "' créé"
            );

            redirectAttributes.addFlashAttribute("success", "Département créé avec succès");
            return "redirect:/admin/departments";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Un département avec ce nom existe déjà");
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "admin/departments";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la création: " + e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "admin/departments";
        }
    }

    @PostMapping("/update")
    public String updateDepartment(@Valid @ModelAttribute DepartmentDTO departmentDTO,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
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

            redirectAttributes.addFlashAttribute("success", "Département mis à jour avec succès");
            return "redirect:/admin/departments";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "admin/departments";
        }
    }

    @PostMapping("/startVote/{id}")
    public String startVote(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Département non trouvé"));

            if (department.getHeadOfDepartment() != null) {
                throw new RuntimeException("Un chef de département est déjà désigné");
            }

            department.setVoteEnCours(true);
            departmentRepository.save(department);

            // Envoyer des notifications aux enseignants
            notificationService.sendVoteNotification(department, "Vote lancé pour le chef de département '" + department.getName() + "'");

            redirectAttributes.addFlashAttribute("success", "Vote lancé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    @PostMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            departmentRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Département supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }
}