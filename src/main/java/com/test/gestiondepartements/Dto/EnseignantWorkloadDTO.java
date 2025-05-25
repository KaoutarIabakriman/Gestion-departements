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
    private String username; // email
    private List<String> departmentNames;
    private int totalWorkload;
}