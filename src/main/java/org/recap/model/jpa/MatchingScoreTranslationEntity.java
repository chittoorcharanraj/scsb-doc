package org.recap.model.jpa;

import lombok.Data;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "matching_score_translation_t")
@Data
public class MatchingScoreTranslationEntity {
    @Id
    @Column(
            name = "ID"
    )
    private Integer id;
    @Column(
            name = "DECIMAL_MA_SCORE"
    )
    private Integer decMaScore;
    @Column(
            name = "BINARY_MA_SCORE"
    )
    private String binMaScore;
    @Column(
            name = "STRING_MA_SCORE"
    )
    private String stringMaScore;
}
