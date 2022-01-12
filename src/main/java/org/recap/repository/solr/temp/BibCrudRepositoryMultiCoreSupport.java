package org.recap.repository.solr.temp;

import org.recap.model.solr.Bib;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.repository.support.SimpleSolrRepository;

/**
 * Created by pvsubrah on 6/14/16.
 */
public class BibCrudRepositoryMultiCoreSupport extends SimpleSolrRepository<Bib, String> {

    /**
     * This method instantiates a new bib crud repository to perform crud operations on temp cores.
     *
     * @param solrOperations solr Operations
     * @param entityClass  solr entity class
     */
    public BibCrudRepositoryMultiCoreSupport(SolrOperations solrOperations, Class<Bib> entityClass) {
        super(solrOperations, entityClass);
    }
}
