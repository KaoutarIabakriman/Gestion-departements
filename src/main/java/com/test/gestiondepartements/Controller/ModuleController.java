package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.ModuleRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.DepartmentService;
import com.test.gestiondepartements.Service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleRepository moduleRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;
    private final DepartmentService departmentService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/admin/modules")
    public String listModules(Model model, HttpServletRequest request,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Module> modulesPage = moduleRepository.findAll(pageable);

        model.addAttribute("modules", modulesPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("currentURI", request.getRequestURI());

        return "admin/modules";
    }

    @GetMapping("/admin/modules/add")
    public String showAddModuleForm(Model model, HttpServletRequest request,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "size", defaultValue = "10") int size) {

        model.addAttribute("module", new Module());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("currentURI", request.getRequestURI());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "admin/addModule";
    }

    @PostMapping("/admin/modules/add")
    @Transactional
    public String addModule(@ModelAttribute("module") Module module,
                            RedirectAttributes redirectAttributes,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            if (module.getDepartment() == null || module.getDepartment().getId() == null) {
                redirectAttributes.addFlashAttribute("error", "Department selection is required.");
                return "redirect:/admin/modules/add?page=" + page + "&size=" + size; // Redirect back with pagination
            }

            Long departmentIdFromForm = module.getDepartment().getId();
            Department department = departmentRepository.findById(departmentIdFromForm)
                    .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentIdFromForm));

            module.setDepartment(department);
            moduleRepository.save(module);


            String notificationMessage = "Le module '" + module.getName() + "' a été ajouté au département " + department.getName() + ".";

            List<Utilisateur> enseignants = utilisateurRepository.findByAppRoles_RoleName("ENSEIGNANT");
            for (Utilisateur enseignant : enseignants) {

                if (departmentService.departmentMatchesSkills(department, enseignant) ||
                        departmentService.departmentMatchesSkills(department, enseignant)) {

                    notificationService.createNotification(
                            enseignant,
                            department,
                            module,
                            notificationMessage,
                            NotificationType.NEW_MODULE_ADDED,
                            null
                    );
                }
            }

            redirectAttributes.addFlashAttribute("success", "Module added successfully!");
            return "redirect:/admin/modules?page=" + page + "&size=" + size;

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error adding module: " + e.getMessage());
            return "redirect:/admin/modules/add?page=" + page + "&size=" + size; // Redirect back with pagination
        }
    }

    @GetMapping("/admin/modules/edit/{moduleId}")
    public String showEditModuleForm(@PathVariable Long moduleId, Model model, HttpServletRequest request) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec ID: " + moduleId));
        model.addAttribute("module", module);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("currentURI", request.getRequestURI());
        return "admin/editModule";
    }


    @PostMapping("/admin/modules/edit/{moduleId}")
    @Transactional
    public String updateModule(@PathVariable Long moduleId,
                               @ModelAttribute("module") Module moduleFromForm,
                               RedirectAttributes redirectAttributes) {
        try {
            Module moduleToUpdate = moduleRepository.findById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module not found with ID: " + moduleId));

            if (moduleFromForm.getDepartment() == null || moduleFromForm.getDepartment().getId() == null) {
                redirectAttributes.addFlashAttribute("error", "Department selection is required.");
                return "redirect:/admin/modules/edit/" + moduleId;
            }

            Long departmentIdFromForm = moduleFromForm.getDepartment().getId();
            Department department = departmentRepository.findById(departmentIdFromForm)
                    .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentIdFromForm));

            moduleToUpdate.setName(moduleFromForm.getName());
            moduleToUpdate.setDescription(moduleFromForm.getDescription());
            moduleToUpdate.setDepartment(department);
            moduleToUpdate.setWorkload(moduleFromForm.getWorkload());

            moduleRepository.save(moduleToUpdate);

            redirectAttributes.addFlashAttribute("success", "Module updated successfully!");
            return "redirect:/admin/modules";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error updating module: " + e.getMessage());
            return "redirect:/admin/modules/edit/" + moduleId;
        }
    }

}