package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.ProfileDTO;
import com.test.gestiondepartements.Security.Entities.Utilisateur;
import com.test.gestiondepartements.Security.Repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public Utilisateur updateProfile(ProfileDTO profileDTO) {
        Utilisateur user = utilisateurRepository.findById(profileDTO.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setFirstName(profileDTO.getFirstName());
        user.setLastName(profileDTO.getLastName());
        user.setPhone(profileDTO.getPhone());
        user.setSkills(profileDTO.getSkills());
        user.setLanguages(profileDTO.getLanguages());
        user.setEducation(profileDTO.getEducation());

        return utilisateurRepository.save(user);
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