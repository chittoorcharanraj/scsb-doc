package org.recap.model.jpa;

import org.junit.jupiter.api.Test;
import org.recap.BaseTestCaseUT;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MatchingMatchPointsEntityUT extends BaseTestCaseUT
{
    @Test
    public void MatchingMatchPointsEntity()
    {
        String matchCriteria = "singlematch";
        MatchingMatchPointsEntity matchingMatchPointsEntity = new MatchingMatchPointsEntity();
        matchingMatchPointsEntity.setMatchCriteria(matchCriteria);
        assertNotNull(matchingMatchPointsEntity.getMatchCriteria());

    }
}
