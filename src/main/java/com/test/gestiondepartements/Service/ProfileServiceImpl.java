package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.ProfileDTO;
import com.test.gestiondepartements.Entities.Department;
import com.test.gestiondepartements.Entities.NotificationType;
import com.test.gestiondepartements.Entities.Vote;
import com.test.gestiondepartements.Entities.VoteStatus;
import com.test.gestiondepartements.Repositories.VoteRepository;
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
    private final DepartmentService departmentService;
    private final NotificationService notificationService;
    private final VoteRepository voteRepository;

    @Override
    @Transactional
    public Utilisateur updateProfile(ProfileDTO profileDTO) {
        Utilisateur user = utilisateurRepository.findByIdWithDepartments(profileDTO.getId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + profileDTO.getId()));

        user.setFirstName(profileDTO.getFirstName());
        user.setLastName(profileDTO.getLastName());
        user.setPhone(profileDTO.getPhone());
        user.setSkills(profileDTO.getSkills());
        user.setLanguages(profileDTO.getLanguages());
        user.setEducation(profileDTO.getEducation());

        Utilisateur savedUser = utilisateurRepository.save(user);

        List<Department> userDepartments = savedUser.getDepartments();
        for (Department department : userDepartments) {
            Vote activeVote = voteRepository.findByDepartmentAndStatus(department, VoteStatus.ACTIVE);

            if (activeVote != null) {
                notificationService.createNotification(
                        savedUser,
                        department,
                        "Vos changements de profil pourraient affecter le vote en cours pour le chef du département '" + department.getName() + "'!",
                        NotificationType.VOTE,
                        activeVote
                );
            }

            if (departmentService.departmentMatchesSkills(department, savedUser)) {
                String message = "Vos compétences mises à jour correspondent maintenant au département '" + department.getName() + "'!";
                notificationService.createNotification(
                        savedUser,
                        department,
                        message,
                        NotificationType.GENERAL,
                        null
                );
            }
        }
        return savedUser;
    }

    @Override
    public ProfileDTO getProfileById(Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return ProfileDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .skills(user.getSkills())
                .languages(user.getLanguages())
                .education(user.getEducation())
                .build();
    }
}