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
import java.util.Optional;

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
            Optional<Vote> activeVoteOptional = Optional.ofNullable(voteRepository.findByDepartmentAndStatus(department, VoteStatus.ACTIVE));
            if (activeVoteOptional.isPresent()) {
                Vote activeVote = activeVoteOptional.get();
                notificationService.createNotification(
                        savedUser, department, "Vos changements...", NotificationType.VOTE, activeVote
                );
            }

            if (departmentService.departmentMatchesSkills(department, savedUser)) {
                // ... (your notification code)
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