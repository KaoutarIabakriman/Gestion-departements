package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Command.AddDepartmentCommand;
import com.test.gestiondepartements.Command.Command;
import com.test.gestiondepartements.Command.CommandInvoker;
import com.test.gestiondepartements.Command.UpdateDepartmentCommand;
import com.test.gestiondepartements.Dto.DepartmentDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.History;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Repositories.HistoryRepository;
import com.test.gestiondepartements.Repositories.VoteRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
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

    public DepartmentController(DepartmentService departmentService, CommandInvoker commandInvoker,
                                DepartmentRepository departmentRepository, HistoryRepository historyRepository,
                                NotificationService notificationService, UtilisateurRepository utilisateurRepository,
                                VoteRepository voteRepository) {
        this.departmentService = departmentService;
        this.commandInvoker = commandInvoker;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
        this.utilisateurRepository = utilisateurRepository;
        this.voteRepository = voteRepository;
    }

    @GetMapping
    public String listDepartments(Model model, @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Department> departmentPage = departmentService.getAllDepartments(pageable);
        model.addAttribute("departments", departmentPage);
        model.addAttribute("departmentDTO", new DepartmentDTO());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "admin/departments";
    }

    @PostMapping
    public String addDepartment(@Valid @ModelAttribute("departmentDTO") DepartmentDTO departmentDTO, BindingResult result, Model model, RedirectAttributes redirectAttributes, @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "10") int size) {
        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Department> departmentPage = departmentService.getAllDepartments(pageable);
            model.addAttribute("departments", departmentPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            return "admin/departments";
        }

        try {
            Department department = new Department();
            department.setName(departmentDTO.getName());
            department.setDescription(departmentDTO.getDescription());

            Command command = new AddDepartmentCommand(department, departmentRepository, historyRepository, notificationService, utilisateurRepository);
            commandInvoker.executeCommand(command);

            redirectAttributes.addFlashAttribute("success", "dept_added");
            return "redirect:/admin/departments?page=" + page + "&size=" + size;
        } catch (DataIntegrityViolationException e) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Department> departmentPage = departmentService.getAllDepartments(pageable);
            model.addAttribute("departments", departmentPage);
            model.addAttribute("departmentDTO", departmentDTO);
            model.addAttribute("error", "Un département avec le nom '" + departmentDTO.getName() + "' existe déjà.");
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            return "admin/departments";
        } catch (Exception e) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Department> departmentPage = departmentService.getAllDepartments(pageable);
            model.addAttribute("departments", departmentPage);
            model.addAttribute("departmentDTO", departmentDTO);
            model.addAttribute("error", "Erreur lors de la création du département: " + e.getMessage());
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            return "admin/departments";
        }
    }

    @PostMapping("/update")
    public String updateDepartment(@Valid @ModelAttribute("departmentDTO") DepartmentDTO departmentDTO, BindingResult result, Model model, RedirectAttributes redirectAttributes, @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "10") int size) {
        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Department> departmentPage = departmentService.getAllDepartments(pageable);
            model.addAttribute("departments", departmentPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            return "admin/departments";
        }
        try {
            Command command = new UpdateDepartmentCommand(departmentDTO, departmentRepository, historyRepository);
            commandInvoker.executeCommand(command);
            redirectAttributes.addFlashAttribute("success", "dept_updated");
            return "redirect:/admin/departments?page=" + page + "&size=" + size;
        } catch (Exception e) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Department> departmentPage = departmentService.getAllDepartments(pageable);
            model.addAttribute("departments", departmentPage);
            model.addAttribute("departmentDTO", departmentDTO);
            model.addAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            return "admin/departments";
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
            redirectAttributes.addFlashAttribute("success", "vote_started");
        } catch (Exception e) {
            System.err.println("Error starting vote for department ID " + departmentId + ": " + e.getMessage());
            e.printStackTrace();
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
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Département non trouvé avec ID: " + departmentId));

            Utilisateur newHead = utilisateurRepository.findById(candidateUserId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur candidat non trouvé avec ID: " + candidateUserId));

            Vote vote = voteRepository.findById(voteId)
                    .orElseThrow(() -> new RuntimeException("Vote non trouvé avec ID: " + voteId));

            if (!vote.getDepartment().equals(department)) {
                throw new IllegalArgumentException("Le vote ne correspond pas au département spécifié.");
            }
            boolean isActualCandidate = vote.getCandidates().stream().anyMatch(c -> c.getUser().equals(newHead));
            if (!isActualCandidate) {
                throw new IllegalArgumentException("L'utilisateur sélectionné n'était pas candidat pour ce vote.");
            }

            Utilisateur oldHead = department.getHeadOfDepartment();
            department.setHeadOfDepartment(newHead);
            departmentRepository.save(department);

            History historyEntry = new History();
            historyEntry.setAction("UPDATE");
            historyEntry.setEntityType("Department");
            historyEntry.setEntityId(department.getId());
            String details = "Chef du département '" + department.getName() + "' mis à jour. ";
            details += "Nouveau chef: " + newHead.getFirstName() + " " + newHead.getLastName() + " (ID: " + newHead.getId() + "). ";
            if (oldHead != null) {
                details += "Ancien chef: " + oldHead.getFirstName() + " " + oldHead.getLastName() + " (ID: " + oldHead.getId() + "). ";
            } else {
                details += "Aucun chef précédent. ";
            }
            details += "Sélectionné suite au vote ID: " + vote.getId() + ".";
            historyEntry.setDetails(details);
            historyRepository.save(historyEntry);

            redirectAttributes.addFlashAttribute("successMessage", "Chef de département '" + newHead.getFirstName() + " " + newHead.getLastName() + "' assigné avec succès pour " + department.getName() + ".");
            return "redirect:/admin/departments";

        } catch (Exception e) {
            System.err.println("Error setting department head: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'assignation du chef de département: " + e.getMessage());
            return "redirect:/admin/candidates" + (departmentId != null ? "?departmentId=" + departmentId : "");
        }
    }
}