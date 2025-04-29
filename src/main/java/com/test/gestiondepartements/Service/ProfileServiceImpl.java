package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.ProfileDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Repositories.DepartmentRepository;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final UtilisateurRepository utilisateurRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentService departmentService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Utilisateur updateProfile(ProfileDTO profileDTO) {
        Utilisateur user = utilisateurRepository.findByIdWithDepartments(profileDTO.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setFirstName(profileDTO.getFirstName());
        user.setLastName(profileDTO.getLastName());
        user.setPhone(profileDTO.getPhone());
        user.setSkills(profileDTO.getSkills());
        user.setLanguages(profileDTO.getLanguages());
        user.setEducation(profileDTO.getEducation());

        Utilisateur savedUser = utilisateurRepository.save(user);

        List<Department> allDepartments = departmentRepository.findAll();
        for (Department department : allDepartments) {
            if (!user.getDepartments().contains(department)
                    && departmentService.departmentMatchesSkills(department, savedUser)) {
                String message = "Le département '" + department.getName() + "' correspond à vos nouvelles compétences.";
                notificationService.createNotification(savedUser, department, message);
            }
        }

        return savedUser;
    }

    @Override
    public ProfileDTO getProfileById(Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        ProfileDTO dto = new ProfileDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setSkills(user.getSkills());
        dto.setLanguages(user.getLanguages());
        dto.setEducation(user.getEducation());

        return dto;
    }
}