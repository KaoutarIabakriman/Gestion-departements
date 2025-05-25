package com.test.gestiondepartements.Controller;

import jakarta.servlet.http.HttpServletRequest;

import com.test.gestiondepartements.Dto.CandidateVoteDetailsDTO;
import com.test.gestiondepartements.Entities.VoteStatus;
import com.test.gestiondepartements.Service.CandidateService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @GetMapping
    public String showCandidates(Model model, HttpServletRequest request,
                                 @RequestParam(name = "departmentId", required = false) Long departmentIdParam,
                                 @RequestParam(name = "voteId", required = false) Long voteIdParam) {

        List<CandidateVoteDetailsDTO> allCandidatesInfo = candidateService.getAllCandidaciesWithVoteDetails();
        List<CandidateVoteDetailsDTO> displayedCandidates;

        String pageTitle = "Toutes les Candidatures Enregistrées";

        if (departmentIdParam != null) {
            model.addAttribute("filterDepartmentId", departmentIdParam);
            displayedCandidates = allCandidatesInfo.stream()
                    .filter(c -> c.getDepartmentId() != null && c.getDepartmentId().equals(departmentIdParam))
                    .filter(c -> c.getVoteStatus() == VoteStatus.COMPLETED)
                    .collect(Collectors.toList());
            if (!displayedCandidates.isEmpty()) {
                pageTitle = "Candidats pour le vote terminé du département : " + displayedCandidates.get(0).getDepartmentName();
            } else {
                pageTitle = "Candidatures pour le département (aucun vote terminé trouvé ou aucun candidat)";
                displayedCandidates = allCandidatesInfo.stream()
                        .filter(c -> c.getDepartmentId() != null && c.getDepartmentId().equals(departmentIdParam))
                        .collect(Collectors.toList());
            }
        } else if (voteIdParam != null) {
            model.addAttribute("filterVoteId", voteIdParam);
            displayedCandidates = allCandidatesInfo.stream()
                    .filter(c -> c.getVoteId() != null && c.getVoteId().equals(voteIdParam))
                    .collect(Collectors.toList());
            if (!displayedCandidates.isEmpty()) {
                pageTitle = "Candidats pour le vote ID : " + voteIdParam + " (Département: " + displayedCandidates.get(0).getDepartmentName() + ")";
            } else {
                pageTitle = "Aucun candidat pour le vote ID : " + voteIdParam;
            }
        } else {
            displayedCandidates = allCandidatesInfo;
        }


        model.addAttribute("currentURI", request.getRequestURI());
        model.addAttribute("candidatesInfo", displayedCandidates);
        model.addAttribute("pageTitle", pageTitle);
        return "admin/candidateManagement";
    }
}