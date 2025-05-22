package com.test.gestiondepartements.Entities;

import com.test.gestiondepartements.Security.Entities.Utilisateur;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modules")
@Data
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private int workload; // Charge horaire en heures
    public Module() {
        this.workload = 0; // Initialisation à 0
    }

    @ManyToMany(mappedBy = "modules")
    private List<Utilisateur> enseignants = new ArrayList<>();

    public int getWorkload() {
        return workload;
    }

    public void setWorkload(int workload) {
        this.workload = workload;
    }
}