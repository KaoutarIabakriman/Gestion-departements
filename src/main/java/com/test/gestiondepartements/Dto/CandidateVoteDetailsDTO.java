package com.test.gestiondepartements.Dto;

import com.test.gestiondepartements.Entities.VoteStatus;

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
    private Long departmentId;
    private String departmentName;
    private String candidateFullName;
    private String candidateUsername;
    private Long candidateUserId;
    private LocalDateTime declaredAt;
    private long voteCount;
    private List<String> voters;
    private VoteStatus voteStatus;
}