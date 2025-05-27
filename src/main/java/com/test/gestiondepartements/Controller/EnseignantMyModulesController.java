package com.test.gestiondepartements.Controller;

import com.test.gestiondepartements.Entities.Module;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/enseignant/my-modules")
@RequiredArgsConstructor
public class EnseignantMyModulesController {

    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String listMyTaughtModules(Model model,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      HttpServletRequest request,
                                      @RequestParam(name = "searchTerm", required = false) String searchTerm,
                                      @RequestParam(name = "minWorkload", required = false) Integer minWorkload,
                                      @RequestParam(name = "maxWorkload", required = false) Integer maxWorkload,
                                      @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
                                      @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
                                      @RequestParam(name = "page", defaultValue = "0") int page,
                                      @RequestParam(name = "size", defaultValue = "10") int size) {

        if (userDetails == null) {
            return "redirect:/login";
        }
        Utilisateur currentUser = utilisateurRepository.findByUsername(userDetails.getUsername());
        if (currentUser == null) {
            return "redirect:/login?error=UserNotFound";
        }

        Set<Module> allTaughtModulesSet = currentUser.getModules();
        List<Module> filteredModules = new ArrayList<>(allTaughtModulesSet);


        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearchTerm = searchTerm.toLowerCase();
            filteredModules = filteredModules.stream()
                    .filter(module -> (module.getName() != null && module.getName().toLowerCase().contains(lowerSearchTerm)) ||
                            (module.getDescription() != null && module.getDescription().toLowerCase().contains(lowerSearchTerm)))
                    .collect(Collectors.toList());
        }
        if (minWorkload != null) {
            filteredModules = filteredModules.stream()
                    .filter(module -> module.getWorkload() >= minWorkload)
                    .collect(Collectors.toList());
        }
        if (maxWorkload != null) {
            filteredModules = filteredModules.stream()
                    .filter(module -> module.getWorkload() <= maxWorkload)
                    .collect(Collectors.toList());
        }

        Comparator<Module> comparator = switch (sortBy) {
            case "workload" -> Comparator.comparing(Module::getWorkload);
            case "description" -> Comparator.comparing(Module::getDescription, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(Module::getName, Comparator.nullsLast(String::compareToIgnoreCase));
        };
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        filteredModules.sort(comparator);

        int startItem = page * size;
        List<Module> pageContent;

        if (filteredModules.size() < startItem) {
            pageContent = List.of();
        } else {
            int toIndex = Math.min(startItem + size, filteredModules.size());
            pageContent = filteredModules.subList(startItem, toIndex);
        }

        Page<Module> modulePage = new PageImpl<>(pageContent, PageRequest.of(page, size), filteredModules.size());

        model.addAttribute("modulePage", modulePage);
        model.addAttribute("currentURI", request.getRequestURI());
        model.addAttribute("taughtModules", allTaughtModulesSet);

        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("minWorkload", minWorkload);
        model.addAttribute("maxWorkload", maxWorkload);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "enseignant/my_modules";
    }
}