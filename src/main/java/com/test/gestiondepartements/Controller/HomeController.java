package com.test.gestiondepartements.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/index")
    public String home(Model model) {
        return "index";
    }


    @GetMapping("/")
    public String homeRedirect() {
        return "redirect:/index";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Admin Dashboard");
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("authenticatedUsername", authentication.getName());
        }

        return "admin/departments";
    }
}