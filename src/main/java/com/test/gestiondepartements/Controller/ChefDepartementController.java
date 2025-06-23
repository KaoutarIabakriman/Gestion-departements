package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Dto.EnseignantWorkloadDTO;
import com.test.gestiondepartements.Entities.*;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.ModuleRequestRepository;
import com.test.gestiondepartements.Repositories.ModuleRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.NotificationService;
import com.test.gestiondepartements.Strategy.EvenWorkloadAssignmentStrategy;
import com.test.gestiondepartements.Strategy.SpecificWorkloadAssignmentStrategy;
import com.test.gestiondepartements.Strategy.WorkloadAssignmentStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/chef")
@RequiredArgsConstructor
public class ChefDepartementController {

    private final UtilisateurRepository utilisateurRepository;
    private final DepartmentRepository departmentRepository;
    private final ModuleRequestRepository moduleRequestRepository;
    private final ModuleRepository moduleRepository;
    private final NotificationService notificationService;
    private final EvenWorkloadAssignmentStrategy evenWorkloadAssignmentStrategy;
    private final SpecificWorkloadAssignmentStrategy specificWorkloadAssignmentStrategy;


    @GetMapping("/enseignants")
    public String listDepartmentEnseignants(Model model, @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request,
                                            @RequestParam(name = "page", defaultValue = "0") int pageNumber,
                                            @RequestParam(name = "size", defaultValue = "10") int pageSize) {

        if (userDetails == null) {
            return "redirect:/login";
        }
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (chef == null) {
            return "redirect:/login?error=UserNotFound";
        }


        Department department = departmentRepository.findByHeadOfDepartment(chef);
        if (department == null) {
            model.addAttribute("errorMessage", "Vous n'êtes pas chef de département.");

            Page<EnseignantWorkloadDTO> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, pageSize), 0);
            model.addAttribute("teacherPage", emptyPage);


            return "chef/enseignants";
        }


        List<Utilisateur> enseignantsDuDepartement = department.getMembers().stream()
                .filter(member -> member.getAppRoles().stream().anyMatch(role -> "ENSEIGNANT".equals(role.getRoleName())))
                .collect(Collectors.toList());


        List<EnseignantWorkloadDTO> enseignantsDTOs = enseignantsDuDepartement.stream().map(enseignant -> {
            int totalWorkload = enseignant.getModules().stream().mapToInt(Module::getWorkload).sum();
            return new EnseignantWorkloadDTO(
                    enseignant.getId(), enseignant.getFirstName(), enseignant.getLastName(), enseignant.getUsername(),
                    Collections.singletonList(department.getName()), totalWorkload, enseignant.getPhone(),
                    enseignant.getEducation(), enseignant.getSkills(), enseignant.getLanguages());
        }).collect(Collectors.toList());


        int start = (int) PageRequest.of(pageNumber, pageSize).getOffset();
        int end = Math.min((start + pageSize), enseignantsDTOs.size());
        List<EnseignantWorkloadDTO> pageContent = (start <= end) ? enseignantsDTOs.subList(start, end) : Collections.emptyList();
        Page<EnseignantWorkloadDTO> teacherPage = new PageImpl<>(pageContent, PageRequest.of(pageNumber, pageSize), enseignantsDTOs.size());



        model.addAttribute("teacherPage", teacherPage);
        model.addAttribute("department", department);
        model.addAttribute("currentURI", request.getRequestURI());

        return "chef/enseignants";
    }


    @GetMapping("/dashboard")
    public String chefDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        return "redirect:/chef/demandes";
    }


    @GetMapping("/modules")
    public String chefModules(Model model, @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "10") int size,
                              @RequestParam(name = "searchTerm", required = false) String searchTerm) {

        if (userDetails == null) { return "redirect:/login"; }
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (chef == null) {
            model.addAttribute("errorMessage", "Utilisateur non trouvé.");
            return "error";
        }

        Department department = departmentRepository.findByHeadOfDepartment(chef);
        if (department == null) {
            model.addAttribute("errorMessage", "Vous n'êtes pas chef d'un département.");
            return "error";
        }

        List<Module> allModulesForDepartment;
        try {
            allModulesForDepartment = (List<Module>) moduleRepository.findByDepartment(department);
        } catch (Exception e) {
            Pageable fetchAllPageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Module> tempPage = (Page<Module>) moduleRepository.findByDepartment(department);
            allModulesForDepartment = new ArrayList<>(tempPage.getContent());
            System.err.println("WARN: Fetched all modules for department " + department.getName() + " using a large page request. Consider adding `List<Module> findByDepartment(Department department)` to ModuleRepository for efficiency.");
        }

        List<Module> filteredModules = new ArrayList<>(allModulesForDepartment);

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String lowerSearchTerm = searchTerm.toLowerCase().trim();
            filteredModules = filteredModules.stream()
                    .filter(module -> (module.getName() != null && module.getName().toLowerCase().contains(lowerSearchTerm)) ||
                            (module.getDescription() != null && module.getDescription().toLowerCase().contains(lowerSearchTerm)))
                    .collect(Collectors.toList());
        }

        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredModules.size());
        List<Module> pageContent = Collections.emptyList();
        if (start <= end && start < filteredModules.size()) {
            pageContent = filteredModules.subList(start, end);
        }

        Page<Module> modulePage = new PageImpl<>(pageContent, pageable, filteredModules.size());

        model.addAttribute("modulePage", modulePage);
        model.addAttribute("modules", modulePage.getContent());
        model.addAttribute("currentURI", request.getRequestURI());
        model.addAttribute("pageTitle", "Modules du Département: " + department.getName()); // More specific title

        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("currentSearchTerm", searchTerm);
        model.addAttribute("totalPages", modulePage.getTotalPages());
        model.addAttribute("totalItems", modulePage.getTotalElements());

        return "chef/modules";
    }


    @GetMapping("/modules/assign/{moduleId}")
    public String showAssignModuleForm(@PathVariable Long moduleId, Model model,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       HttpServletRequest request) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (chef == null) {
            return "redirect:/error";
        }
        Department chefDepartment = departmentRepository.findByHeadOfDepartment(chef);
        if (chefDepartment == null) {
            model.addAttribute("errorMessage", "Vous n'êtes pas chef d'un département.");
            return "redirect:/chef/modules";
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module introuvable: " + moduleId));

        if (!module.getDepartment().equals(chefDepartment)) {
            model.addAttribute("errorMessage", "Ce module n'appartient pas à votre département.");
            return "redirect:/chef/modules";
        }

        List<Utilisateur> enseignants = chefDepartment.getMembers().stream()
                .filter(member -> member.getAppRoles().stream().anyMatch(role -> "ENSEIGNANT".equals(role.getRoleName())))
                .collect(Collectors.toList());

        Set<Long> assignedEnseignantIds = module.getEnseignants().stream().map(Utilisateur::getId).collect(Collectors.toSet());
        List<Long> requestingEnseignantIds = moduleRequestRepository.findEnseignantIdsByModuleAndStatus(module, ModuleRequestStatus.PENDING);
        List<Long> approvedEnseignantIds = moduleRequestRepository.findEnseignantIdsByModuleAndStatus(module, ModuleRequestStatus.APPROVED);

        model.addAttribute("module", module);
        model.addAttribute("enseignants", enseignants);
        model.addAttribute("assignedEnseignantIds", assignedEnseignantIds);
        model.addAttribute("requestingEnseignantIds", requestingEnseignantIds);
        model.addAttribute("approvedEnseignantIds", approvedEnseignantIds);
        model.addAttribute("pageTitle", "Affecter Module: " + module.getName());

        model.addAttribute("currentURI", request.getRequestURI());

        return "chef/assignModule";
    }


    @PostMapping("/modules/assign/{moduleId}")
    @Transactional
    public String assignModule(@PathVariable Long moduleId,
                               @RequestParam(required = false) List<Long> enseignantIds,
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
                    .orElseThrow(() -> new RuntimeException("Module non trouvé"));

            if (!module.getDepartment().equals(department)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ce module n'appartient pas à votre département.");
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
                    redirectAttributes.addFlashAttribute("errorMessage", "Veuillez sélectionner au moins un enseignant pour la répartition égale si le module a une charge horaire.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }
            }

            WorkloadAssignmentStrategy strategy;
            int totalModuleWorkload = module.getWorkload();
            Map<Long, Integer> processedWorkloadMap = new HashMap<>();

            if ("specific".equals(assignmentStrategy)) {
                strategy = specificWorkloadAssignmentStrategy;

                if (allRequestParams == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Aucun paramétre de formulaire reçu.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }

                if (validSelectedEnseignants.isEmpty() && totalModuleWorkload > 0) {
                    // This check is now primarily handled by JS, but good to have a server-side fallback.
                    redirectAttributes.addFlashAttribute("errorMessage", "Pour la répartition manuelle, veuillez sélectionner des enseignants.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }


                int sumOfSpecificWorkloads = 0;
                for (Utilisateur selectedEnseignant : validSelectedEnseignants) {
                    Long currentEnseignantId = selectedEnseignant.getId();
                    // paramValue can be the teacher's ID or a specific input name if you change the JS
                    String paramValue = allRequestParams.get(String.valueOf(currentEnseignantId));
                    Integer workloadValue;

                    if (paramValue == null || paramValue.trim().isEmpty()) {
                        if (totalModuleWorkload > 0) { // Only error if module has workload
                            redirectAttributes.addFlashAttribute("errorMessage", "La charge de travail pour l'enseignant sélectionné " + selectedEnseignant.getFirstName() + " " + selectedEnseignant.getLastName() + " (ID: " + currentEnseignantId + ") doit être spécifiée.");
                            return "redirect:/chef/modules/assign/" + moduleId;
                        } else {
                            workloadValue = 0; // If module workload is 0, assign 0
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

                if (sumOfSpecificWorkloads != totalModuleWorkload) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "La somme des charges spécifiques (" + sumOfSpecificWorkloads + ") doit être égale à la charge totale du module (" + totalModuleWorkload + " heures).");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }
            } else { // "even" or default
                strategy = evenWorkloadAssignmentStrategy;
                if (validSelectedEnseignants.isEmpty() && totalModuleWorkload > 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Aucun enseignant sélectionné à qui assigner la charge de travail uniformément.");
                    return "redirect:/chef/modules/assign/" + moduleId;
                }
            }

            strategy.assignWorkload(module, validSelectedEnseignants, processedWorkloadMap);

            List<Utilisateur> finalEnseignantsForModuleList;
            if("specific".equals(assignmentStrategy)){
                // Only include teachers who actually got assigned a workload > 0 or if module workload is 0
                finalEnseignantsForModuleList = validSelectedEnseignants.stream()
                        .filter(e -> processedWorkloadMap.getOrDefault(e.getId(), 0) > 0 || totalModuleWorkload == 0)
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
                    req.setStatus(ModuleRequestStatus.REJECTED);
                }
                moduleRequestRepository.save(req);
            }

            for (Utilisateur enseignant : nouveauxEnseignantsAffectes) {
                if(!anciensEnseignants.contains(enseignant)) {
                    String message = "Vous avez été affecté au module '" + savedModule.getName() +
                            "' dans le département '" + department.getName() + "'.";
                    notificationService.createNotification(enseignant, department, savedModule, message, NotificationType.MODULE_ASSIGNMENT, null);
                }
            }

            for(Utilisateur ancienEnseignant : anciensEnseignants) {
                if(!nouveauxEnseignantsAffectes.contains(ancienEnseignant)) {
                    String messageRetrait = "Vous avez été retiré du module '" + savedModule.getName() +
                            "' dans le département '" + department.getName() + "'.";
                    notificationService.createNotification(ancienEnseignant, department, savedModule, messageRetrait, NotificationType.MODULE_UNASSIGNMENT, null);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Module '" + savedModule.getName() + "' assigné avec succès.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur d'assignation: " + e.getMessage());
            return "redirect:/chef/modules/assign/" + moduleId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur système lors de l'assignation: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/chef/modules/assign/" + moduleId;
        }
        return "redirect:/chef/modules";
    }

    @GetMapping("/demandes")
    public String showDemandesPage(Model model, @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request,
                                   @RequestParam(value = "keyword", required = false) String keyword,
                                   @RequestParam(value = "status", required = false) ModuleRequestStatus status,
                                   @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDateTime startDate,
                                   @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDateTime endDate,
                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                   @RequestParam(name = "size", defaultValue = "10") int size) {


        if (userDetails == null) { return "redirect:/login"; }
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (chef == null) {return "error"; }


        Department department = departmentRepository.findByHeadOfDepartment(chef);
        if (department == null) {
            model.addAttribute("errorMessage", "Non autorisé.");


        } else {
            Pageable pageable = PageRequest.of(page, size);

            Page<ModuleRequest> demandesPage = moduleRequestRepository.findAllByCriteria(department, keyword, status, startDate, endDate, pageable);


            model.addAttribute("demandesParModule", demandesPage.getContent().stream()

                    .collect(Collectors.groupingBy(ModuleRequest::getModule)));
            model.addAttribute("demandesPage", demandesPage);

        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate != null ? startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        model.addAttribute("endDate", endDate != null ? endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        model.addAttribute("pageTitle", "Gestion des Demandes de Modules");


        model.addAttribute("currentURI", request.getRequestURI());


        return "chef/demandes";
    }


    @PostMapping("/demandes/approve/{requestId}")
    public String approveDemande(@PathVariable Long requestId, @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        // Basic implementation for approval
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        ModuleRequest demande = moduleRequestRepository.findById(requestId).orElse(null);

        if (demande != null && demande.getModule().getDepartment().getHeadOfDepartment().equals(chef)) {
            demande.setStatus(ModuleRequestStatus.APPROVED);
            demande.setApprovedBy(chef);
            demande.setApprovedAt(LocalDateTime.now());
            moduleRequestRepository.save(demande);
            // Optionally, auto-assign module if not already? Or just approve the request.
            // For now, just approving the request. Further logic for auto-assignment can be complex.
            redirectAttributes.addFlashAttribute("successMessage", "Demande approuvée.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Action non autorisée ou demande introuvable.");
        }
        return "redirect:/chef/demandes";
    }


    @PostMapping("/demandes/reject/{requestId}")
    public String rejectDemande(@PathVariable Long requestId, @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        // Basic implementation for rejection
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        ModuleRequest demande = moduleRequestRepository.findById(requestId).orElse(null);

        if (demande != null && demande.getModule().getDepartment().getHeadOfDepartment().equals(chef)) {
            demande.setStatus(ModuleRequestStatus.REJECTED);
            demande.setRejectedBy(chef);
            demande.setRejectedAt(LocalDateTime.now());
            moduleRequestRepository.save(demande);
            redirectAttributes.addFlashAttribute("successMessage", "Demande rejetée.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Action non autorisée ou demande introuvable.");
        }
        return "redirect:/chef/demandes";
    }
}