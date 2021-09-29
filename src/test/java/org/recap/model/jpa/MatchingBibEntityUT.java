package org.recap.model.jpa;

import org.junit.jupiter.api.Test;
import org.recap.BaseTestCaseUT;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MatchingBibEntityUT extends BaseTestCaseUT
{
    @Test
    public void MatchingBibEntity()
    {
        String status = "IN";
        String setroot = "test";
        MatchingBibEntity matchingBibEntity = new MatchingBibEntity();
        matchingBibEntity.setRoot(setroot);
        matchingBibEntity.setStatus(status);

        assertNotNull(matchingBibEntity.getRoot());
        assertNotNull(matchingBibEntity.getStatus());
    }
}

