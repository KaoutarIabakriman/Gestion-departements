package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.ModuleRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/modules") // Ou /chef/modules si géré par le chef de département
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleRepository moduleRepository;
    private final DepartmentRepository departmentRepository;
    private final UtilisateurRepository utilisateurRepository;


    @GetMapping
    public String listModules(Model model) {
        List<Module> modules = moduleRepository.findAll();
        model.addAttribute("modules", modules);
        return "admin/modules"; // Créer ce template
    }

    @GetMapping("/add")
    public String showAddModuleForm(Model model) {
        model.addAttribute("module", new Module()); // <-- CRUCIAL !
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin/addModule";
    }

    @PostMapping("/add")
    public String addModule(@ModelAttribute("module") Module module,
                            @RequestParam Long departmentId,  // Pour le département
                            RedirectAttributes redirectAttributes){
        try {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            module.setDepartment(department);
            moduleRepository.save(module);
            redirectAttributes.addFlashAttribute("success", "Module ajouté avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout du module : " + e.getMessage());
        }
        return "redirect:/admin/modules";
    }
    @PostMapping("/assign")
    public String assignModuleToEnseignants(
            @RequestParam Long moduleId,
            @RequestParam(required = false) List<Long> enseignantIds,
            RedirectAttributes redirectAttributes) {

        try {
            Module module = moduleRepository.findById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module not found"));

            if (enseignantIds != null) {
                List<Utilisateur> enseignants = utilisateurRepository.findAllById(enseignantIds);
                module.getEnseignants().clear(); // Ou la logique d'ajout sans remplacement
                module.getEnseignants().addAll(enseignants);
                moduleRepository.save(module);
                redirectAttributes.addFlashAttribute("success", "Module assigned successfully!");
            } else {
                redirectAttributes.addFlashAttribute("warning", "No teachers selected for assignment.");
            }

            return "redirect:/admin/modules/edit/" + moduleId; // Rediriger vers la page d'édition
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error assigning module: " + e.getMessage());
            return "redirect:/admin/modules/edit/" + moduleId; // Rediriger vers la page d'édition
        }
    }

    @GetMapping("/edit/{moduleId}")
    public String showEditModuleForm(@PathVariable Long moduleId, Model model) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        model.addAttribute("module", module);
        model.addAttribute("departments", departmentRepository.findAll()); // Liste des départements
        return "admin/editModule";
    }
    @PostMapping("/edit/{moduleId}")
    public String updateModule(@PathVariable Long moduleId, @ModelAttribute Module updatedModule, @RequestParam Long departmentId, RedirectAttributes redirectAttributes) {
        try {
            Module module = moduleRepository.findById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module not found"));

            module.setName(updatedModule.getName());
            module.setDescription(updatedModule.getDescription());
            module.setWorkload(updatedModule.getWorkload()); // Mettre à jour la charge horaire

            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found"));

            module.setDepartment(department);


            moduleRepository.save(module);

            redirectAttributes.addFlashAttribute("success", "Module mis à jour avec succès !");
            return "redirect:/admin/modules"; // Ou rediriger vers la page d'édition
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour du module : " + e.getMessage());
            return "redirect:/admin/modules"; // Ou rediriger vers la page d'édition
        }
    }


}