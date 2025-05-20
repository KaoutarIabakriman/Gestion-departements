// CandidateVoteDetailsDTO.java
package com.test.gestiondepartements.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateVoteDetailsDTO {
    private Long candidateId;
    private Long voteId;
    private String departmentName;
    private String candidateFullName;
    private String candidateUsername;
    private LocalDateTime declaredAt;
    private long voteCount;
    private List<String> voters;
}