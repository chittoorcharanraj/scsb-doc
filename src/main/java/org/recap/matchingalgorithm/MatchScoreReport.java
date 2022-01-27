package org.recap.matchingalgorithm;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchScoreReport {
    List<Integer> bibIds;
    Integer matchScore;
}
