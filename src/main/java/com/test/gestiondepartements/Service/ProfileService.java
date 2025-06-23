package com.test.gestiondepartements.Service;

import com.test.gestiondepartements.Dto.ProfileDTO;
import com.test.gestiondepartements.Security.Entities.Utilisateur;

public interface ProfileService {
    Utilisateur updateProfile(ProfileDTO profileDTO);
    ProfileDTO getProfileById(Long userId);
}

