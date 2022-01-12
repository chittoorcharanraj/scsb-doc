package org.recap.model.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by rajeshbabuk on 31/Oct/2021
 */
@Getter
@Setter
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
