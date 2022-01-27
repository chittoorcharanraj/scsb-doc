package org.recap.model.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by angelind on 27/10/16.
 */
@Entity
@Table(name = "MATCHING_MATCHPOINTS_T", catalog = "")
@Data
@EqualsAndHashCode(callSuper = false)
public class MatchingMatchPointsEntity extends AbstractEntity<Integer> {
    @Column(name = "MATCH_CRITERIA")
    private String matchCriteria;

    @Column(name = "CRITERIA_VALUE")
    private String criteriaValue;

    @Column(name = "CRITERIA_VALUE_COUNT")
    private Integer criteriaValueCount;
}
