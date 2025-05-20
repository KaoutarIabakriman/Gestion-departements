// CandidateController.java
package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Dto.CandidateVoteDetailsDTO;
import com.test.gestiondepartements.Repositories.CandidateRepository;
import com.test.gestiondepartements.Repositories.VoteChoiceRepository;
import com.test.gestiondepartements.Service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private static final Logger log = LoggerFactory.getLogger(CandidateController.class);
    private final CandidateService candidateService;
    private final CandidateRepository candidateRepository;
    private final VoteChoiceRepository voteChoiceRepository;

    @GetMapping
    public String showCandidates(Model model) {
        List<CandidateVoteDetailsDTO> candidatesInfo = candidateService.getAllCandidaciesWithVoteDetails();
        model.addAttribute("candidatesInfo", candidatesInfo);
        return "admin/candidateManagement";
    }

    @PostMapping("/delete/{candidateId}")
    public String deleteCandidate(@PathVariable Long candidateId, RedirectAttributes redirectAttributes) {
        try {
            candidateService.deleteCandidacy(candidateId);
            redirectAttributes.addFlashAttribute("successMessage", "Candidature supprimée avec succès !");
        } catch (Exception e) {
            log.error("Error deleting candidacy: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting candidacy: " + e.getMessage());
        }
        return "redirect:/admin/candidates";
    }
}