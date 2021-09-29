package org.recap.model.jpa;

import org.junit.jupiter.api.Test;
import org.recap.BaseTestCaseUT;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MatchingScoreTranslationEntityUT extends BaseTestCaseUT
{
    @Test
    public void MatchingScoreTranslationEntity() {
        Integer id = 1;
        Integer decMaScore = 2;
        String binMaScore = "test";
        String stringMaScore = "test";

        MatchingScoreTranslationEntity matchingScoreTranslationEntity = new MatchingScoreTranslationEntity();
        matchingScoreTranslationEntity.setId(id);
        matchingScoreTranslationEntity.setDecMaScore(decMaScore);
        matchingScoreTranslationEntity.setBinMaScore(binMaScore);
        matchingScoreTranslationEntity.setStringMaScore(stringMaScore);

        assertNotNull(matchingScoreTranslationEntity.getId());
        assertNotNull(matchingScoreTranslationEntity.getBinMaScore());
        assertNotNull(matchingScoreTranslationEntity.getDecMaScore());
        assertNotNull(matchingScoreTranslationEntity.getStringMaScore());

    }
}
