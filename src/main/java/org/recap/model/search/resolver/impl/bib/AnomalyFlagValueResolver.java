package org.recap.model.search.resolver.impl.bib;

import org.recap.model.search.resolver.BibValueResolver;
import org.recap.model.solr.BibItem;

/**
 * Created by dinakar on 8/27/21.
 */
public class AnomalyFlagValueResolver implements BibValueResolver {
    @Override
    public Boolean isInterested(String field) {
        return "AnamolyFlag".equalsIgnoreCase(field);
    }

    @Override
    public void setValue(BibItem bibItem, Object value) {
        bibItem.setAnamolyFlag((Boolean) value);
    }
}