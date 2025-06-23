package com.test.gestiondepartements.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO  {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String skills;
    private String languages;
    private String education;
}