package com.test.gestiondepartements.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnseignantWorkloadDTO {
    private Long enseignantId;
    private String firstName;
    private String lastName;
    private String username;
    private List<String> departmentNames;
    private int totalWorkload;
    private String phone;
    private String education;
    private String skills;
    private String languages;
}