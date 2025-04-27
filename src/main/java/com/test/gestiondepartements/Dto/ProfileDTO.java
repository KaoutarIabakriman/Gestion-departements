package com.test.gestiondepartements.Dto;

import lombok.Data;

@Data
public class ProfileDTO  {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String skills;
    private String languages;
    private String education;
}