package com.test.gestiondepartements.Dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data

public class DepartmentDTO {
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    private String description;
    private String headOfDepartment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}