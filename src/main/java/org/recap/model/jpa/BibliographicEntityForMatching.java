package org.recap.model.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Created by rajeshbabuk on 31/Oct/2021
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "bibliographic_t", catalog = "")
@DynamicUpdate
public class BibliographicEntityForMatching {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "BIBLIOGRAPHIC_ID")
    private Integer bibliographicId;

    @Column(name = "OWNING_INST_ID")
    private Integer owningInstitutionId;

    @Column(name = "MATCHING_IDENTITY")
    private String matchingIdentity;

    @Column(name = "MATCH_SCORE")
    private int matchScore;

    @Column(name = "ANAMOLY_FLAG")
    private boolean anamolyFlag;
}
