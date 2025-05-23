package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Command.AddDepartmentCommand;
import com.test.gestiondepartements.Command.Command;
import com.test.gestiondepartements.Command.CommandInvoker;
import com.test.gestiondepartements.Command.UpdateDepartmentCommand;
import com.test.gestiondepartements.Command.SetDepartmentHeadCommand;
import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Entities.VoteStatus;
import com.test.gestiondepartements.Repositories.*;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.AppRoleRepository;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.DepartmentService;
import com.test.gestiondepartements.Service.NotificationService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/admin/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CommandInvoker commandInvoker;
    private final DepartmentRepository departmentRepository;
    private final HistoryRepository historyRepository;
    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;
    private final VoteRepository voteRepository;
    private final AppRoleRepository appRoleRepository;

    public DepartmentController(DepartmentService departmentService, CommandInvoker commandInvoker,
                                DepartmentRepository departmentRepository, HistoryRepository historyRepository,
                                NotificationService notificationService, UtilisateurRepository utilisateurRepository,
                                VoteRepository voteRepository, AppRoleRepository appRoleRepository) {
        this.departmentService = departmentService;
        this.commandInvoker = commandInvoker;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
        this.utilisateurRepository = utilisateurRepository;
        this.voteRepository = voteRepository;
        this.appRoleRepository = appRoleRepository;
    }

    @GetMapping
    public String listDepartments(Model model,
                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                  @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Department> departmentPage = departmentService.getAllDepartments(pageable);
        model.addAttribute("departments", departmentPage);
        model.addAttribute("departmentDTO", new DepartmentDTO());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "admin/departments";
    }

    @PostMapping
    public String addDepartment(@Valid @ModelAttribute("departmentDTO") DepartmentDTO departmentDTO,
                                BindingResult result, Model model,
                                RedirectAttributes redirectAttributes,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                @RequestParam(name = "size", defaultValue = "10") int size) {
        if (result.hasErrors()) {
            return handleFormError(page, size, model, departmentDTO);
        }

        try {
            Department department = new Department();
            department.setName(departmentDTO.getName());
            department.setDescription(departmentDTO.getDescription());

            Command command = new AddDepartmentCommand(department, departmentRepository,
                    historyRepository, notificationService,
                    utilisateurRepository);
            commandInvoker.executeCommand(command);

            redirectAttributes.addFlashAttribute("success", "dept_added"); // Message clé pour i18n
            return "redirect:/admin/departments?page=" + page + "&size=" + size;
        } catch (DataIntegrityViolationException e) {
            return handleDuplicateDepartmentError(page, size, model, departmentDTO,
                    "Un département avec le nom '" + departmentDTO.getName() + "' existe déjà.");
        } catch (Exception e) {
            return handleGenericError(page, size, model, departmentDTO,
                    "Erreur lors de la création du département: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public String updateDepartment(@Valid @ModelAttribute("departmentDTO") DepartmentDTO departmentDTO,
                                   BindingResult result, Model model,
                                   RedirectAttributes redirectAttributes,
                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                   @RequestParam(name = "size", defaultValue = "10") int size) {
        if (result.hasErrors()) {
            return handleFormError(page, size, model, departmentDTO);
        }

        try {
            Command command = new UpdateDepartmentCommand(departmentDTO, departmentRepository,
                    historyRepository);
            commandInvoker.executeCommand(command);
            redirectAttributes.addFlashAttribute("success", "dept_updated"); // Message clé pour i18n
            return "redirect:/admin/departments?page=" + page + "&size=" + size;
        } catch (Exception e) {
            return handleGenericError(page, size, model, departmentDTO,
                    "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @PostMapping("/startVote/{departmentId}")
    public String startVote(@PathVariable Long departmentId,
                            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                            RedirectAttributes redirectAttributes,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {

            departmentService.startVote(departmentId, endDate);
            redirectAttributes.addFlashAttribute("success", "vote_started"); // Message clé
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du démarrage du vote: " + e.getMessage());
        }
        return "redirect:/admin/departments?page=" + page + "&size=" + size;
    }

    @PostMapping("/set-head")
    @Transactional
    public String setDepartmentHead(@RequestParam Long departmentId,
                                    @RequestParam Long candidateUserId,
                                    @RequestParam Long voteId,
                                    RedirectAttributes redirectAttributes) {
        try {
            Command command = new SetDepartmentHeadCommand(
                    departmentId,
                    candidateUserId,
                    voteId,
                    departmentRepository,
                    utilisateurRepository,
                    voteRepository,
                    appRoleRepository,
                    historyRepository
            );
            commandInvoker.executeCommand(command);


            Department department = departmentRepository.findById(departmentId).orElse(null); // Optionnel pour le message
            Utilisateur newHead = utilisateurRepository.findById(candidateUserId).orElse(null); // Optionnel pour le message
            if (department != null && newHead != null) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Chef de département '" + newHead.getFirstName() + " " + newHead.getLastName() +
                                "' assigné avec succès pour " + department.getName());
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Chef de département assigné avec succès.");
            }

            return buildSuccessRedirect(departmentId, voteId, redirectAttributes);
        } catch (Exception e) {

            return handleHeadAssignmentError(departmentId, voteId, e, redirectAttributes);
        }
    }


    private String handleFormError(int page, int size, Model model, DepartmentDTO departmentDTO) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Department> departmentPage = departmentService.getAllDepartments(pageable);
        model.addAttribute("departments", departmentPage);
        model.addAttribute("departmentDTO", departmentDTO);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "admin/departments";
    }

    private String handleDuplicateDepartmentError(int page, int size, Model model,
                                                  DepartmentDTO departmentDTO, String errorMessage) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Department> departmentPage = departmentService.getAllDepartments(pageable);
        model.addAttribute("departments", departmentPage);
        model.addAttribute("departmentDTO", departmentDTO);
        model.addAttribute("error", errorMessage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "admin/departments";
    }

    private String handleGenericError(int page, int size, Model model,
                                      DepartmentDTO departmentDTO, String errorMessage) {
        return handleDuplicateDepartmentError(page, size, model, departmentDTO, errorMessage);
    }

    private String buildSuccessRedirect(Long departmentId, Long voteId, RedirectAttributes redirectAttributes) {
        Optional<Vote> voteOpt = voteRepository.findByDepartmentIdAndStatus(departmentId, VoteStatus.COMPLETED)
                .stream()
                .filter(v -> v.getId().equals(voteId))
                .findFirst();

        if (voteOpt.isPresent()) {
            return "redirect:/admin/candidates?voteId=" + voteOpt.get().getId();
        } else {

            return "redirect:/admin/candidates?departmentId=" + departmentId;
        }
    }

    private String handleHeadAssignmentError(Long departmentId, Long voteId,
                                             Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage",
                "Erreur lors de l'assignation du chef de département: " + e.getMessage());

        if (voteId != null) {
            return "redirect:/admin/candidates?voteId=" + voteId;
        } else if (departmentId != null) {
            return "redirect:/admin/candidates?departmentId=" + departmentId;
        }
        return "redirect:/admin/candidates";
    }
}