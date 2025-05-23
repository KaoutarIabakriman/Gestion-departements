// File: src/main/java/com/test/gestiondepartements/Controller/EnseignantController.java (New or existing)
package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.EnseignantModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/enseignant/modules")
@RequiredArgsConstructor
public class EnseignantController {

    private final EnseignantModuleService enseignantModuleService;
    private final UtilisateurRepository utilisateurRepository;

    @PostMapping("/request/{moduleId}")
    public String requestModuleTeaching(@PathVariable Long moduleId,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        RedirectAttributes redirectAttributes) {
        Utilisateur enseignant = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (enseignant == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non authentifié.");
            return "redirect:/login";
        }

        try {
            enseignantModuleService.requestModule(moduleId, enseignant);
            redirectAttributes.addFlashAttribute("successMessage", "Votre demande pour enseigner le module a été soumise avec succès.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("warningMessage", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la soumission de la demande : " + e.getMessage());
        }
        return "redirect:/enseignant/notifications"; // Or back to a module list page
    }
}