package com.test.gestiondepartements.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/index")
    public String home(Model model) {
        return "index";
    }
    @GetMapping("/layout/dashboard")
    public String index(Model model) {
        return "layout/dashboard";
    }


    @GetMapping("/")
    public String home() {
        return "redirect:/index";
    }
}