/*

package org.recap.matchingalgorithm;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;

import org.hibernate.validator.constraints.ru.INN;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.recap.BaseTestCase;
import org.recap.BaseTestCaseUT;
import org.recap.ScsbCommonConstants;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.util.StopWatch;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertNotNull;


*/
/**
 * Created by angelind on 18/10/16.
 *//*



public class MatchingSolrQueryAT extends BaseTestCaseUT {




    @Resource
    private SolrTemplate solrTemplate;

    @Test
    public void testFacetQuery() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<String> duplicateOCLCNumbers = new ArrayList<>();
        SolrQuery solrQuery = new SolrQuery("*:* AND DocType:Bib");
        solrQuery.setFacet(true);
        solrQuery.setFields(ScsbCommonConstants.MATCH_POINT_FIELD_OCLC);
        solrQuery.addFacetField(ScsbCommonConstants.MATCH_POINT_FIELD_OCLC);
        solrQuery.setFacetLimit(-1);
        solrQuery.setFacetMinCount(2);
        solrQuery.setRows(0);
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
        List<FacetField> facetFields = queryResponse.getFacetFields();
        for(FacetField facetField : facetFields) {
            List<FacetField.Count> values = facetField.getValues();
            for (Iterator<FacetField.Count> iterator = values.iterator(); iterator.hasNext(); ) {
                FacetField.Count next = iterator.next();
                String name = next.getName();
                duplicateOCLCNumbers.add(name);
            }
        }
        stopWatch.stop();
        System.out.println("Total Time Taken : " + stopWatch.getTotalTimeSeconds());
        assertNotNull(duplicateOCLCNumbers);
    }

}

*/
