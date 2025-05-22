package com.test.gestiondepartements.Entities;

import com.test.gestiondepartements.Security.Entities.Utilisateur;
import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet; // Importez HashSet
import java.util.Set;    // Importez Set

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
    // Constructeur par défaut pour initialisation de workload
    public Module() {
        this.workload = 0;
    }

    @ManyToMany(mappedBy = "modules") // Ou votre configuration @JoinTable si Module est propriétaire de la relation
    private Set<Utilisateur> enseignants = new HashSet<>(); // MODIFIÉ: List -> Set, ArrayList -> HashSet

    // Assurez-vous que les getters et setters générés par Lombok ou manuels
    // correspondent à Set<Utilisateur>
    // public Set<Utilisateur> getEnseignants() { return enseignants; }
    // public void setEnseignants(Set<Utilisateur> enseignants) { this.enseignants = enseignants; }


    // workload getter/setter est déjà là
    public int getWorkload() {
        return workload;
    }

    public void setWorkload(int workload) {
        this.workload = workload;
    }
}