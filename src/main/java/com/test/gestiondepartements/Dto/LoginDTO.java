package com.test.gestiondepartements.Dto;

import jakarta.validation.constraints.NotEmpty;

public class LoginDTO {
    @NotEmpty(message = "L'email ne peut pas être vide")
    private String username;

    @NotEmpty(message = "Le mot de passe ne peut pas être vide")
    private String password;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}