// ===== FICHIER: DepartmentController.java =====
// [Chemin: C:\Users\Kaoutar Iabakriman\Desktop\GestionDepartements\src\main\java\com\test\gestiondepartements\Controller\DepartmentController.java]

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
import com.test.gestiondepartements.Security.Entities.AppRole; // AJOUT
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.AppRoleRepository; // AJOUT
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Security.Service.SecurityService; // AJOUT (Optionnel, mais recommandé)
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
import java.util.Optional; // AJOUT

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
    private final AppRoleRepository appRoleRepository; // AJOUT
    private final SecurityService securityService; // AJOUT

    public DepartmentController(DepartmentService departmentService, CommandInvoker commandInvoker,
                                DepartmentRepository departmentRepository, HistoryRepository historyRepository,
                                NotificationService notificationService, UtilisateurRepository utilisateurRepository,
                                VoteRepository voteRepository, AppRoleRepository appRoleRepository, SecurityService securityService) { // AJOUT dans le constructeur
        this.departmentService = departmentService;
        this.commandInvoker = commandInvoker;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
        this.utilisateurRepository = utilisateurRepository;
        this.voteRepository = voteRepository;
        this.appRoleRepository = appRoleRepository; // AJOUT
        this.securityService = securityService; // AJOUT
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
            model.addAttribute("error", "Un d??partement avec le nom '" + departmentDTO.getName() + "' existe d??j??.");
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            return "admin/departments";
        } catch (Exception e) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Department> departmentPage = departmentService.getAllDepartments(pageable);
            model.addAttribute("departments", departmentPage);
            model.addAttribute("departmentDTO", departmentDTO);
            model.addAttribute("error", "Erreur lors de la cr??ation du d??partement: " + e.getMessage());
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
            model.addAttribute("error", "Erreur lors de la mise ?? jour: " + e.getMessage());
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
            redirectAttributes.addFlashAttribute("error", "Erreur lors du d??marrage du vote: " + e.getMessage());
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
                    .orElseThrow(() -> new RuntimeException("D??partement non trouv?? avec ID: " + departmentId));

            Utilisateur newHead = utilisateurRepository.findById(candidateUserId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur candidat non trouv?? avec ID: " + candidateUserId));

            Vote vote = voteRepository.findById(voteId)
                    .orElseThrow(() -> new RuntimeException("Vote non trouv?? avec ID: " + voteId));

            if (!vote.getDepartment().equals(department)) {
                throw new IllegalArgumentException("Le vote ne correspond pas au d??partement sp??cifi??.");
            }
            boolean isActualCandidate = vote.getCandidates().stream().anyMatch(c -> c.getUser().equals(newHead));
            if (!isActualCandidate) {
                throw new IllegalArgumentException("L'utilisateur s??lectionn?? n'??tait pas candidat pour ce vote.");
            }

            Utilisateur oldHead = department.getHeadOfDepartment();

            // --- MODIFICATION POUR LE CHANGEMENT DE ROLE ---
            AppRole departmentHeadRole = appRoleRepository.findByRoleName("DEPARTMENT_HEAD");
            AppRole enseignantRole = appRoleRepository.findByRoleName("ENSEIGNANT");

            if (departmentHeadRole == null) {
                // Gérer le cas où le rôle n'existe pas. Peut-être le créer ou lever une exception plus spécifique.
                throw new RuntimeException("Le rôle 'DEPARTMENT_HEAD' n'a pas été trouvé. Veuillez configurer les rôles.");
            }
            if (enseignantRole == null) {
                // Optionnel, mais bon à vérifier si on veut le réassigner à l'ancien chef
                System.err.println("Attention: Le rôle 'ENSEIGNANT' n'a pas été trouvé.");
            }


            // Mettre à jour les rôles de l'ancien chef (s'il existe et est différent du nouveau)
            if (oldHead != null && !oldHead.getId().equals(newHead.getId())) {
                oldHead.getAppRoles().remove(departmentHeadRole);
                // Réassigner le rôle ENSEIGNANT s'il n'est pas déjà présent et s'il existe
                if (enseignantRole != null && !oldHead.getAppRoles().contains(enseignantRole)) {
                    oldHead.getAppRoles().add(enseignantRole);
                }
                utilisateurRepository.save(oldHead); // Sauvegarder les modifications de l'ancien chef
            }

            // Mettre à jour les rôles du nouveau chef
            // Retirer le rôle ENSEIGNANT s'il est présent (optionnel, dépend de votre logique métier)
            if (enseignantRole != null) {
                newHead.getAppRoles().remove(enseignantRole);
            }
            // Ajouter le rôle DEPARTMENT_HEAD s'il n'est pas déjà présent
            if (!newHead.getAppRoles().contains(departmentHeadRole)) {
                newHead.getAppRoles().add(departmentHeadRole);
            }
            utilisateurRepository.save(newHead); // Sauvegarder les modifications du nouveau chef
            // --- FIN MODIFICATION POUR LE CHANGEMENT DE ROLE ---


            department.setHeadOfDepartment(newHead);
            departmentRepository.save(department);

            History historyEntry = new History();
            historyEntry.setAction("UPDATE");
            historyEntry.setEntityType("Department");
            historyEntry.setEntityId(department.getId());
            String details = "Chef du d??partement '" + department.getName() + "' mis ?? jour. ";
            details += "Nouveau chef: " + newHead.getFirstName() + " " + newHead.getLastName() + " (ID: " + newHead.getId() + ", Rôle: DEPARTMENT_HEAD). ";
            if (oldHead != null) {
                details += "Ancien chef: " + oldHead.getFirstName() + " " + oldHead.getLastName() + " (ID: " + oldHead.getId() + "). ";
            } else {
                details += "Aucun chef pr??c??dent. ";
            }
            details += "S??lectionn?? suite au vote ID: " + vote.getId() + ".";
            historyEntry.setDetails(details);
            historyRepository.save(historyEntry);

            redirectAttributes.addFlashAttribute("successMessage", "Chef de d??partement '" + newHead.getFirstName() + " " + newHead.getLastName() + "' assign?? avec succ??s pour " + department.getName() + " et son rôle a été mis à jour.");
            return "redirect:/admin/departments";

        } catch (Exception e) {
            System.err.println("Error setting department head: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'assignation du chef de d??partement: " + e.getMessage());
            // Rediriger vers la page des candidats du département concerné si possible, sinon vers la liste générale des candidats.
            String redirectUrl = "/admin/candidates";
            if (departmentId != null) {
                // Si nous avons un voteId associé à ce departmentId, il serait mieux de rediriger vers les candidats de ce vote
                Optional<Vote> voteOpt = voteRepository.findByDepartmentIdAndStatus(departmentId, com.test.gestiondepartements.Entities.VoteStatus.COMPLETED)
                        .stream().findFirst(); // Ou une logique plus précise pour trouver le bon vote
                if(voteOpt.isPresent()){
                    redirectUrl += "?voteId=" + voteOpt.get().getId();
                } else {
                    redirectUrl += "?departmentId=" + departmentId;
                }
            }
            return "redirect:" + redirectUrl;
        }
    }
}