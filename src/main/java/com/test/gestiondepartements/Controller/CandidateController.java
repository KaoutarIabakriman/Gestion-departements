package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Repositories.CandidateRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.test.gestiondepartements.Entities.Candidate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/candidates")
public class CandidateController {
    private final CandidateRepository candidateRepository;

    public CandidateController(CandidateRepository candidateRepository) {
        this.candidateRepository=candidateRepository;
    }

    @GetMapping
    public String showCandidates(Model model) {
        List<Candidate> candidates = candidateRepository.findAll();
        model.addAttribute("candidates", candidates);
        return "admin/candidateManagement";
    }

    @PostMapping("/delete/{candidateId}")
    public String deleteCandidate(@PathVariable Long candidateId, RedirectAttributes redirectAttributes) {
        try {
            candidateRepository.deleteById(candidateId);
            redirectAttributes.addFlashAttribute("successMessage", "Candidacy deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting candidacy: " + e.getMessage());
        }
        return "redirect:/admin/candidates";
    }

}