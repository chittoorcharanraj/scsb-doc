package org.recap.model.search.resolver.impl.bib;

import org.recap.model.search.resolver.BibValueResolver;
import org.recap.model.solr.BibItem;

/**
 * Created by dinakar on 8/27/21.
 */
public class MAQualifierValueResolver implements BibValueResolver {
    @Override
    public Boolean isInterested(String field) {
        return "MAQualifier".equalsIgnoreCase(field);
    }

    @Override
    public void setValue(BibItem bibItem, Object value) {
        bibItem.setMaQualifier((Integer) value);
    }
}
