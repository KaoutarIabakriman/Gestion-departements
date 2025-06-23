package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Dto.ProfileDTO;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Repositories.NotificationRepository;
import com.test.gestiondepartements.Service.ProfileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Set;

@Controller
@RequestMapping("/enseignant/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationRepository notificationRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String userProfile(Model model,
                              @AuthenticationPrincipal UserDetails userDetails,
                              HttpServletRequest request) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Utilisateur currentUser = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            model.addAttribute("error", "Utilisateur non trouvé.");

            return "enseignant/profile";
        }

        ProfileDTO profileDTO = profileService.getProfileById(currentUser.getId());
        model.addAttribute("profileDTO", profileDTO);
        model.addAttribute("currentURI", request.getRequestURI());

        Set<Module> taughtModules = currentUser.getModules();
        model.addAttribute("taughtModules", taughtModules != null ? taughtModules : new ArrayList<Module>());

        long unreadCount = notificationRepository.countByUserAndReadStatusFalse(currentUser);
        model.addAttribute("unreadCount", unreadCount);

        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }
        if (model.containsAttribute("error")) {
            model.addAttribute("error", model.getAttribute("error"));
        }

        return "enseignant/profile";
    }

    @PostMapping
    @Transactional
    public String updateUserProfile(@Valid @ModelAttribute("profileDTO") ProfileDTO profileDTO,
                                    BindingResult result,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes,
                                    HttpServletRequest request) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Utilisateur authUser = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (authUser == null) {
            redirectAttributes.addFlashAttribute("error", "Session invalide ou utilisateur non trouvé.");
            return "redirect:/login";
        }

        profileDTO.setId(authUser.getId());

        if (result.hasErrors()) {

            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.profileDTO", result);
            redirectAttributes.addFlashAttribute("profileDTO", profileDTO);
            redirectAttributes.addFlashAttribute("error", "Veuillez corriger les erreurs indiquées.");
            return "redirect:/enseignant/profile";
        }

        try {
            profileService.updateProfile(profileDTO);

            redirectAttributes.addFlashAttribute("successMessage", "Profil mis à jour avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Une erreur technique est survenue lors de la mise à jour du profil.");
        }
        return "redirect:/enseignant/profile";
    }
}