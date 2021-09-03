package org.recap.model.jpa;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GeneratorType;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "matching_score_translation_t")
@Getter
@Setter
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
