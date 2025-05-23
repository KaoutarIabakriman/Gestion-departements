package com.test.gestiondepartements.Dto;

import jakarta.validation.constraints.NotEmpty;

public class LoginDTO {
    @NotEmpty(message = "L'email ne peut pas être vide")
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}