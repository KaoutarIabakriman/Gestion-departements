package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.*;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.ModuleRepository;
import com.test.gestiondepartements.Repositories.ModuleRequestRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.NotificationService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/chef")
public class ChefDepartmentController {

    private final DepartmentRepository departmentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleRequestRepository moduleRequestRepository;
    private final NotificationService notificationService;

    public ChefDepartmentController(DepartmentRepository departmentRepository,
                                    UtilisateurRepository utilisateurRepository,
                                    ModuleRepository moduleRepository,
                                    ModuleRequestRepository moduleRequestRepository,
                                    NotificationService notificationService) {
        this.departmentRepository = departmentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.moduleRepository = moduleRepository;
        this.moduleRequestRepository = moduleRequestRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        Department department = departmentRepository.findByHeadOfDepartment(chef);

        model.addAttribute("department", department);
        model.addAttribute("members", department != null ? department.getMembers() : Collections.emptyList());
        model.addAttribute("pageTitle", "Tableau de Bord Chef");


        return "chef/dashboard";
    }

    @GetMapping("/enseignants")
    public String listDepartmentEnseignants(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());

        if (chef == null) {
            model.addAttribute("errorMessage", "Utilisateur non trouvé.");
            return "error";
        }

        Department department = departmentRepository.findByHeadOfDepartment(chef);


        if (department == null) {
            model.addAttribute("errorMessage", "Vous n'êtes chef d'aucun département actuellement.");
            model.addAttribute("enseignants", Collections.emptyList());
            return "chef/enseignants";
        }

        List<Utilisateur> enseignants = department.getMembers().stream()
                .filter(member -> member.getAppRoles().stream().anyMatch(role -> "ENSEIGNANT".equals(role.getRoleName())))
                .filter(member -> !member.getId().equals(chef.getId())) // Exclude the HOD themselves
                .collect(Collectors.toList());


        model.addAttribute("department", department);
        model.addAttribute("enseignants", enseignants);
        model.addAttribute("pageTitle", "Enseignants du Département");
        return "chef/enseignants";
    }


    @GetMapping("/modules")
    public String listDepartmentModules(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        Department department = departmentRepository.findByHeadOfDepartment(chef);

        if (department != null) {
            List<Module> modules = moduleRepository.findByDepartment(department);
            model.addAttribute("modules", modules);
            List<Utilisateur> enseignantsPourAffectation = department.getMembers().stream()
                    .filter(member -> member.getAppRoles().stream()
                            .anyMatch(role -> "ENSEIGNANT".equals(role.getRoleName())))
                    .filter(member -> member.getFirstName() != null && !member.getFirstName().isEmpty() &&
                            member.getLastName() != null && !member.getLastName().isEmpty())
                    .filter(member -> !member.getId().equals(chef.getId()))
                    .collect(Collectors.toList());
            model.addAttribute("enseignants", enseignantsPourAffectation);
        } else {
            model.addAttribute("modules", Collections.emptyList());
            model.addAttribute("enseignants", Collections.emptyList());
        }
        return "chef/modules";
    }


    @GetMapping("/modules/assign/{moduleId}")
    public String showAssignModuleForm(@PathVariable Long moduleId,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       Model model) {
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        Department department = departmentRepository.findByHeadOfDepartment(chef);

        if (department == null) {
            model.addAttribute("errorMessage", "Vous n'êtes pas actuellement chef d'un département.");
            return "error";
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec ID: " + moduleId));

        if (!module.getDepartment().equals(department)) {
            model.addAttribute("errorMessage", "Ce module n'appartient pas à votre département.");
            return "redirect:/chef/modules";
        }

        List<Utilisateur> enseignantsDuDepartement = department.getMembers().stream()
                .filter(member -> member.getAppRoles().stream()
                        .anyMatch(role -> "ENSEIGNANT".equals(role.getRoleName())))
                .filter(member -> member.getFirstName() != null && !member.getFirstName().isEmpty() &&
                        member.getLastName() != null && !member.getLastName().isEmpty())
                .filter(member -> !member.getId().equals(chef.getId()))
                .collect(Collectors.toList());

        Set<Long> assignedEnseignantIds = module.getEnseignants().stream()
                .map(Utilisateur::getId)
                .collect(Collectors.toSet());

        List<ModuleRequest> pendingRequestsForThisModule = moduleRequestRepository.findByModuleAndStatus(module, ModuleRequestStatus.PENDING);
        Set<Long> requestingEnseignantIds = pendingRequestsForThisModule.stream()
                .map(request -> request.getEnseignant().getId())
                .collect(Collectors.toSet());

        List<ModuleRequest> approvedRequestsForThisModule = moduleRequestRepository.findByModuleAndStatus(module, ModuleRequestStatus.APPROVED);
        Set<Long> approvedEnseignantIds = approvedRequestsForThisModule.stream()
                .map(request -> request.getEnseignant().getId())
                .collect(Collectors.toSet());

        model.addAttribute("module", module);
        model.addAttribute("enseignants", enseignantsDuDepartement);
        model.addAttribute("assignedEnseignantIds", assignedEnseignantIds);
        model.addAttribute("requestingEnseignantIds", requestingEnseignantIds);
        model.addAttribute("approvedEnseignantIds", approvedEnseignantIds);


        return "chef/assignModule";
    }




    @GetMapping("/demandes")
    public String listDepartmentDemandes(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        Department department = departmentRepository.findByHeadOfDepartment(chef);

        if (department != null) {
            List<ModuleRequest> allRequests = moduleRequestRepository.findByDepartmentIdWithDetails(department.getId());
            Map<Module, List<ModuleRequest>> demandesParModule = allRequests.stream()
                    .collect(Collectors.groupingBy(ModuleRequest::getModule));
            model.addAttribute("demandesParModule", demandesParModule);
        } else {
            model.addAttribute("demandesParModule", Collections.emptyMap());
        }

        model.addAttribute("department", department);
        model.addAttribute("pageTitle", "Demandes d'Affectation de Modules");
        return "chef/demandes";
    }

    @PostMapping("/demandes/approve/{requestId}")
    @Transactional
    public String approveModuleRequest(@PathVariable Long requestId,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       RedirectAttributes redirectAttributes) {
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        ModuleRequest request = moduleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée: " + requestId));

        if (request.getModule().getDepartment().getHeadOfDepartment() == null ||
                !request.getModule().getDepartment().getHeadOfDepartment().getId().equals(chef.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Action non autorisée.");
            return "redirect:/chef/demandes";
        }

        if (request.getStatus() == ModuleRequestStatus.PENDING) {
            request.setStatus(ModuleRequestStatus.APPROVED);
            moduleRequestRepository.save(request);

            notificationService.createNotification(
                    request.getEnseignant(),
                    request.getModule().getDepartment(),
                    request.getModule(),
                    "Votre demande pour enseigner le module '" + request.getModule().getName() + "' a été APPROUVÉE par le chef de département.",
                    NotificationType.GENERAL,
                    null
            );
            redirectAttributes.addFlashAttribute("successMessage", "Demande approuvée pour " + request.getEnseignant().getFirstName() + " concernant le module " + request.getModule().getName() + ".");
        } else {
            redirectAttributes.addFlashAttribute("warningMessage", "Cette demande n'est plus en attente.");
        }
        return "redirect:/chef/demandes";
    }

    @PostMapping("/demandes/reject/{requestId}")
    @Transactional
    public String rejectModuleRequest(@PathVariable Long requestId,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        ModuleRequest request = moduleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée: " + requestId));

        if (request.getModule().getDepartment().getHeadOfDepartment() == null ||
                !request.getModule().getDepartment().getHeadOfDepartment().getId().equals(chef.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Action non autorisée.");
            return "redirect:/chef/demandes";
        }

        if (request.getStatus() == ModuleRequestStatus.PENDING) {
            request.setStatus(ModuleRequestStatus.REJECTED);
            moduleRequestRepository.save(request);

            notificationService.createNotification(
                    request.getEnseignant(),
                    request.getModule().getDepartment(),
                    request.getModule(),
                    "Votre demande pour enseigner le module '" + request.getModule().getName() + "' a été REJETÉE par le chef de département.",
                    NotificationType.GENERAL, // Or a new specific type like MODULE_REQUEST_REJECTED
                    null
            );
            redirectAttributes.addFlashAttribute("successMessage", "Demande rejetée pour " + request.getEnseignant().getFirstName() + " concernant le module " + request.getModule().getName() + ".");
        } else {
            redirectAttributes.addFlashAttribute("warningMessage", "Cette demande n'est plus en attente.");
        }
        return "redirect:/chef/demandes";
    }
}