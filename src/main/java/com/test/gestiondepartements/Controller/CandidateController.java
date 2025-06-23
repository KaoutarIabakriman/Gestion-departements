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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;
    public static record SimpleDepartmentInfo(Long departmentId, String departmentName) {}
    public static record SimpleVoteInfo(Long voteId, String departmentName, VoteStatus voteStatus) {}


    @GetMapping
    public String showCandidates(Model model, HttpServletRequest request,
                                 @RequestParam(name = "departmentId", required = false) Long departmentIdParam,
                                 @RequestParam(name = "voteId", required = false) Long voteIdParam) {

        List<CandidateVoteDetailsDTO> allCandidatesInfo = candidateService.getAllCandidaciesWithVoteDetails();
        List<CandidateVoteDetailsDTO> displayedCandidates;
        String pageTitle = "Gestion des Candidatures et Votes";
        boolean specificVoteSelected = false;
        VoteStatus currentVoteStatusForButton = null;
        Long currentFilterVoteId = voteIdParam;
        Long currentFilterDepartmentId = departmentIdParam;


        if (voteIdParam != null) {
            model.addAttribute("filterVoteId", voteIdParam);
            specificVoteSelected = true;
            displayedCandidates = allCandidatesInfo.stream()
                    .filter(c -> c.getVoteId() != null && c.getVoteId().equals(voteIdParam))
                    .sorted(Comparator.comparing(CandidateVoteDetailsDTO::getVoteCount).reversed())
                    .collect(Collectors.toList());

            if (!displayedCandidates.isEmpty()) {
                pageTitle = "Candidats pour le Vote ID : " + voteIdParam + " (Département: " + displayedCandidates.get(0).getDepartmentName() + ")";
                currentVoteStatusForButton = displayedCandidates.get(0).getVoteStatus();
                if (displayedCandidates.get(0).getDepartmentId() != null) {
                    currentFilterDepartmentId = displayedCandidates.get(0).getDepartmentId();
                    model.addAttribute("filterDepartmentId", currentFilterDepartmentId);
                }
            } else {
                pageTitle = "Aucun candidat pour le vote ID : " + voteIdParam;
            }
        } else if (departmentIdParam != null) {
            model.addAttribute("filterDepartmentId", departmentIdParam);
            specificVoteSelected = true;
            displayedCandidates = allCandidatesInfo.stream()
                    .filter(c -> c.getDepartmentId() != null && c.getDepartmentId().equals(departmentIdParam))
                    .filter(c -> c.getVoteStatus() == VoteStatus.COMPLETED)
                    .sorted(Comparator.comparing(CandidateVoteDetailsDTO::getVoteId).reversed()
                            .thenComparing(CandidateVoteDetailsDTO::getVoteCount).reversed())
                    .collect(Collectors.toList());

            if (!displayedCandidates.isEmpty()) {
                Long latestCompletedVoteId = displayedCandidates.get(0).getVoteId();
                displayedCandidates = displayedCandidates.stream()
                        .filter(c -> c.getVoteId().equals(latestCompletedVoteId))
                        .collect(Collectors.toList());
                pageTitle = "Candidats du dernier vote terminé pour le département : " + displayedCandidates.get(0).getDepartmentName();
                currentVoteStatusForButton = VoteStatus.COMPLETED;
                currentFilterVoteId = latestCompletedVoteId;
                model.addAttribute("filterVoteId", currentFilterVoteId);
            } else {
                specificVoteSelected = false;
                displayedCandidates = allCandidatesInfo.stream()
                        .filter(c -> c.getDepartmentId() != null && c.getDepartmentId().equals(departmentIdParam))
                        .sorted(Comparator.comparing(CandidateVoteDetailsDTO::getVoteId).reversed())
                        .collect(Collectors.toList());
                pageTitle = "Toutes les candidatures pour le département : " + (displayedCandidates.isEmpty() ? "N/A" : displayedCandidates.get(0).getDepartmentName());
                if (!displayedCandidates.isEmpty()){
                    currentVoteStatusForButton = displayedCandidates.get(0).getVoteStatus();
                }
            }
        } else {
            displayedCandidates = allCandidatesInfo.stream()
                    .sorted(Comparator.comparing(CandidateVoteDetailsDTO::getVoteStatus, (s1, s2) -> {
                                if (s1 == VoteStatus.COMPLETED && s2 != VoteStatus.COMPLETED) return -1;
                                if (s1 != VoteStatus.COMPLETED && s2 == VoteStatus.COMPLETED) return 1;
                                return 0;
                            }).thenComparing(CandidateVoteDetailsDTO::getVoteId).reversed()
                            .thenComparing(CandidateVoteDetailsDTO::getVoteCount).reversed())
                    .collect(Collectors.toList());
            pageTitle = "Liste de toutes les candidatures";
        }

        model.addAttribute("candidatesInfo", displayedCandidates);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("currentURI", request.getRequestURI());
        model.addAttribute("specificVoteSelected", specificVoteSelected);
        model.addAttribute("currentVoteStatusForButton", currentVoteStatusForButton);

        model.addAttribute("filterVoteId", currentFilterVoteId);
        model.addAttribute("filterDepartmentId", currentFilterDepartmentId);

        model.addAttribute("allDepartments", allCandidatesInfo.stream()
                .filter(c -> c.getDepartmentName() != null && c.getDepartmentId() != null)
                .map(c -> new SimpleDepartmentInfo(c.getDepartmentId(), c.getDepartmentName()))
                .distinct()
                .sorted(Comparator.comparing(SimpleDepartmentInfo::departmentName))
                .collect(Collectors.toList()));

        model.addAttribute("allVotes", allCandidatesInfo.stream()
                .filter(c -> c.getVoteId() != null && c.getDepartmentName() != null)
                .map(c -> new SimpleVoteInfo(c.getVoteId(), "ID " + c.getVoteId() + " - " +c.getDepartmentName(), c.getVoteStatus())) // Modified text for clarity
                .distinct()
                .sorted(Comparator.comparing(SimpleVoteInfo::voteId).reversed())
                .collect(Collectors.toList()));

        return "admin/candidateManagement";
    }
}