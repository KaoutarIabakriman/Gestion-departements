// File: src/main/java/com/test/gestiondepartements/Entities/ModuleRequest.java
package com.test.gestiondepartements.Entities;

import com.test.gestiondepartements.Security.Entities.Utilisateur;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "module_requests")
@Data
@NoArgsConstructor
public class ModuleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id", nullable = false)
    private Utilisateur enseignant;

    @CreationTimestamp
    @Column(name = "request_date", updatable = false)
    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ModuleRequestStatus status = ModuleRequestStatus.PENDING;

    public ModuleRequest(Module module, Utilisateur enseignant) {
        this.module = module;
        this.enseignant = enseignant;
    }
}