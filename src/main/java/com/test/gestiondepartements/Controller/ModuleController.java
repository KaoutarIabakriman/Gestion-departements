package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.History;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;
import com.test.gestiondepartements.Repositories.ModuleRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.strategy.EvenWorkloadAssignmentStrategy;
import com.test.gestiondepartements.strategy.SpecificWorkloadAssignmentStrategy;
import com.test.gestiondepartements.strategy.WorkloadAssignmentStrategy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.HashSet; // Importer HashSet
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleRepository moduleRepository;
    private final DepartmentRepository departmentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final HistoryRepository historyRepository;

    @Autowired
    private EvenWorkloadAssignmentStrategy evenWorkloadAssignmentStrategy;

    @Autowired
    private SpecificWorkloadAssignmentStrategy specificWorkloadAssignmentStrategy;

    @GetMapping("/admin/modules")
    public String listModules(Model model) {
        List<Module> modules = moduleRepository.findAll();
        model.addAttribute("modules", modules);
        return "admin/modules";
    }

    @GetMapping("/admin/modules/add")
    public String showAddModuleForm(Model model) {
        model.addAttribute("module", new Module());
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin/addModule";
    }

    @PostMapping("/admin/modules/add")
    @Transactional
    public String addModule(@ModelAttribute("module") Module module, @RequestParam Long departmentId, RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentRepository.findById(departmentId).orElseThrow(() -> new RuntimeException("Department not found"));
            module.setDepartment(department);
            moduleRepository.save(module);

            History history = new History();
            history.setAction("CREATE");
            history.setEntityType("Module");
            history.setEntityId(module.getId());
            history.setDetails("Module '" + module.getName() + "' created.");
            historyRepository.save(history);

            redirectAttributes.addFlashAttribute("success", "Module ajout?? avec succ??s.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout du module : " + e.getMessage());
        }
        return "redirect:/admin/modules";
    }

    @PostMapping("/chef/modules/assign/{moduleId}")
    @Transactional
    public String assignModule(@PathVariable Long moduleId,
                               @RequestParam List<Long> enseignantIds,
                               @RequestParam(required = false) Map<String, String> allRequestParams,
                               @RequestParam String assignmentStrategy,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        try {
            Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
            Department department = departmentRepository.findByHeadOfDepartment(chef);
            if (department == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vous n'??tes pas chef d'un d??partement valide.");
                return "redirect:/chef/modules";
            }

            Module module = moduleRepository.findById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module non trouv?? avec ID: " + moduleId));

            if (!module.getDepartment().equals(department)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ce module n'appartient pas ?? votre d??partement.");
                return "redirect:/chef/modules/assign/" + moduleId;
            }

            List<Utilisateur> validSelectedEnseignants = utilisateurRepository.findAllById(enseignantIds).stream()
                    .filter(e -> department.getMembers().contains(e))
                    .collect(Collectors.toList());

            if (validSelectedEnseignants.isEmpty() && !enseignantIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Les enseignants s??lectionn??s n'appartiennent pas ?? votre d??partement ou aucun enseignant valide n'a ??t?? s??lectionn??.");
                return "redirect:/chef/modules/assign/" + moduleId;
            }
            if (validSelectedEnseignants.isEmpty() && module.getWorkload() > 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Aucun enseignant valide s??lectionn?? pour assigner au module.");
                return "redirect:/chef/modules/assign/" + moduleId;
            }

            WorkloadAssignmentStrategy strategy;
            int totalModuleWorkload = module.getWorkload();
            Map<Long, Integer> processedWorkloadMap = new HashMap<>();

            if ("specific".equals(assignmentStrategy)) {
                strategy = specificWorkloadAssignmentStrategy;

                if (allRequestParams == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Aucun param??tre de formulaire re??u pour la strat??gie sp??cifique.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }

                if (validSelectedEnseignants.isEmpty() && totalModuleWorkload > 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Veuillez s??lectionner des enseignants pour l'assignation sp??cifique.");
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
                        } else {
                            workloadValue = 0;
                        }
                    } else {
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

            } else {
                strategy = evenWorkloadAssignmentStrategy;
                if (validSelectedEnseignants.isEmpty() && totalModuleWorkload > 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Aucun enseignant s??lectionn?? ?? qui assigner la charge de travail uniform??ment.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }
            }

            strategy.assignWorkload(module, validSelectedEnseignants, processedWorkloadMap);

            List<Utilisateur> finalEnseignantsForModuleList;
            if ("specific".equals(assignmentStrategy)) {
                finalEnseignantsForModuleList = validSelectedEnseignants.stream()
                        .filter(e -> processedWorkloadMap.getOrDefault(e.getId(), 0) > 0)
                        .collect(Collectors.toList());
                if (finalEnseignantsForModuleList.isEmpty() && totalModuleWorkload > 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Aucun enseignant n'a ??t?? assign?? avec une charge de travail positive pour la strat??gie sp??cifique, malgr?? que la somme corresponde.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }
            } else {
                finalEnseignantsForModuleList = validSelectedEnseignants;
            }

            // MODIFIÉ ICI : Convertir la liste en HashSet avant de la setter
            module.setEnseignants(new HashSet<>(finalEnseignantsForModuleList));
            moduleRepository.save(module);

            History history = new History();
            history.setAction("ASSIGN_MODULE");
            history.setEntityType("Module");
            history.setEntityId(module.getId());
            String assignedUsernames = finalEnseignantsForModuleList.stream() // Utilise toujours la liste pour les détails
                    .map(Utilisateur::getUsername)
                    .collect(Collectors.joining(", "));
            if (assignedUsernames.isEmpty()) assignedUsernames = "aucun";
            history.setDetails("Module '" + module.getName() + "' assign??. Strat??gie: " + assignmentStrategy +
                    ". Enseignants: " + assignedUsernames + ".");
            historyRepository.save(history);

            redirectAttributes.addFlashAttribute("successMessage", "Module assign?? avec succ??s."); // Changé pour correspondre au nom dans le template

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur d'assignation: " + e.getMessage());
            return "redirect:/chef/modules/assign/" + moduleId;
        }
        catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur syst??me: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/chef/modules/assign/" + moduleId;
        }
        return "redirect:/chef/modules";
    }

    @GetMapping("/admin/modules/edit/{moduleId}")
    public String showEditModuleForm(@PathVariable Long moduleId, Model model) {
        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new RuntimeException("Module not found"));
        model.addAttribute("module", module);
        model.addAttribute("allEnseignants", utilisateurRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin/editModule";
    }

    @PostMapping("/admin/modules/edit/{moduleId}")
    @Transactional
    public String updateModule(@PathVariable Long moduleId, @ModelAttribute Module updatedModule, @RequestParam Long departmentId, RedirectAttributes redirectAttributes) {
        try {
            Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new RuntimeException("Module not found"));
            module.setName(updatedModule.getName());
            module.setDescription(updatedModule.getDescription());
            module.setWorkload(updatedModule.getWorkload());

            Department department = departmentRepository.findById(departmentId).orElseThrow(() -> new RuntimeException("Department not found"));
            module.setDepartment(department);

            // Conserver les enseignants existants si non modifiés explicitement ici
            // ou prévoir une logique pour les mettre à jour si l'édition de module permet de changer les enseignants
            // Pour l'instant, on ne touche pas à module.getEnseignants() lors d'une simple édition des détails du module

            moduleRepository.save(module);

            History history = new History();
            history.setAction("UPDATE");
            history.setEntityType("Module");
            history.setEntityId(module.getId());
            history.setDetails("Module '" + updatedModule.getName() + "' modified.");
            historyRepository.save(history);

            redirectAttributes.addFlashAttribute("success", "Module mis ?? jour avec succ??s !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise ?? jour du module : " + e.getMessage());
        }
        return "redirect:/admin/modules";
    }
}