package org.recap.repository.solr.temp;

import org.recap.model.solr.Item;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.repository.support.SimpleSolrRepository;

/**
 * Created by angelind on 15/6/16.
 */
public class ItemCrudRepositoryMultiCoreSupport extends SimpleSolrRepository<Item, String> {

    /**
     * This method Instantiates a new item crud repository multi core support.
     *
     * @param solrOperations solr Operations
     * @param entityClass  solr entity class
     */
    public ItemCrudRepositoryMultiCoreSupport(SolrOperations solrOperations, Class<Item> entityClass) {
        super(solrOperations, entityClass);
    }
}
