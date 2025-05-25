package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Entities.Notification;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.EnseignantModuleService;
import com.test.gestiondepartements.Service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
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

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/enseignant/modules")
@RequiredArgsConstructor
public class EnseignantController {

    private final EnseignantModuleService enseignantModuleService;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    @PostMapping("/request/{moduleId}")
    public String requestModuleTeaching(@PathVariable Long moduleId,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        RedirectAttributes redirectAttributes) {
        Utilisateur enseignant = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (enseignant == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non authentifi??.");
            return "redirect:/login";
        }

        try {
            enseignantModuleService.requestModule(moduleId, enseignant);
            redirectAttributes.addFlashAttribute("successMessage", "Votre demande pour enseigner le module a ??t?? soumise avec succ??s.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("warningMessage", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la soumission de la demande : " + e.getMessage());
        }
        return "redirect:/enseignant/notifications";
    }

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String enseignantDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails, Authentication authentication) {
        String username = userDetails.getUsername();
        Utilisateur enseignant = utilisateurRepository.findByUsername(username);

        if (enseignant == null) {
            return "redirect:/login";
        }

        Set<Module> modules = enseignant.getModules();

        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(enseignant);

        model.addAttribute("enseignant", enseignant);
        model.addAttribute("modules", modules);
        model.addAttribute("unreadNotificationsCount", unreadNotifications.size());
        model.addAttribute("pageTitle", "Tableau de bord enseignant");

        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("authenticatedUsername", authentication.getName());
        }

        return "layout/dashboard";
    }
}