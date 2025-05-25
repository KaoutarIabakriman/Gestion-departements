package com.test.gestiondepartements.Entities;

import com.test.gestiondepartements.Security.Entities.Utilisateur;
import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

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

    private int workload;
    public Module() {
        this.workload = 0;
    }

    @ManyToMany(mappedBy = "modules")
    private Set<Utilisateur> enseignants = new HashSet<>();

    public int getWorkload() { // Public access is essential!
        return workload;
    }

    public void setWorkload(int workload) {
        this.workload = workload;
    }
}