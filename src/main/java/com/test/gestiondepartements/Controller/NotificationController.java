package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.Notification;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.NotificationService;
import com.test.gestiondepartements.Repositories.NotificationRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


// Path: C:\Users\Kaoutar Iabakriman\Desktop\Gestion_departement\GestionDepartementsV2\GestionDepartements\src\main\java\com\test\gestiondepartements\Controller\NotificationController.java
// ... other imports
import com.test.gestiondepartements.Service.DepartmentService; // Add this import
// ...

@Controller
@RequestMapping("/enseignant/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationRepository notificationRepository;
    private final DepartmentService departmentService; // Add this

    @GetMapping
    @Transactional(readOnly = true)
    public String showNotificationsPage(Model model,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        HttpServletRequest request,
                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                        @RequestParam(name = "size", defaultValue = "5") int size) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Utilisateur currentUser = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            return "redirect:/login?error=UserNotFound";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notificationPage = notificationRepository.findByUser(currentUser, pageable);
        long unreadCount = notificationRepository.countByUserAndReadStatusFalse(currentUser);

        model.addAttribute("notifications", notificationPage.getContent());
        model.addAttribute("notificationPage", notificationPage);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("currentURI", request.getRequestURI());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("currentUser", currentUser); // Add currentUser to the model for Thymeleaf checks

        // Keep flash attributes if they exist
        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.getAttribute("errorMessage"));
        }
        if (model.containsAttribute("warningMessage")) {
            model.addAttribute("warningMessage", model.getAttribute("warningMessage"));
        }

        return "enseignant/notifications";
    }

    @PostMapping("/markAsRead/{notificationId}")
    public String markAsRead(@PathVariable Long notificationId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes,
                             @RequestParam(name = "page", defaultValue = "0") int page,
                             @RequestParam(name = "size", defaultValue = "5") int size) {
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vous devez être connecté.");
            return "redirect:/login";
        }
        Utilisateur currentUser = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé.");
            return "redirect:/login";
        }

        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null || !notification.getUser().equals(currentUser)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Notification non trouvée ou non autorisée.");
            return "redirect:/enseignant/notifications?page=" + page + "&size=" + size;
        }

        notificationService.markAsRead(notificationId);
        // No success message here for marking as read, it should be silent or handled by UI change
        return "redirect:/enseignant/notifications?page=" + page + "&size=" + size;
    }

    @PostMapping("/joinDepartment/{departmentId}") // Correct mapping including "Department"
    public String joinDepartmentAction(@PathVariable Long departmentId,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       RedirectAttributes redirectAttributes,
                                       @RequestParam(name = "page", defaultValue = "0") int page,
                                       @RequestParam(name = "size", defaultValue = "5") int size) {
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vous devez être connecté.");
            return "redirect:/login";
        }
        Utilisateur currentUser = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé.");
            return "redirect:/login";
        }

        try {
            departmentService.addUserToDepartment(departmentId, currentUser);
            com.test.gestiondepartements.Entities.Department joinedDepartment = currentUser.getDepartments().stream()
                    .filter(d -> d.getId().equals(departmentId))
                    .findFirst()
                    .orElse(null);

            if (joinedDepartment != null) {
                redirectAttributes.addFlashAttribute("successMessage", "Vous avez rejoint le département '" + joinedDepartment.getName() + "' avec succès.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Vous avez rejoint le département (ID: " + departmentId + ") avec succès.");
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("warningMessage", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la tentative de rejoindre le département: " + e.getMessage());
        }
        return "redirect:/enseignant/notifications?page=" + page + "&size=" + size;
    }
}