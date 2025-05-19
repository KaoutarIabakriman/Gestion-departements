package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.Candidate;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Entities.VoteChoice;
import com.test.gestiondepartements.Entities.VoteStatus;
import com.test.gestiondepartements.Repositories.CandidateRepository;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.VoteChoiceRepository;
import com.test.gestiondepartements.Repositories.VoteRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.DepartmentService;
import com.test.gestiondepartements.Service.VoteService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/vote")
@RequiredArgsConstructor
public class VoteController {

    private final VoteRepository voteRepository;
    private final VoteChoiceRepository voteChoiceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DepartmentService departmentService;
    private final CandidateRepository candidateRepository;
    private final VoteService voteService;


    @GetMapping("/{voteId}")
    public String showVotePage(@PathVariable Long voteId, Model model,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        Utilisateur currentUser = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non authentifié.");
            return "redirect:/login";
        }

        Vote vote = voteRepository.findByIdWithDepartmentAndMembers(voteId).orElse(null);
        if (vote == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vote non trouvé.");
            return "redirect:/enseignant/notifications";
        }

        Department voteDepartment = vote.getDepartment();
        if (voteDepartment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Département associé au vote non trouvé.");
            return "redirect:/enseignant/notifications";
        }

        if (currentUser.getDepartments() == null || !currentUser.getDepartments().contains(voteDepartment)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vous n'êtes pas autorisé à voter pour ce département.");
            return "redirect:/enseignant/notifications";
        }

        model.addAttribute("vote", vote);
        if (vote.getStatus() != VoteStatus.ACTIVE) {
            String message = vote.getStatus() == VoteStatus.COMPLETED ? "Ce vote est terminé." : "Ce vote n'est pas encore actif ou a été annulé.";
            model.addAttribute("warningMessage", message);
        }

        boolean hasVoted = voteChoiceRepository.findByVoteAndVoter(vote, currentUser).isPresent();
        model.addAttribute("hasVoted", hasVoted);

        boolean isCandidate = voteService.isCandidateInVote(voteId, currentUser); // Use VoteService
        model.addAttribute("isCandidate", isCandidate);

        List<Candidate> candidates = departmentService.getCandidatesForVote(voteId);
        model.addAttribute("candidates", candidates);

        return "enseignant/votePage";
    }

    @PostMapping("/submit")
    public String submitVote(@RequestParam Long voteId,
                             @RequestParam Long candidateId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        Utilisateur voter = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (voter == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur votant non authentifié.");
            return "redirect:/login";
        }

        Vote vote = voteRepository.findByIdWithDepartmentAndMembers(voteId).orElse(null);
        Utilisateur chosenCandidate = utilisateurRepository.findById(candidateId).orElse(null);

        if (vote == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vote non trouvé.");
            return "redirect:/enseignant/notifications";
        }
        if (chosenCandidate == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Candidat choisi non trouvé.");
            return "redirect:/vote/" + voteId;
        }

        Department voteDepartment = vote.getDepartment();
        if (voteDepartment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Département pour ce vote non trouvé.");
            return "redirect:/enseignant/notifications";
        }

        if (voter.getDepartments() == null || !voter.getDepartments().contains(voteDepartment)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vous n'êtes pas autorisé à voter pour ce département.");
            return "redirect:/enseignant/notifications";
        }

        if (chosenCandidate.getDepartments() == null || !chosenCandidate.getDepartments().contains(voteDepartment)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le candidat sélectionné n'appartient pas à ce département.");
            return "redirect:/vote/" + voteId;
        }

        if (vote.getStatus() != VoteStatus.ACTIVE || LocalDateTime.now().isAfter(vote.getEndDate())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ce vote n'est plus actif.");
            return "redirect:/enseignant/notifications";
        }

        if (voteChoiceRepository.findByVoteAndVoter(vote, voter).isPresent()) {
            redirectAttributes.addFlashAttribute("warningMessage", "Vous avez déjà participé à ce vote.");
            return "redirect:/enseignant/notifications";
        }

        VoteChoice voteChoice = new VoteChoice();
        voteChoice.setVote(vote);
        voteChoice.setVoter(voter);
        voteChoice.setChosenCandidate(chosenCandidate);
        voteChoiceRepository.save(voteChoice);

        redirectAttributes.addFlashAttribute("successMessage", "Votre vote a été enregistré avec succès !");
        return "redirect:/enseignant/notifications";
    }

    @PostMapping("/declareCandidacy/{voteId}")
    public String declareCandidacy(@PathVariable Long voteId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            Utilisateur candidate = utilisateurRepository.findByUsername(userDetails.getUsername());
            if (candidate == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
                return "redirect:/";
            }

            Vote vote = voteRepository.findById(voteId).orElseThrow(() -> new RuntimeException("Vote not found."));

            if (vote.getStatus() != VoteStatus.ACTIVE || LocalDateTime.now().isAfter(vote.getEndDate())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vote is closed or no longer active.");
                return "redirect:/enseignant/notifications";
            }

            boolean isCandidate = voteService.isCandidateInVote(voteId, candidate);  // Use voteService
            if (isCandidate) {
                redirectAttributes.addFlashAttribute("errorMessage", "You are already a candidate in this vote.");
                return "redirect:/vote/" + voteId;
            }

            Candidate candidacy = new Candidate();
            candidacy.setVote(vote);
            candidacy.setUser(candidate);
            candidacy.setDeclaredAt(LocalDateTime.now());
            candidateRepository.save(candidacy);

            redirectAttributes.addFlashAttribute("successMessage", "Your candidacy has been declared!");


        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            return "redirect:/vote/" + voteId;
        }
        return "redirect:/vote/" + voteId;
    }
}