package org.recap.matchingalgorithm;

import org.junit.Test;
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
}
@Test
    public void getMatchScoreForMatchPoint() throws Exception
{
    String matchpoints = "OCLCNumber";
    Integer result = matchScoreUtil.getMatchScoreForMatchPoint(matchpoints);
}

    @Test
    public void getMatchScoreForMatchpoint() throws Exception
    {
        String matchpoints = "ISBN";
        Integer result = matchScoreUtil.getMatchScoreForMatchPoint(matchpoints);
    }
    @Test
    public void getMatchscoreFormatchpoint() throws Exception
    {
        String matchpoints = "LCCN";
        Integer result = matchScoreUtil.getMatchScoreForMatchPoint(matchpoints);
    }
    @Test
    public void getmatchscoreFormatchpoint() throws Exception
    {
        String matchpoints = "ISSN";
        Integer result = matchScoreUtil.getMatchScoreForMatchPoint(matchpoints);
    }

    @Test
    public void getmatchScoreFormatchpoint() throws Exception
    {
        String matchpoints = "TITLE";
        Integer result = matchScoreUtil.getMatchScoreForMatchPoint(matchpoints);
    }

}
