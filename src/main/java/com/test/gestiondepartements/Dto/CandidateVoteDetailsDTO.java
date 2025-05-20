// Path: src/main/java/com/test/gestiondepartements/Dto/CandidateVoteDetailsDTO.java
package com.test.gestiondepartements.Dto;

import com.test.gestiondepartements.Entities.VoteStatus; // Import VoteStatus
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
    private Long departmentId; // Added
    private String departmentName;
    private String candidateFullName;
    private String candidateUsername;
    private Long candidateUserId; // Added to easily get the user ID
    private LocalDateTime declaredAt;
    private long voteCount;
    private List<String> voters;
    private VoteStatus voteStatus; // Added
}