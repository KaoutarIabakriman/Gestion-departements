package com.test.gestiondepartements.Controller;// Add these imports if not already present
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List; // Make sure this is imported
import java.util.stream.Collectors;

// ... other imports

@Controller
@RequestMapping("/chef")
public class ChefDepartmentController {

    private final DepartmentRepository departmentRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ChefDepartmentController(DepartmentRepository departmentRepository, UtilisateurRepository utilisateurRepository) {
        this.departmentRepository = departmentRepository;
        this.utilisateurRepository = utilisateurRepository;
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

    // NEW METHOD
    @GetMapping("/enseignants")
    public String listDepartmentEnseignants(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());

        if (chef == null) { // Handle the case where the chef is not found
            model.addAttribute("errorMessage", "Utilisateur non trouvé.");
            return "error"; // Or some error page
        }

        Department department = departmentRepository.findByHeadOfDepartment(chef);


        if (department == null) {
            model.addAttribute("errorMessage", "Vous n'êtes chef d'aucun département actuellement.");
            return "chef/enseignants"; // Return even if no department so the message can be displayed
        }

        List<Utilisateur> enseignants = department.getMembers().stream()
                .filter(member -> !member.getId().equals(chef.getId())) // Efficiently filter out the HoD
                .collect(Collectors.toList());


        model.addAttribute("department", department); // Absolutely essential!
        model.addAttribute("enseignants", enseignants);
        model.addAttribute("pageTitle", "Enseignants du Département");
        return "chef/enseignants";
    }
    // Autres méthodes pour gérer les modules, les demandes, etc.
    // Placeholder for modules
    @GetMapping("/modules")
    public String listDepartmentModules(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Implement logic to fetch modules for the HoD's department
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        Department department = departmentRepository.findByHeadOfDepartment(chef);
        model.addAttribute("department", department);
        model.addAttribute("pageTitle", "Modules du Département");
        // model.addAttribute("modules", ...);
        return "chef/modules"; // Create chef/modules.html
    }

    // Placeholder for demandes
    @GetMapping("/demandes")
    public String listDepartmentDemandes(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Implement logic to fetch demandes for the HoD's department
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        Department department = departmentRepository.findByHeadOfDepartment(chef);
        model.addAttribute("department", department);
        model.addAttribute("pageTitle", "Demandes du Département");
        // model.addAttribute("demandes", ...);
        return "chef/demandes"; // Create chef/demandes.html
    }
}