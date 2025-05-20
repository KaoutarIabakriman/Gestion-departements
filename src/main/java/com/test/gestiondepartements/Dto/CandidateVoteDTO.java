package com.test.gestiondepartements.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateVoteDTO {
    private Long candidateId;
    private Long voteId;
    private String departmentName;
    private String candidateFullName;
    private String candidateUsername;
    private LocalDateTime declaredAt;
    private long voteCount;
}