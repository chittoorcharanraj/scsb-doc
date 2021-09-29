package org.recap.matchingalgorithm;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MatchScoreUtilUT extends BaseTestCaseUT {

    MatchScoreUtil matchScoreUtil;

@Test
    public void calculateMatchScore() throws  Exception {
    Integer m1 = 1;
    Integer m2 = 2;
    Integer result = matchScoreUtil.calculateMatchScore(m1,m2);
    assertNotNull(result);
}}
