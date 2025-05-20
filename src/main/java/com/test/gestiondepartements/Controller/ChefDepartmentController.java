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
        Department department = departmentRepository.findByHeadOfDepartment(chef);

        List<Utilisateur> enseignants = Collections.emptyList();
        String departmentName = "N/A";

        if (department != null) {
            // Assuming 'members' are the enseignants of this department
            // Filter out the HoD themselves if they are also listed as a member and you only want other teachers
            // For simplicity, we'll list all members here. Adjust if needed.
            enseignants = department.getMembers();
            departmentName = department.getName();
        } else {
            model.addAttribute("errorMessage", "Vous n'êtes chef d'aucun département actuellement.");
        }

        model.addAttribute("enseignants", enseignants);
        model.addAttribute("departmentName", departmentName);
        model.addAttribute("pageTitle", "Enseignants du Département");
        return "chef/enseignants"; // Path to the new HTML file
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