package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.History;
import com.test.gestiondepartements.Repositories.HistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/history")
public class HistoryController {
    private final HistoryRepository historyRepository;

    public HistoryController(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @GetMapping
    public String showHistory(Model model, HttpServletRequest request,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<History> historyPage = historyRepository.findAll(pageable);

        model.addAttribute("historyPage", historyPage);
        model.addAttribute("currentURI", request.getRequestURI());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "admin/history";
    }
}