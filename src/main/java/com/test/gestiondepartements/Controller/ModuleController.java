package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Entities.ModuleRequest;
import com.test.gestiondepartements.Entities.ModuleRequestStatus;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
// import com.test.gestiondepartements.Repositories.HistoryRepository; // Not used in these methods
import com.test.gestiondepartements.Repositories.ModuleRepository;
import com.test.gestiondepartements.Repositories.ModuleRequestRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.NotificationService;
import com.test.gestiondepartements.Strategy.EvenWorkloadAssignmentStrategy;
import com.test.gestiondepartements.Strategy.SpecificWorkloadAssignmentStrategy;
import com.test.gestiondepartements.Strategy.WorkloadAssignmentStrategy;

import jakarta.servlet.http.HttpServletRequest; // Import HttpServletRequest

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleRepository moduleRepository;
    private final DepartmentRepository departmentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final ModuleRequestRepository moduleRequestRepository;

    private final EvenWorkloadAssignmentStrategy evenWorkloadAssignmentStrategy;
    private final SpecificWorkloadAssignmentStrategy specificWorkloadAssignmentStrategy;


    @GetMapping("/admin/modules")
    public String listModules(Model model, HttpServletRequest request, // Add HttpServletRequest
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Module> modulesPage = moduleRepository.findAll(pageable);

        model.addAttribute("modules", modulesPage);
        // model.addAttribute("departments", departmentRepository.findAll()); // For add module modal if used
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("currentURI", request.getRequestURI()); // Add this line

        return "admin/modules";
    }

    @GetMapping("/admin/modules/add")
    public String showAddModuleForm(Model model, HttpServletRequest request, // Add HttpServletRequest
                                    @RequestParam(name = "page", defaultValue = "0") int page, // For redirect consistency
                                    @RequestParam(name = "size", defaultValue = "10") int size) {

        model.addAttribute("module", new Module());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("currentURI", request.getRequestURI()); // Add this line
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "admin/addModule"; // Renders addModule.html
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
                // Redirect back to the add form, passing current page/size for consistency
                return "redirect:/admin/modules/add?page=" + page + "&size=" + size;
            }

            Long departmentIdFromForm = module.getDepartment().getId();
            Department department = departmentRepository.findById(departmentIdFromForm)
                    .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentIdFromForm));

            module.setDepartment(department);
            moduleRepository.save(module);

            redirectAttributes.addFlashAttribute("success", "Module added successfully!");
            return "redirect:/admin/modules?page=" + page + "&size=" + size;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error adding module: " + e.getMessage());
            return "redirect:/admin/modules/add?page=" + page + "&size=" + size;
        }
    }


    @GetMapping("/admin/modules/edit/{moduleId}")
    public String showEditModuleForm(@PathVariable Long moduleId, Model model, HttpServletRequest request) { // Add HttpServletRequest
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouv?? avec ID: " + moduleId));
        model.addAttribute("module", module);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("currentURI", request.getRequestURI()); // Add this line
        return "admin/editModule"; // Renders editModule.html
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

    // The chef/modules/assign method remains the same as in the previous version.
    // Make sure it also adds currentURI if it renders a view with the sidebar.
    // Based on its current logic, it only does redirects.
    @PostMapping("/chef/modules/assign/{moduleId}")
    @Transactional
    public String assignModule(@PathVariable Long moduleId,
                               @RequestParam(required = false) List<Long> enseignantIds,
                               @RequestParam(required = false) Map<String, String> allRequestParams,
                               @RequestParam String assignmentStrategy,
                               @AuthenticationPrincipal UserDetails userDetails,
                               // HttpServletRequest request, // Not needed if only redirecting
                               RedirectAttributes redirectAttributes) {


        try {
            Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
            Department department = departmentRepository.findByHeadOfDepartment(chef);
            if (department == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vous n'??tes pas chef d'un d??partement valide.");
                return "redirect:/chef/modules"; // Assuming this is the correct redirect
            }


            Module module = moduleRepository.findById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module non trouv??"));


            if (!module.getDepartment().equals(department)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ce module n'appartient pas ?? votre d??partement.");
                return "redirect:/chef/modules/assign/" + moduleId;
            }


            Set<Utilisateur> anciensEnseignants = new HashSet<>(module.getEnseignants());

            List<Utilisateur> validSelectedEnseignants = Collections.emptyList();
            if (enseignantIds != null && !enseignantIds.isEmpty()) {
                validSelectedEnseignants = utilisateurRepository.findAllById(enseignantIds).stream()
                        .filter(e -> department.getMembers().contains(e))
                        .collect(Collectors.toList());
            }


            if ((enseignantIds == null || enseignantIds.isEmpty()) && module.getWorkload() > 0) {
                if ("even".equals(assignmentStrategy)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Veuillez s??lectionner au moins un enseignant pour la r??partition ??gale si le module a une charge horaire.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }
            }


            WorkloadAssignmentStrategy strategy;
            int totalModuleWorkload = module.getWorkload();
            Map<Long, Integer> processedWorkloadMap = new HashMap<>();


            if ("specific".equals(assignmentStrategy)) {

                strategy = specificWorkloadAssignmentStrategy;

                if (allRequestParams == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Aucun param??tre de formulaire re??u.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }


                if (validSelectedEnseignants.isEmpty() && totalModuleWorkload > 0) {
                    if (enseignantIds != null && !enseignantIds.isEmpty()) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Les enseignants coch??s n'appartiennent pas ?? votre d??partement.");
                        return "redirect:/chef/modules/assign/" + moduleId;
                    }
                    redirectAttributes.addFlashAttribute("errorMessage", "Veuillez s??lectionner des enseignants pour l'assignation sp??cifique si le module a une charge.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }


                int sumOfSpecificWorkloads = 0;
                for (Utilisateur selectedEnseignant : validSelectedEnseignants) {
                    Long currentEnseignantId = selectedEnseignant.getId();
                    String paramValue = allRequestParams.get(String.valueOf(currentEnseignantId));
                    Integer workloadValue;


                    if (paramValue == null || paramValue.trim().isEmpty()) {
                        if (totalModuleWorkload > 0) {
                            redirectAttributes.addFlashAttribute("errorMessage", "La charge de travail pour l'enseignant s??lectionn?? " + selectedEnseignant.getFirstName() + " " + selectedEnseignant.getLastName() + " (ID: " + currentEnseignantId + ") doit ??tre sp??cifi??e.");
                            return "redirect:/chef/modules/assign/" + moduleId;
                        }
                        else {
                            workloadValue = 0;
                        }
                    }

                    else {
                        try {
                            workloadValue = Integer.parseInt(paramValue);
                            if (workloadValue < 0) {
                                redirectAttributes.addFlashAttribute("errorMessage", "La charge de travail pour l'enseignant ID " + currentEnseignantId + " ne peut ??tre n??gative.");
                                return "redirect:/chef/modules/assign/" + moduleId;
                            }
                        } catch (NumberFormatException e) {
                            redirectAttributes.addFlashAttribute("errorMessage", "Charge de travail invalide ('" + paramValue + "') pour l'enseignant ID " + currentEnseignantId + ". Ce doit ??tre un nombre.");
                            return "redirect:/chef/modules/assign/" + moduleId;
                        }
                    }
                    processedWorkloadMap.put(currentEnseignantId, workloadValue);
                    sumOfSpecificWorkloads += workloadValue;
                }


                if (sumOfSpecificWorkloads != totalModuleWorkload) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "La somme des charges sp??cifiques (" + sumOfSpecificWorkloads + ") doit ??tre ??gale ?? la charge totale du module (" + totalModuleWorkload + " heures).");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }


            }

            else { // "even" or default
                strategy = evenWorkloadAssignmentStrategy;
                if (validSelectedEnseignants.isEmpty() && totalModuleWorkload > 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Aucun enseignant s??lectionn?? ?? qui assigner la charge de travail uniform??ment.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }
            }



            strategy.assignWorkload(module, validSelectedEnseignants, processedWorkloadMap);


            List<Utilisateur> finalEnseignantsForModuleList;
            if("specific".equals(assignmentStrategy)){
                finalEnseignantsForModuleList = validSelectedEnseignants.stream()
                        .filter(e -> processedWorkloadMap.getOrDefault(e.getId(), 0) > 0)
                        .collect(Collectors.toList());
            }else { // even assignment
                finalEnseignantsForModuleList = validSelectedEnseignants;
            }


            Set<Utilisateur> nouveauxEnseignantsAffectes = new HashSet<>(finalEnseignantsForModuleList);
            module.setEnseignants(nouveauxEnseignantsAffectes);
            Module savedModule = moduleRepository.save(module);


            List<ModuleRequest> requestsForModule = moduleRequestRepository.findByModuleAndStatus(savedModule, ModuleRequestStatus.PENDING);
            for (ModuleRequest req : requestsForModule) {
                if (nouveauxEnseignantsAffectes.contains(req.getEnseignant())) {
                    req.setStatus(ModuleRequestStatus.APPROVED);
                } else {
                    // If they were PENDING and not selected, they might be implicitly rejected or stay pending.
                    // Current logic seems to reject if not in nouveauxEnseignantsAffectes. This might be too aggressive.
                    // Consider if PENDING requests for unselected teachers should remain PENDING or be explicitly REJECTED.
                    // For now, keeping existing logic:
                    req.setStatus(ModuleRequestStatus.REJECTED);
                }
                moduleRequestRepository.save(req);
            }


            for (Utilisateur enseignant : nouveauxEnseignantsAffectes) {

                if(!anciensEnseignants.contains(enseignant)) { // Notify only newly assigned
                    String message = "Vous avez ??t?? affect?? au module '" + savedModule.getName() +
                            "' dans le d??partement '" + department.getName() + "'.";
                    notificationService.createNotification(enseignant, department, savedModule, message, NotificationType.MODULE_ASSIGNMENT, null);
                }
            }


            for(Utilisateur ancienEnseignant : anciensEnseignants) {
                if(!nouveauxEnseignantsAffectes.contains(ancienEnseignant)) { // Notify only unassigned
                    String messageRetrait = "Vous avez ??t?? retir?? du module '" + savedModule.getName() +
                            "' dans le d??partement '" + department.getName() + "'.";
                    notificationService.createNotification(ancienEnseignant, department, savedModule, messageRetrait, NotificationType.MODULE_UNASSIGNMENT, null);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Module '" + savedModule.getName() + "' assign?? avec succ??s.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur d'assignation: " + e.getMessage());
            return "redirect:/chef/modules/assign/" + moduleId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur syst??me lors de l'assignation: " + e.getMessage());
            // It's good practice to log the full exception for server-side debugging
            // log.error("System error during module assignment for module ID {}: ", moduleId, e);
            e.printStackTrace();
            return "redirect:/chef/modules/assign/" + moduleId;
        }
        return "redirect:/chef/modules";
    }
}