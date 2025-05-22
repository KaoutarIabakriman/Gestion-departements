package com.test.gestiondepartements.Controller;// Add these imports if not already present
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
import java.util.List; // Make sure this is imported
import java.util.Set;
import java.util.stream.Collectors;

// ... other imports

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
// Dans ChefDepartmentController.java
    @GetMapping("/modules")
    public String listDepartmentModules(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        Department department = departmentRepository.findByHeadOfDepartment(chef);

        if (department != null) {
            List<Module> modules = moduleRepository.findByDepartment(department);
            model.addAttribute("modules", modules);

            // Filtrer les enseignants valides du département
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
    }    @GetMapping("/demandes")
    public String listDepartmentDemandes(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: Implement logic to fetch demandes for the HoD's department
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        Department department = departmentRepository.findByHeadOfDepartment(chef);
        model.addAttribute("department", department);
        model.addAttribute("pageTitle", "Demandes du Département");
        // model.addAttribute("demandes", ...);
        return "chef/demandes"; // Create chef/demandes.html
    }

    // Dans ChefDepartmentController.java


    @GetMapping("/modules/assign/{moduleId}") // Assurez-vous que c'est le bon mapping pour afficher le formulaire
    public String showAssignModuleForm(@PathVariable Long moduleId,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       Model model) {
        Utilisateur chef = utilisateurRepository.findByUsername(userDetails.getUsername());
        Department department = departmentRepository.findByHeadOfDepartment(chef);

        if (department == null) {
            // Gérer le cas où l'utilisateur n'est pas chef ou le département n'est pas trouvé
            model.addAttribute("errorMessage", "Vous n'êtes pas actuellement chef d'un département valide.");
            return "error"; // ou une page d'erreur appropriée
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouv?? avec ID: " + moduleId));

        // Vérifier que le module appartient bien au département du chef
        if (!module.getDepartment().equals(department)) {
            model.addAttribute("errorMessage", "Ce module n'appartient pas à votre département.");
            // Potentiellement rediriger ou afficher une erreur. Pour l'instant, on continue pour afficher le formulaire avec l'erreur.
            // Rediriger pourrait être mieux :
            // redirectAttributes.addFlashAttribute("errorMessage", "Ce module n'appartient pas à votre département.");
            // return "redirect:/chef/modules";
        }

        // Récupérer les enseignants du département du chef
        List<Utilisateur> enseignantsDuDepartement = department.getMembers().stream()
                .filter(member -> member.getAppRoles().stream()
                        .anyMatch(role -> "ENSEIGNANT".equals(role.getRoleName())))
                .filter(member -> member.getFirstName() != null && !member.getFirstName().isEmpty() &&
                        member.getLastName() != null && !member.getLastName().isEmpty())
                .filter(member -> !member.getId().equals(chef.getId())) // Exclure le chef lui-même
                .collect(Collectors.toList());

        // PRÉ-CALCUL: Obtenir les IDs des enseignants déjà assignés à ce module
        Set<Long> assignedEnseignantIds = module.getEnseignants().stream()
                .map(Utilisateur::getId)
                .collect(Collectors.toSet());

        model.addAttribute("module", module);
        model.addAttribute("enseignants", enseignantsDuDepartement); // Renommé pour clarté, ou gardez "enseignants" si vous préférez
        model.addAttribute("assignedEnseignantIds", assignedEnseignantIds); // PASSER LES IDs AU MODÈLE

        // Pour pré-remplir la stratégie (si nécessaire, sinon Thymeleaf peut gérer le premier radio par défaut)
        // model.addAttribute("assignmentStrategy", "even"); // ou la stratégie actuelle si en mode édition

        return "chef/assignModule"; // Le nom de votre template Thymeleaf
    }

}