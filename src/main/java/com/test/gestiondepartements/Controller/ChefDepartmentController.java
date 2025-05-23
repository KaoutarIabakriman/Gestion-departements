package com.test.gestiondepartements.Controller;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.ModuleRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/chef")
public class ChefDepartmentController {

    private final DepartmentRepository departmentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ModuleRepository moduleRepository;

    public ChefDepartmentController(DepartmentRepository departmentRepository, UtilisateurRepository utilisateurRepository, ModuleRepository moduleRepository) {
        this.departmentRepository = departmentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.moduleRepository = moduleRepository;
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
            return "chef/enseignants";
        }

        List<Utilisateur> enseignants = department.getMembers().stream()
                .filter(member -> !member.getId().equals(chef.getId()))
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
            List<Utilisateur> enseignants = department.getMembers().stream()
                    .filter(member -> {
                        if (member.getAppRoles() == null) return false;
                        return member.getAppRoles().stream()
                                .anyMatch(role -> "ENSEIGNANT".equals(role.getRoleName()));
                    })
                    .filter(member ->
                            member.getFirstName() != null &&
                                    !member.getFirstName().isEmpty() &&
                                    member.getLastName() != null &&
                                    !member.getLastName().isEmpty()
                    )
                    .filter(member ->
                            !member.getId().equals(chef.getId())
                    )
                    .collect(Collectors.toList());

            model.addAttribute("enseignants", enseignants);
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

        model.addAttribute("module", module);
        model.addAttribute("enseignants", enseignantsDuDepartement);
        model.addAttribute("assignedEnseignantIds", assignedEnseignantIds);
        return "chef/assignModule";
    }

    @GetMapping("/demandes")
    public String listDepartmentDemandes(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        Department department = departmentRepository.findByHeadOfDepartment(chef);
        model.addAttribute("department", department);
        model.addAttribute("pageTitle", "Demandes du Département");
        return "chef/demandes";
    }





}