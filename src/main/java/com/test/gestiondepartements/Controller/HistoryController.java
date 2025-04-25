package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Repositories.HistoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/historique")
public class HistoryController {

    private final HistoryRepository historyRepository;

    public HistoryController(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @GetMapping
    public String showHistory(Model model) {
        model.addAttribute("history", historyRepository.findAllByOrderByCreatedAtDesc());
        return "admin/history";
    }
}