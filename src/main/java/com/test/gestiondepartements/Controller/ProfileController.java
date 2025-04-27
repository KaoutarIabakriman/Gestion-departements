package com.test.gestiondepartements.Controller;


import com.test.gestiondepartements.Dto.ProfileDTO;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/enseignant/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    public String showProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        Utilisateur user = utilisateurRepository.findByUsername(username);
        ProfileDTO profile = profileService.getProfileById(user.getId());
        model.addAttribute("profileDTO", profile);
        return "enseignant/profile";
    }

    @PostMapping
    public String updateProfile(@ModelAttribute @Valid ProfileDTO profileDTO,
                                BindingResult result,
                                @AuthenticationPrincipal UserDetails userDetails) {
        if (result.hasErrors()) {
            return "enseignant/profile";
        }

        String username = userDetails.getUsername();
        Utilisateur user = utilisateurRepository.findByUsername(username);
        profileDTO.setId(user.getId());

        profileService.updateProfile(profileDTO);
        return "redirect:/enseignant/profile?success";
    }
}