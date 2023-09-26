package org.recap.repository.solr.main;

import org.recap.model.solr.Holdings;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.solr.repository.SolrCrudRepository;

import java.util.List;

/**
 * Created by rajeshbabuk on 13/9/16.
 */
public interface HoldingsSolrCrudRepository extends SolrCrudRepository<Holdings, String>, CrudRepository<Holdings, String> {

    /**
     * Finds holdings based on the given holdings id in solr.
     *
     * @param holdingsId the holdings id
     * @return the holdings
     */
    Holdings findByHoldingsId(Integer holdingsId);

    /**
     * Deletes holdings based on the given holdings id in solr.
     *
     * @param holdingsId the holdings id
     * @return the int
     */
    int deleteByHoldingsId(@Param("holdingsId") Integer holdingsId);

    /**
     * Deletes holdings based on the given list of holdings id in solr.
     *
     * @param holdingsIds the holdings ids
     * @return the int
     */
    int deleteByHoldingsIdIn(@Param("holdingsIds") List<Integer> holdingsIds);

    /**
     * Delete by owning institution int.
     *
     * @param owningInstitution the owning institution
     * @return the int
     */
    int deleteByOwningInstitution(@Param("owningInstitution") String owningInstitution);
}
