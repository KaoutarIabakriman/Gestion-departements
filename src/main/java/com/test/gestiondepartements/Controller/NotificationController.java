package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.Notification;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import com.test.gestiondepartements.Service.DepartmentService;
import com.test.gestiondepartements.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/enseignant/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;
    private final DepartmentService departmentService;
    private final DepartmentRepository departmentRepository;
    @GetMapping
    public String getNotifications(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Utilisateur user = utilisateurRepository.findByUsername(userDetails.getUsername());
        List<Notification> notifications = notificationService.getUnreadNotifications(user);

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notifications.size());

        return "enseignant/notifications";
    }
    @PostMapping("/join/{departmentId}")
    public String joinDepartment(@PathVariable Long departmentId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            Utilisateur user = utilisateurRepository.findByUsername(userDetails.getUsername());
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Département non trouvé"));

            // Vérification si déjà dans un département
            if (user.isInAnyDepartment()) {
                redirectAttributes.addAttribute("error", "already_in_department");
                return "redirect:/enseignant/notifications";
            }

            // Vérification des compétences
            if (!departmentService.departmentMatchesSkills(department, user)) {
                redirectAttributes.addAttribute("error", "skills_mismatch");
                return "redirect:/enseignant/notifications";
            }

            departmentService.addUserToDepartment(departmentId, user);
            redirectAttributes.addAttribute("success", true);
            return "redirect:/enseignant/notifications";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "general_error");
            return "redirect:/enseignant/notifications";
        }
    }

    @PostMapping("/markAsRead/{id}")
    public String markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return "redirect:/enseignant/notifications";
    }

}