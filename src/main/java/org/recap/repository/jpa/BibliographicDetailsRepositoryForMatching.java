package org.recap.repository.jpa;

import org.recap.model.jpa.BibliographicEntityForMatching;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by rajeshbabuk on 31/Oct/2021
 */
public interface BibliographicDetailsRepositoryForMatching extends JpaRepository<BibliographicEntityForMatching, Integer> {
    List<BibliographicEntityForMatching> findByOwningInstitutionIdInAndBibliographicIdIn(List<Integer> owningInstitutionIds, List<Integer> bibliographicIds);
    List<BibliographicEntityForMatching> findByOwningInstitutionIdInAndMatchingIdentityIn(List<Integer> allInstitutionIdsExceptSupportInstitution, List<String> matchingIdentifiers);
    List<BibliographicEntityForMatching> findByOwningInstitutionIdInAndMatchingIdentity(List<Integer> owningInstitutionIds, String matchingIdentity);
}
