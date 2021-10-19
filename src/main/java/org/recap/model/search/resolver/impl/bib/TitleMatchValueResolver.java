package org.recap.model.search.resolver.impl.bib;

import org.recap.model.search.resolver.BibValueResolver;
import org.recap.model.solr.BibItem;

/**
 * Created by rajeshbabuk on 28/Sep/2021
 */
public class TitleMatchValueResolver implements BibValueResolver {


    @Override
    public Boolean isInterested(String field) {
        return "Title_match".equalsIgnoreCase(field);
    }

    @Override
    public void setValue(BibItem bibItem, Object value) {
        bibItem.setTitleMatch(((String) value).toLowerCase());
    }
}
