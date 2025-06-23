package com.test.gestiondepartements.Entities;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "votes")
@Data
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    private VoteStatus status = VoteStatus.PENDING;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL)
    private List<VoteChoice> choices;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL)
    private List<Candidate> candidates;

    private LocalDateTime endDate;
}