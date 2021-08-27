package org.recap.util;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.jpa.MatchingMatchPointsEntity;
import org.recap.model.search.SearchRecordsRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by peris on 9/30/16.
 */
public class SolrQueryBuilderUT extends BaseTestCaseUT {

    @InjectMocks
    SolrQueryBuilder solrQueryBuilder;

    @Mock
    SearchRecordsRequest searchRecordsRequest;

    @Test
    public void getQueryStringForParentCriteriaForChild() throws Exception {
        String[] inputs={ScsbConstants.TITLE_MATCHED,"test"};
        for (String input:inputs) {
            Mockito.when(searchRecordsRequest.getTitleMatch()).thenReturn(Arrays.asList(input));
            String query = solrQueryBuilder.getQueryStringForParentCriteriaForChild(searchRecordsRequest);
            assertNotNull(query);
        }
    }

    @Test
    public void getQueryStringForMatchChildReturnParent() throws Exception {
        String[] inputs={ScsbConstants.TITLE_MATCHED,"test"};
        for (String input:inputs) {
            Mockito.when(searchRecordsRequest.getTitleMatch()).thenReturn(Arrays.asList(input));
            String query = solrQueryBuilder.getQueryStringForMatchChildReturnParent(searchRecordsRequest);
            assertNotNull(query);
        }
    }

    @Test
    public void getQueryForParentAndChildCriteria() throws Exception {
        SearchRecordsRequest[] searchRecordsRequests = {getSearchRecordsRequest("Title_search","Scotland"),getSearchRecordsRequest("","Scotland"),getSearchRecordsRequest("","")};
        for (SearchRecordsRequest searchRecordsRequest:
                searchRecordsRequests) {
            SolrQuery quryForAllFieldsNoValue = solrQueryBuilder.getQueryForParentAndChildCriteria(searchRecordsRequest);
            assertNotNull(quryForAllFieldsNoValue);
        }
    }

    @Test
    public void getQueryForChildAndParentCriteria() throws Exception {
        SearchRecordsRequest[] searchRecordsRequests = {getSearchRecordsRequest("BibLastUpdated","2016-10-21T14:30Z TO NOW"),getSearchRecordsRequest(ScsbCommonConstants.BARCODE,"123125123"),getSearchRecordsRequest(ScsbCommonConstants.CALL_NUMBER,"1234")};
        for (SearchRecordsRequest searchRecordsRequest:
        searchRecordsRequests) {
            SolrQuery quryForAllFieldsNoValue = solrQueryBuilder.getQueryForChildAndParentCriteria(searchRecordsRequest);
            assertNotNull(quryForAllFieldsNoValue);
        }
    }

    private SearchRecordsRequest getSearchRecordsRequest(String name, String value) {
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setFieldName(name);
        searchRecordsRequest.setFieldValue(value);
        searchRecordsRequest.getOwningInstitutions().addAll(Arrays.asList("CUL", "PUL"));
        searchRecordsRequest.getCollectionGroupDesignations().addAll(Arrays.asList("Shared", "Private", "Open"));
        searchRecordsRequest.getAvailability().addAll(Arrays.asList("Available", "Not Available"));
        searchRecordsRequest.getUseRestrictions().addAll(Arrays.asList("No Restrictions", "In Library Use", "Supervised Use"));
        searchRecordsRequest.getMaterialTypes().addAll(Arrays.asList("Monograph", "Serial", "Other"));
        return searchRecordsRequest;
    }

    @Test
    public void getDeletedQueryForDataDump(){
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setFieldName("BibLastUpdatedDate");
        searchRecordsRequest.setFieldValue("2016-10-21T14:30Z TO NOW");
        searchRecordsRequest.getOwningInstitutions().addAll(Arrays.asList("CUL", "PUL"));
        searchRecordsRequest.getMaterialTypes().addAll(Arrays.asList("Monograph", "Serial", "Other"));
        searchRecordsRequest.setImsDepositoryCodes(Arrays.asList("RECAP"));
        SolrQuery queryForAllFieldsNoValue = solrQueryBuilder.getDeletedQueryForDataDump(searchRecordsRequest,true);
        assertNotNull(queryForAllFieldsNoValue);
    }

    @Test
    public void fetchCreatedOrUpdatedBibs(){
        String fetchCreatedOrUpdatedBibs = solrQueryBuilder.fetchCreatedOrUpdatedBibs("2016-10-21T14:30Z TO NOW");
        assertNotNull(fetchCreatedOrUpdatedBibs);
    }

    @Test
    public void fetchBibsByBibIdRange(){
        String fetchBibsByBibIdRange = solrQueryBuilder.fetchBibsByBibIdRange("1","2");
        assertNotNull(fetchBibsByBibIdRange);
    }

    @Test
    public void getQueryForParentAndChildCriteriaForDataDump(){
        SearchRecordsRequest searchRecordsRequest=new SearchRecordsRequest();
        searchRecordsRequest.setFieldName("BibLastUpdatedDate");
        searchRecordsRequest.setFieldValue("2016-10-21T14:30Z TO NOW");
        searchRecordsRequest.getOwningInstitutions().addAll(Arrays.asList("CUL", "PUL"));
        searchRecordsRequest.getMaterialTypes().addAll(Arrays.asList("Monograph", "Serial", "Other"));
        SolrQuery solrQuery = solrQueryBuilder.getQueryForParentAndChildCriteriaForDataDump(searchRecordsRequest);
        assertNotNull(solrQuery);
    }

    @Test
    public void solrQueryToFetchBibDetails(){
        List<MatchingMatchPointsEntity> matchingMatchPointsEntities=new ArrayList<>();
        MatchingMatchPointsEntity matchingMatchPointsEntity=new MatchingMatchPointsEntity();
        matchingMatchPointsEntity.setCriteriaValue("\\");
        matchingMatchPointsEntity.setCriteriaValueCount(1);
        matchingMatchPointsEntities.add(matchingMatchPointsEntity);
        List<String> matchCriteriaValues=new ArrayList<>();
        SolrQuery solrQueryToFetchBibDetails = solrQueryBuilder.solrQueryToFetchBibDetails(matchingMatchPointsEntities,matchCriteriaValues,"");
        assertNotNull(solrQueryToFetchBibDetails);
    }

    @Test
    public void buildSolrQueryForDeaccessionReports(){
        SolrQuery buildSolrQueryForDeaccessionReports = solrQueryBuilder.buildSolrQueryForDeaccessionReports("2016-10-21T14:30Z TO NOW","PUL",true,"Private");
        assertNotNull(buildSolrQueryForDeaccessionReports);
    }

    @Test
    public void getDeletedQueryForDataDumpNonPrivate(){
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setFieldName(ScsbCommonConstants.TITLE_STARTS_WITH);
        searchRecordsRequest.setFieldValue("test");
        searchRecordsRequest.setAvailability(null);
        searchRecordsRequest.setOwningInstitutions(null);
        searchRecordsRequest.getMaterialTypes().addAll(Arrays.asList("Monograph", "Serial", "Other"));
        searchRecordsRequest.setImsDepositoryCodes(Arrays.asList("RECAP"));
        SolrQuery queryForAllFieldsNoValue = solrQueryBuilder.getDeletedQueryForDataDump(searchRecordsRequest,false);
        assertNotNull(queryForAllFieldsNoValue);
    }

    @Test
    public void getCountQueryForParentAndChildCriteria(){
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setFieldName("test");
        searchRecordsRequest.setFieldValue("test");
        searchRecordsRequest.setAvailability(null);
        searchRecordsRequest.setOwningInstitutions(null);
        searchRecordsRequest.getMaterialTypes().addAll(Arrays.asList("Monograph", "Serial", "Other"));
        SolrQuery queryForAllFieldsNoValue = solrQueryBuilder.getCountQueryForParentAndChildCriteria(searchRecordsRequest);
        SolrQuery query = solrQueryBuilder.buildSolrQueryToGetBibDetails(Arrays.asList(1),1);
        assertEquals(ScsbConstants.BIB_DOC_TYPE,query.getQuery());
        SolrQuery queryBib = solrQueryBuilder.getSolrQueryForBibItem("test");
        assertEquals("test",queryBib.getQuery());
        assertNotNull(queryForAllFieldsNoValue);
    }

    @Test
    public void getCountQueryForChildAndParentCriteria(){
        String[] names={ScsbCommonConstants.CALL_NUMBER,"test","",ScsbCommonConstants.TITLE_STARTS_WITH};
        for (String name: names) {
            SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
            searchRecordsRequest.setFieldName(name);
            searchRecordsRequest.setFieldValue("2016-10-21T14:30Z TO NOW");
            searchRecordsRequest.setAvailability(null);
            searchRecordsRequest.setOwningInstitutions(null);
            searchRecordsRequest.getMaterialTypes().addAll(Arrays.asList("Monograph", "Serial", "Other"));
            SolrQuery queryForAllFieldsNoValue = solrQueryBuilder.getCountQueryForChildAndParentCriteria(searchRecordsRequest);
            assertNotNull(queryForAllFieldsNoValue);
        }
    }
    @Test
    public void getQueryForParentAndChildCriteriaForDeletedDataDump(){
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setFieldName("name");
        searchRecordsRequest.setFieldValue("2016-10-21T14:30Z TO NOW \\ ?*+{}[]'^~()!$%#./@");
        searchRecordsRequest.setAvailability(null);
        searchRecordsRequest.setOwningInstitutions(null);
        SolrQuery queryForParentAndChildCriteriaForDeletedDataDump=solrQueryBuilder.getQueryForParentAndChildCriteriaForDeletedDataDump(searchRecordsRequest);
        assertNotNull(queryForParentAndChildCriteriaForDeletedDataDump);
        String solrQueryForOngoingMatching=solrQueryBuilder.solrQueryForOngoingMatching("fieldName",Arrays.asList("1\\"));
        assertNotNull(solrQueryForOngoingMatching);
        String solrQueryForOngoingMatching1=solrQueryBuilder.solrQueryForOngoingMatching("fieldName",("1\\"));
        assertNotNull(solrQueryForOngoingMatching1);
    }

    @Test
    @DisplayName("Test Build Build solr query for cgd reports")
    public void buildSolrQueryForCGDReports(){
        SolrQuery solrQuery=solrQueryBuilder.buildSolrQueryForCGDReports("PUL",ScsbCommonConstants.SHARED_CGD);
        assertNotNull(solrQuery);
        assertTrue(solrQuery.getQuery().contains(ScsbCommonConstants.SHARED_CGD));
    }

    @Test
    @DisplayName("Test Build solr query for deaccesion report information")
    public void buildSolrQueryForDeaccesionReportInformation(){
        SolrQuery solrQuery=solrQueryBuilder.buildSolrQueryForDeaccesionReportInformation(new Date().toString(),"PUL",true);
        assertNotNull(solrQuery);
        assertTrue(solrQuery.getQuery().contains("PUL"));
    }

    @Test
    @DisplayName("Test Build solr query for Incomplete reports")
    public void buildSolrQueryForIncompleteReports(){
        SolrQuery solrQuery=solrQueryBuilder.buildSolrQueryForIncompleteReports("PUL");
        assertNotNull(solrQuery);
        assertTrue(solrQuery.getQuery().contains(ScsbConstants.ITEM_STATUS_INCOMPLETE));
    }

    @Test
    @DisplayName("Test Build solr query for accession reports")
    public void buildSolrQueryForAccessionReports(){
        SolrQuery solrQuery=solrQueryBuilder.buildSolrQueryForAccessionReports(new Date().toString(),"PUL",true,ScsbCommonConstants.SHARED_CGD);
        assertNotNull(solrQuery);
        assertTrue(solrQuery.getQuery().contains(ScsbCommonConstants.SHARED_CGD));
    }

    @Test
    @DisplayName("Test Solr query for initial matching for listed matchingpointvale")
    public void solrQueryForInitialMatchingList(){
        String solrQuery=solrQueryBuilder.solrQueryForInitialMatching(ScsbCommonConstants.BARCODE,Arrays.asList("\\123456"));
        assertNotNull(solrQuery);
        assertTrue(solrQuery.contains("123456"));
    }

    @Test
    @DisplayName("Test Solr query for initial matching")
    public void solrQueryForInitialMatching(){
        String solrQuery=solrQueryBuilder.solrQueryForInitialMatching(ScsbCommonConstants.BARCODE,"\\123456");
        assertNotNull(solrQuery);
        assertTrue(solrQuery.contains("123456"));
    }

}