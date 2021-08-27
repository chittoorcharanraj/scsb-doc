package org.recap.repository.jpa;

import org.recap.model.jpa.MatchingScoreTranslationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchingScoreTranslationRepository extends CrudRepository<MatchingScoreTranslationEntity,Integer> {
}
