package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.History;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Entities.NotificationType; // Importé
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;
import com.test.gestiondepartements.Repositories.ModuleRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.NotificationService; // Importé
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

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor // Assurez-vous que notificationService est final pour être inclus par Lombok
public class ModuleController {

    private final ModuleRepository moduleRepository;
    private final DepartmentRepository departmentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final HistoryRepository historyRepository;
    private final NotificationService notificationService; // Injection via constructeur grâce à @RequiredArgsConstructor

    // Les stratégies peuvent aussi être injectées via le constructeur si déclarées final
    // Ou gardées avec @Autowired si vous préférez cette approche.
    // Pour la cohérence avec @RequiredArgsConstructor, je les passerais en final.
    // private final EvenWorkloadAssignmentStrategy evenWorkloadAssignmentStrategy;
    // private final SpecificWorkloadAssignmentStrategy specificWorkloadAssignmentStrategy;
    // Si vous les passez en final, retirez @Autowired.
    @Autowired
    private EvenWorkloadAssignmentStrategy evenWorkloadAssignmentStrategy;
    @Autowired
    private SpecificWorkloadAssignmentStrategy specificWorkloadAssignmentStrategy;


    @GetMapping("/admin/modules")
    public String listModules(Model model) {
        List<Module> modules = moduleRepository.findAll();
        model.addAttribute("modules", modules);
        model.addAttribute("pageTitle", "Gestion des Modules (Admin)");
        return "admin/modules";
    }

    @GetMapping("/admin/modules/add")
    public String showAddModuleForm(Model model) {
        model.addAttribute("module", new Module());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("pageTitle", "Ajouter un Nouveau Module");
        return "admin/addModule";
    }

    @PostMapping("/admin/modules/add")
    @Transactional
    public String addModule(@ModelAttribute("module") Module module, @RequestParam Long departmentId, RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Département non trouvé avec ID: " + departmentId));
            module.setDepartment(department);
            Module savedModule = moduleRepository.save(module);

            History history = new History();
            history.setAction("CREATE");
            history.setEntityType("Module");
            history.setEntityId(savedModule.getId());
            history.setDetails("Module '" + savedModule.getName() + "' créé dans le département '" + department.getName() + "'.");
            historyRepository.save(history);

            // --- DÉBUT NOTIFICATION AJOUT MODULE ---
            String messageNotificationAjoutBase = "Un nouveau module '" + savedModule.getName() + "' a été ajouté au département '" + department.getName() + "'.";
            List<Utilisateur> enseignantsDuDepartement = department.getMembers().stream()
                    .filter(membre -> membre.getAppRoles().stream().anyMatch(role -> "ENSEIGNANT".equals(role.getRoleName())))
                    .collect(Collectors.toList());

            for (Utilisateur enseignant : enseignantsDuDepartement) {
                boolean matchesSkills = false;
                // Logique de correspondance des compétences (peut être externalisée vers NotificationService)
                if (enseignant.getSkills() != null && !enseignant.getSkills().trim().isEmpty() &&
                        savedModule.getDescription() != null && !savedModule.getDescription().trim().isEmpty()) {
                    String[] userSkillsArray = enseignant.getSkills().toLowerCase().split("\\s*,\\s*");
                    String moduleDescription = savedModule.getDescription().toLowerCase();
                    matchesSkills = Arrays.stream(userSkillsArray)
                            .map(String::trim)
                            .filter(skill -> !skill.isEmpty())
                            .anyMatch(moduleDescription::contains);
                }
                // Alternative si vous avez refactorisé :
                // boolean matchesSkills = notificationService.skillsMatchDescription(enseignant.getSkills(), savedModule.getDescription());


                String finalMessage = messageNotificationAjoutBase;
                if (matchesSkills) {
                    finalMessage += " Il pourrait correspondre à vos compétences.";
                }

                notificationService.createNotification(
                        enseignant,
                        department, // Département auquel le module est rattaché
                        finalMessage,
                        NotificationType.NEW_DEPARTMENT, // Ou NEW_MODULE_ADDED si vous l'avez créé
                        null // Pas de vote associé
                );
            }
            // --- FIN NOTIFICATION AJOUT MODULE ---

            redirectAttributes.addFlashAttribute("successMessage", "Module '" + savedModule.getName() + "' ajouté avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'ajout du module : " + e.getMessage());
            e.printStackTrace(); // Pour le débogage
        }
        return "redirect:/admin/modules";
    }


    @PostMapping("/chef/modules/assign/{moduleId}")
    @Transactional
    public String assignModule(@PathVariable Long moduleId,
                               @RequestParam(required = false) List<Long> enseignantIds, // Peut être vide si on désassigne tout le monde
                               @RequestParam(required = false) Map<String, String> allRequestParams,
                               @RequestParam String assignmentStrategy,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
            Department department = departmentRepository.findByHeadOfDepartment(chef);
            if (department == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vous n'êtes pas chef d'un département valide.");
                return "redirect:/chef/modules";
            }

            Module module = moduleRepository.findById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module non trouvé avec ID: " + moduleId));

            if (!module.getDepartment().equals(department)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ce module n'appartient pas à votre département.");
                return "redirect:/chef/modules/assign/" + moduleId;
            }

            Set<Utilisateur> anciensEnseignants = new HashSet<>(module.getEnseignants());

            List<Utilisateur> validSelectedEnseignants = Collections.emptyList();
            if (enseignantIds != null && !enseignantIds.isEmpty()) {
                validSelectedEnseignants = utilisateurRepository.findAllById(enseignantIds).stream()
                        .filter(e -> department.getMembers().contains(e)) // S'assurer qu'ils sont membres du département du chef
                        .collect(Collectors.toList());
            }


            // Si enseignantIds est null ou vide (aucun enseignant sélectionné),
            // et que le module a une charge > 0, cela peut être une erreur selon la stratégie.
            if ((enseignantIds == null || enseignantIds.isEmpty()) && module.getWorkload() > 0) {
                // Si on désassigne tout le monde, la stratégie "even" n'a pas de sens.
                // Pour "specific", cela voudrait dire que toutes les charges sont à 0.
                if ("even".equals(assignmentStrategy)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Veuillez sélectionner au moins un enseignant pour la répartition égale si le module a une charge horaire.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }
                // Si specific, et aucun enseignant selectionné, la somme des charges sera 0.
                // Le module aura 0 enseignants actifs si charge > 0.
            }


            WorkloadAssignmentStrategy strategy;
            int totalModuleWorkload = module.getWorkload();
            Map<Long, Integer> processedWorkloadMap = new HashMap<>();

            if ("specific".equals(assignmentStrategy)) {
                strategy = specificWorkloadAssignmentStrategy;
                if (allRequestParams == null) { // Normalement, le form envoie tjrs des params (_csrf au moins)
                    redirectAttributes.addFlashAttribute("errorMessage", "Aucun paramètre de formulaire reçu.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }

                // Si aucun enseignant n'est valide après filtrage, et que la stratégie est spécifique avec une charge de module > 0
                if (validSelectedEnseignants.isEmpty() && totalModuleWorkload > 0) {
                    // Si enseignantIds était non vide mais qu'aucun n'était valide pour le département
                    if (enseignantIds != null && !enseignantIds.isEmpty()) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Les enseignants cochés n'appartiennent pas à votre département.");
                        return "redirect:/chef/modules/assign/" + moduleId;
                    }
                    // Si enseignantIds était vide dès le départ
                    redirectAttributes.addFlashAttribute("errorMessage", "Veuillez sélectionner des enseignants pour l'assignation spécifique si le module a une charge.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }

                int sumOfSpecificWorkloads = 0;
                for (Utilisateur selectedEnseignant : validSelectedEnseignants) { // Itérer seulement sur les sélectionnés et valides
                    Long currentEnseignantId = selectedEnseignant.getId();
                    String paramValue = allRequestParams.get(String.valueOf(currentEnseignantId));
                    Integer workloadValue;
                    if (paramValue == null || paramValue.trim().isEmpty()) {
                        if (totalModuleWorkload > 0) {
                            redirectAttributes.addFlashAttribute("errorMessage", "La charge de travail pour l'enseignant sélectionné " + selectedEnseignant.getFirstName() + " " + selectedEnseignant.getLastName() + " (ID: " + currentEnseignantId + ") doit être spécifiée.");
                            return "redirect:/chef/modules/assign/" + moduleId;
                        } else {
                            workloadValue = 0;
                        }
                    } else {
                        try {
                            workloadValue = Integer.parseInt(paramValue);
                            if (workloadValue < 0) {
                                redirectAttributes.addFlashAttribute("errorMessage", "La charge de travail pour l'enseignant ID " + currentEnseignantId + " ne peut être négative.");
                                return "redirect:/chef/modules/assign/" + moduleId;
                            }
                        } catch (NumberFormatException e) {
                            redirectAttributes.addFlashAttribute("errorMessage", "Charge de travail invalide ('" + paramValue + "') pour l'enseignant ID " + currentEnseignantId + ". Ce doit être un nombre.");
                            return "redirect:/chef/modules/assign/" + moduleId;
                        }
                    }
                    processedWorkloadMap.put(currentEnseignantId, workloadValue);
                    sumOfSpecificWorkloads += workloadValue;
                }

                // Si aucun enseignant n'a été sélectionné (validSelectedEnseignants est vide), sumOfSpecificWorkloads sera 0.
                // Ce cas est géré si totalModuleWorkload est aussi 0.
                if (sumOfSpecificWorkloads != totalModuleWorkload) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "La somme des charges spécifiques (" + sumOfSpecificWorkloads + ") doit être égale à la charge totale du module (" + totalModuleWorkload + " heures).");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }
            } else { // even
                strategy = evenWorkloadAssignmentStrategy;
                if (validSelectedEnseignants.isEmpty() && totalModuleWorkload > 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Aucun enseignant sélectionné à qui assigner la charge de travail uniformément.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }
            }

            // Appeler la stratégie même si validSelectedEnseignants est vide,
            // la stratégie doit gérer ce cas (par exemple, ne rien faire ou lancer une exception si inapproprié)
            strategy.assignWorkload(module, validSelectedEnseignants, processedWorkloadMap);

            List<Utilisateur> finalEnseignantsForModuleList;
            if ("specific".equals(assignmentStrategy)) {
                finalEnseignantsForModuleList = validSelectedEnseignants.stream()
                        .filter(e -> processedWorkloadMap.getOrDefault(e.getId(), 0) > 0)
                        .collect(Collectors.toList());
                if (finalEnseignantsForModuleList.isEmpty() && totalModuleWorkload > 0) {
                    // Ce cas devrait être rare si la somme des charges correspondait au total.
                    // Sauf si toutes les charges étaient 0 et le total > 0, ce qui est une erreur de somme.
                    // Ou si total = 0 et toutes les charges = 0.
                }
            } else {
                finalEnseignantsForModuleList = validSelectedEnseignants;
            }

            Set<Utilisateur> nouveauxEnseignantsAffectes = new HashSet<>(finalEnseignantsForModuleList);
            module.setEnseignants(nouveauxEnseignantsAffectes); // Utiliser le Set
            Module savedModule = moduleRepository.save(module); // Récupérer l'entité sauvegardée pour être sûr

            // --- DÉBUT NOTIFICATION AFFECTATION MODULE ---
            // Notifier les nouveaux ajoutés
            for (Utilisateur enseignant : nouveauxEnseignantsAffectes) {
                if (!anciensEnseignants.contains(enseignant)) {
                    String message = "Vous avez été affecté au module '" + savedModule.getName() +
                            "' dans le département '" + department.getName() + "'.";
                    if ("specific".equals(assignmentStrategy) && processedWorkloadMap.containsKey(enseignant.getId())) {
                        Integer charge = processedWorkloadMap.get(enseignant.getId());
                        if (charge != null && charge > 0) { // Ne mentionner la charge que si > 0
                            message += " Votre charge horaire spécifique est de " + charge + " heures.";
                        }
                    }
                    notificationService.createNotification(
                            enseignant,
                            department,
                            message,
                            NotificationType.MODULE_ASSIGNMENT, // Utiliser un type spécifique si créé
                            null
                    );
                }
            }

            // Notifier ceux qui ont été retirés
            for(Utilisateur ancienEnseignant : anciensEnseignants) {
                if (!nouveauxEnseignantsAffectes.contains(ancienEnseignant)) {
                    String messageRetrait = "Vous avez été retiré du module '" + savedModule.getName() +
                            "' dans le département '" + department.getName() + "'.";
                    notificationService.createNotification(
                            ancienEnseignant,
                            department,
                            messageRetrait,
                            NotificationType.MODULE_UNASSIGNMENT, // Utiliser un type spécifique si créé
                            null
                    );
                }
            }
            // --- FIN NOTIFICATION AFFECTATION MODULE ---

            History history = new History();
            history.setAction("ASSIGN_MODULE");
            history.setEntityType("Module");
            history.setEntityId(savedModule.getId());
            String assignedUsernames = nouveauxEnseignantsAffectes.stream() // Utiliser le Set pour la liste finale
                    .map(u -> u.getFirstName() + " " + u.getLastName())
                    .collect(Collectors.joining(", "));
            if (assignedUsernames.isEmpty()) assignedUsernames = "aucun";
            history.setDetails("Module '" + savedModule.getName() + "' (Dépt: " + department.getName() + ") assigné. Stratégie: " + assignmentStrategy +
                    ". Enseignants affectés: " + assignedUsernames + ".");
            historyRepository.save(history);

            redirectAttributes.addFlashAttribute("successMessage", "Module '" + savedModule.getName() + "' assigné avec succès.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur d'assignation: " + e.getMessage());
            return "redirect:/chef/modules/assign/" + moduleId;
        }
        catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur système lors de l'assignation: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/chef/modules/assign/" + moduleId;
        }
        return "redirect:/chef/modules";
    }

    @GetMapping("/admin/modules/edit/{moduleId}")
    public String showEditModuleForm(@PathVariable Long moduleId, Model model) {
        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new RuntimeException("Module not found"));
        model.addAttribute("module", module);
        model.addAttribute("allEnseignants", utilisateurRepository.findByAppRoles_RoleName("ENSEIGNANT"));
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("pageTitle", "Modifier le Module: " + module.getName());
        return "admin/editModule";
    }

    @PostMapping("/admin/modules/edit/{moduleId}")
    @Transactional
    public String updateModule(@PathVariable Long moduleId, @ModelAttribute Module updatedModuleData, @RequestParam Long departmentId, RedirectAttributes redirectAttributes) {
        try {
            Module moduleToUpdate = moduleRepository.findById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module non trouvé avec ID: " + moduleId));

            moduleToUpdate.setName(updatedModuleData.getName());
            moduleToUpdate.setDescription(updatedModuleData.getDescription());
            moduleToUpdate.setWorkload(updatedModuleData.getWorkload());

            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Département non trouvé avec ID: " + departmentId));
            moduleToUpdate.setDepartment(department);

            // Note: Cette méthode ne modifie pas les affectations d'enseignants.
            // Si l'admin doit pouvoir modifier les affectations, une logique similaire à assignModule serait nécessaire ici.

            moduleRepository.save(moduleToUpdate);

            History history = new History();
            history.setAction("UPDATE");
            history.setEntityType("Module");
            history.setEntityId(moduleToUpdate.getId());
            history.setDetails("Module '" + moduleToUpdate.getName() + "' (ID: " + moduleToUpdate.getId() + ") modifié.");
            historyRepository.save(history);

            redirectAttributes.addFlashAttribute("successMessage", "Module mis à jour avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la mise à jour du module : " + e.getMessage());
            e.printStackTrace(); // Pour le débogage
        }
        return "redirect:/admin/modules";
    }
}