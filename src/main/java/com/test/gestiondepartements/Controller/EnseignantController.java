package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.*;
import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Repositories.*;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.EnseignantModuleService;
import com.test.gestiondepartements.Service.NotificationService;

import com.test.gestiondepartements.Service.VoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/enseignant")
@RequiredArgsConstructor
public class EnseignantController {

    private final EnseignantModuleService enseignantModuleService;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final VoteRepository voteRepository;
    private final CandidateRepository candidateRepository;
    private final VoteChoiceRepository voteChoiceRepository;
    private final VoteService voteService;
    private final ModuleRepository moduleRepository;
    private final ModuleRequestRepository moduleRequestRepository;
    @PostMapping("/modules/request/{moduleId}")
    @Transactional
    public String requestModuleTeaching(@PathVariable Long moduleId,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Utilisateur enseignant = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (enseignant == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non authentifié.");
            return "redirect:/login";
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));


        Department department = module.getDepartment();
        if (department == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le module n'est associé à aucun département.");
            return "redirect:/enseignant/notifications";
        }


        if (moduleRequestRepository.existsByModuleAndEnseignantAndStatus(module, enseignant, ModuleRequestStatus.PENDING)) {
            redirectAttributes.addFlashAttribute("warningMessage", "Vous avez déjà soumis une demande pour ce module.");
            return "redirect:/enseignant/notifications";
        }

        try {
            ModuleRequest request = new ModuleRequest(module, enseignant);
            moduleRequestRepository.save(request);

            Utilisateur departmentHead = department.getHeadOfDepartment();
            if (departmentHead != null) {
                notificationService.createNotification(
                        departmentHead,
                        department,
                        module,
                        "L'enseignant " + enseignant.getUsername() + " a demandé à enseigner le module " + module.getName() + ".",
                        NotificationType.MODULE_REQUEST_PENDING_HOD,
                        null
                );

                notificationService.createNotification(
                        enseignant,
                        department,
                        module,
                        "Votre demande d'enseignement pour le module " + module.getName() + " a été soumise avec succès.",
                        NotificationType.MODULE_REQUEST_SUBMITTED,
                        null
                );
            } else {

                redirectAttributes.addFlashAttribute("warningMessage", "Votre demande a été soumise, mais aucun chef de département n'est actuellement assigné.  Un administrateur traitera votre demande ultérieurement.");

                return "redirect:/enseignant/notifications";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Votre demande a été soumise avec succès.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la soumission de la demande: " + e.getMessage());

            e.printStackTrace();
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
    @PostMapping("/vote/declareCandidacy/{voteId}")
    @Transactional
    public String declareCandidacy(@PathVariable Long voteId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }
        Utilisateur enseignant = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (enseignant == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé.");
            return "redirect:/login";
        }

        Vote vote = voteRepository.findById(voteId)
                .orElse(null);

        if (vote == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vote non trouvé.");
            return "redirect:/enseignant/notifications";
        }


        if (vote.getStatus() != VoteStatus.ACTIVE || (vote.getEndDate() != null && LocalDateTime.now().isAfter(vote.getEndDate()))) {
            redirectAttributes.addFlashAttribute("warningMessage", "Ce vote n'est plus actif ou est terminé. Impossible de déclarer une candidature.");
            return "redirect:/enseignant/vote/" + voteId;
        }

        if (vote.getDepartment() == null || !vote.getDepartment().getMembers().contains(enseignant)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vous n'êtes pas autorisé à être candidat pour ce vote (non membre du département).");
            return "redirect:/enseignant/vote/" + voteId;
        }

        boolean alreadyCandidate = vote.getCandidates().stream().anyMatch(c -> c.getUser().equals(enseignant));

        if (candidateRepository.existsByVoteAndUser(vote, enseignant)) {
            redirectAttributes.addFlashAttribute("infoMessage", "Vous êtes déjà candidat pour ce vote.");
            return "redirect:/enseignant/vote/" + voteId;
        }

        Candidate newCandidacy = new Candidate();
        newCandidacy.setVote(vote);
        newCandidacy.setUser(enseignant);
        newCandidacy.setDeclaredAt(LocalDateTime.now());
        candidateRepository.save(newCandidacy);

        redirectAttributes.addFlashAttribute("successMessage", "Votre candidature a été enregistrée avec succès !");
        return "redirect:/enseignant/vote/" + voteId;
    }

    @GetMapping("/vote/{voteId}")
    public String showVotePage(@PathVariable Long voteId, Model model,
                               @AuthenticationPrincipal UserDetails userDetails,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }
        Utilisateur currentUser = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé.");
            return "redirect:/login";
        }

        Optional<Vote> voteOpt = voteRepository.findById(voteId);
        if (voteOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vote non trouvé.");
            return "redirect:/enseignant/notifications"; // Or a dedicated error page
        }
        Vote vote = voteOpt.get();

        Department department = vote.getDepartment();
        if (department == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Département associé à ce vote non trouvé.");
            return "redirect:/enseignant/notifications";
        }

        if (!department.getMembers().contains(currentUser)) {
            redirectAttributes.addFlashAttribute("warningMessage", "Vous n'êtes pas membre du département concerné par ce vote.");
            return "redirect:/enseignant/notifications";
        }

        if (vote.getStatus() == VoteStatus.PENDING) {
            redirectAttributes.addFlashAttribute("infoMessage", "Ce vote n'a pas encore commencé.");
            return "redirect:/enseignant/notifications";
        }

        if (vote.getStatus() == VoteStatus.COMPLETED || (vote.getEndDate() != null && LocalDateTime.now().isAfter(vote.getEndDate()))) {
            if (vote.getStatus() != VoteStatus.COMPLETED) {
                vote.setStatus(VoteStatus.COMPLETED);
                voteRepository.save(vote);
            }
            redirectAttributes.addFlashAttribute("infoMessage", "Ce vote est terminé.");
            return "redirect:/enseignant/notifications";
        }

        List<Candidate> candidateEntities = candidateRepository.findByVote(vote);
        List<Utilisateur> candidateUsers = candidateEntities.stream()
                .map(Candidate::getUser)
                .collect(Collectors.toList());

        boolean hasVoted = voteChoiceRepository.existsByVoteAndVoter(vote, currentUser);
        boolean isCurrentUserCandidate = voteService.isCandidateInVote(voteId, currentUser);

        model.addAttribute("vote", vote);
        model.addAttribute("department", department);
        model.addAttribute("candidateUsers", candidateUsers);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("hasVoted", hasVoted);
        model.addAttribute("isCandidate", isCurrentUserCandidate);
        model.addAttribute("currentURI", request.getRequestURI());
        model.addAttribute("pageTitle", "Participer au Vote: " + department.getName());


        return "enseignant/votePage";
    }

    @PostMapping("/vote/cast")
    @Transactional
    public String castVote(@RequestParam Long voteId,
                           @RequestParam Long candidateUserId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }
        Utilisateur voter = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (voter == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur votant non trouvé.");
            return "redirect:/login";
        }

        Vote vote = voteRepository.findById(voteId)
                .orElse(null);

        if (vote == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vote non trouvé avec ID: " + voteId);
            return "redirect:/enseignant/notifications";
        }

        Utilisateur chosenCandidate = utilisateurRepository.findById(candidateUserId)
                .orElse(null);

        if (chosenCandidate == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Candidat choisi non trouvé avec ID: " + candidateUserId);
            return "redirect:/enseignant/vote/" + voteId;
        }


        if (vote.getStatus() != VoteStatus.ACTIVE || (vote.getEndDate() != null && LocalDateTime.now().isAfter(vote.getEndDate()))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ce vote n'est plus actif.");
            return "redirect:/enseignant/notifications";
        }

        if (vote.getDepartment() == null || !vote.getDepartment().getMembers().contains(voter)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vous n'êtes pas autorisé à voter dans ce département.");
            return "redirect:/enseignant/vote/" + voteId;
        }

        if (voteChoiceRepository.existsByVoteAndVoter(vote, voter)) {
            redirectAttributes.addFlashAttribute("warningMessage", "Vous avez déjà voté pour ce scrutin.");
            return "redirect:/enseignant/vote/" + voteId;
        }


        VoteChoice voteChoice = new VoteChoice();
        voteChoice.setVote(vote);
        voteChoice.setVoter(voter);
        voteChoice.setChosenCandidate(chosenCandidate);
        voteChoice.setVotedAt(LocalDateTime.now());
        voteChoiceRepository.save(voteChoice);

        redirectAttributes.addFlashAttribute("successMessage", "Votre vote a été enregistré avec succès !");
        return "redirect:/enseignant/notifications";
    }

}