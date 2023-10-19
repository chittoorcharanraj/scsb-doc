package org.recap.util;


import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.recap.BaseTestCaseUT;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.jpa.MatchingMatchPointsEntity;
import org.recap.model.search.SearchRecordsRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.SimpleDateFormat;
import java.util.*;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by peris on 9/30/16.
 */
public class SolrQueryBuilderUT extends BaseTestCaseUT {

    @InjectMocks
    SolrQueryBuilder solrQueryBuilder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Mock
    SearchRecordsRequest searchRecordsRequest;

    @Test
    public void getQueryStringForParentCriteriaForChild() throws Exception {
        String[] inputs = {ScsbConstants.TITLE_MATCHED, "test"};
        for (String input : inputs) {
            Mockito.when(searchRecordsRequest.getTitleMatch()).thenReturn(Arrays.asList(input));
            String query = solrQueryBuilder.getQueryStringForParentCriteriaForChild(searchRecordsRequest);
            assertNotNull(query);
        }
    }

    @Test
    public void getQueryStringForMatchChildReturnParent() throws Exception {
        String[] inputs = {ScsbConstants.TITLE_MATCHED, "test"};
        for (String input : inputs) {
            Mockito.when(searchRecordsRequest.getTitleMatch()).thenReturn(Arrays.asList(input));
            String query = solrQueryBuilder.getQueryStringForMatchChildReturnParent(searchRecordsRequest);
            assertNotNull(query);
        }
    }

    @Test
    public void getQueryForParentAndChildCriteria() throws Exception {
        SearchRecordsRequest[] searchRecordsRequests = {getSearchRecordsRequest("Title_search", "Scotland"), getSearchRecordsRequest("", "Scotland"), getSearchRecordsRequest("", "")};
        for (SearchRecordsRequest searchRecordsRequest :
                searchRecordsRequests) {
            SolrQuery quryForAllFieldsNoValue = solrQueryBuilder.getQueryForParentAndChildCriteria(searchRecordsRequest);
            assertNotNull(quryForAllFieldsNoValue);
        }
    }

    @Test
    public void getQueryForChildAndParentCriteria() throws Exception {
        SearchRecordsRequest[] searchRecordsRequests = {getSearchRecordsRequest("BibLastUpdated", "2016-10-21T14:30Z TO NOW"), getSearchRecordsRequest(ScsbCommonConstants.BARCODE, "123125123"), getSearchRecordsRequest(ScsbCommonConstants.CALL_NUMBER, "1234")};
        for (SearchRecordsRequest searchRecordsRequest :
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
    public void getDeletedQueryForDataDump() {
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setFieldName("BibLastUpdatedDate");
        searchRecordsRequest.setFieldValue("2016-10-21T14:30Z TO NOW");
        searchRecordsRequest.getOwningInstitutions().addAll(Arrays.asList("CUL", "PUL"));
        searchRecordsRequest.getMaterialTypes().addAll(Arrays.asList("Monograph", "Serial", "Other"));
        searchRecordsRequest.setImsDepositoryCodes(Arrays.asList("RECAP"));
        SolrQuery queryForAllFieldsNoValue = solrQueryBuilder.getDeletedQueryForDataDump(searchRecordsRequest, true);
        assertNotNull(queryForAllFieldsNoValue);
    }

    @Test
    public void fetchBibsForCGDProcess() throws Exception {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = formatter.format(date);
        boolean b = true;
        solrQueryBuilder.fetchBibsForCGDProcess(strDate, b);
    }
    @Test
    public void fetchBibsForCGDProcess1() throws Exception {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/");
        String strDate = formatter.format(date);
        boolean b = false;
        solrQueryBuilder.fetchBibsForCGDProcess(strDate, b);
    }


    @Test
    public void fetchCreatedOrUpdatedBibs() {
        String fetchCreatedOrUpdatedBibs = solrQueryBuilder.fetchBibsForGroupingProcess("2016-10-21T14:30Z TO NOW", Boolean.TRUE);
        assertNotNull(fetchCreatedOrUpdatedBibs);
    }

    @Test
    public void fetchCreatedOrUpdatedBibs1() {
        String fetchCreatedOrUpdatedBibs = solrQueryBuilder.fetchBibsForGroupingProcess("", Boolean.FALSE);
        assertNotNull(fetchCreatedOrUpdatedBibs);
    }

    @Test
    public void fetchMatchingQualifiedBibs() {
        String fetchCreatedOrUpdatedBibs = solrQueryBuilder.fetchMatchingQualifiedBibs();
        assertNotNull(fetchCreatedOrUpdatedBibs);
    }

    @Test
    public void fetchBibsByBibIdRange() {
        String fetchBibsByBibIdRange = solrQueryBuilder.fetchBibsByBibIdRange("1", "2");
        assertNotNull(fetchBibsByBibIdRange);
    }

    @Test
    public void getQueryForParentAndChildCriteriaForDataDump() {
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setFieldName("BibLastUpdatedDate");
        searchRecordsRequest.setFieldValue("2016-10-21T14:30Z TO NOW");
        searchRecordsRequest.getOwningInstitutions().addAll(Arrays.asList("CUL", "PUL"));
        searchRecordsRequest.getMaterialTypes().addAll(Arrays.asList("Monograph", "Serial", "Other"));
        SolrQuery solrQuery = solrQueryBuilder.getQueryForParentAndChildCriteriaForDataDump(searchRecordsRequest);
        assertNotNull(solrQuery);
    }

    @Test
    public void solrQueryToFetchBibDetails() {
        List<MatchingMatchPointsEntity> matchingMatchPointsEntities = new ArrayList<>();
        MatchingMatchPointsEntity matchingMatchPointsEntity = new MatchingMatchPointsEntity();
        matchingMatchPointsEntity.setCriteriaValue("\\");
        matchingMatchPointsEntity.setCriteriaValueCount(1);
        matchingMatchPointsEntities.add(matchingMatchPointsEntity);
        List<String> matchCriteriaValues = new ArrayList<>();
        SolrQuery solrQueryToFetchBibDetails = solrQueryBuilder.solrQueryToFetchBibDetails(matchingMatchPointsEntities, matchCriteriaValues, "");
        assertNotNull(solrQueryToFetchBibDetails);
    }

    @Test
    public void solrQueryToFetchMatchedRecords() {
        solrQueryBuilder.solrQueryToFetchMatchedRecords();
    }

    @Test
    public void buildSolrQueryForDeaccessionReports() {
        SolrQuery buildSolrQueryForDeaccessionReports = solrQueryBuilder.buildSolrQueryForDeaccessionReports("2016-10-21T14:30Z TO NOW", "PUL", true, "Private");
        assertNotNull(buildSolrQueryForDeaccessionReports);
    }

    @Test
    public void getDeletedQueryForDataDumpNonPrivate() {
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setFieldName(ScsbCommonConstants.TITLE_STARTS_WITH);
        searchRecordsRequest.setFieldValue("test");
        searchRecordsRequest.setAvailability(null);
        searchRecordsRequest.setOwningInstitutions(null);
        searchRecordsRequest.getMaterialTypes().addAll(Arrays.asList("Monograph", "Serial", "Other"));
        searchRecordsRequest.setImsDepositoryCodes(Arrays.asList("RECAP"));
        SolrQuery queryForAllFieldsNoValue = solrQueryBuilder.getDeletedQueryForDataDump(searchRecordsRequest, false);
        assertNotNull(queryForAllFieldsNoValue);
    }

    @Test
    public void getCountQueryForParentAndChildCriteria() {
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setFieldName("test");
        searchRecordsRequest.setFieldValue("test");
        searchRecordsRequest.setAvailability(null);
        searchRecordsRequest.setOwningInstitutions(null);
        searchRecordsRequest.getMaterialTypes().addAll(Arrays.asList("Monograph", "Serial", "Other"));
        SolrQuery queryForAllFieldsNoValue = solrQueryBuilder.getCountQueryForParentAndChildCriteria(searchRecordsRequest);
        SolrQuery query = solrQueryBuilder.buildSolrQueryToGetBibDetails(Arrays.asList(1), 1);
        assertEquals(ScsbConstants.BIB_DOC_TYPE, query.getQuery());
        SolrQuery queryBib = solrQueryBuilder.getSolrQueryForBibItem("test");
        assertEquals("test", queryBib.getQuery());
        assertNotNull(queryForAllFieldsNoValue);
    }

    @Test
    public void getCountQueryForChildAndParentCriteria() {
        String[] names = {ScsbCommonConstants.CALL_NUMBER, "test", "", ScsbCommonConstants.TITLE_STARTS_WITH};
        for (String name : names) {
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
    public void getQueryForParentAndChildCriteriaForDeletedDataDump() {
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setFieldName("name");
        searchRecordsRequest.setFieldValue("2016-10-21T14:30Z TO NOW \\ ?*+{}[]'^~()!$%#./@");
        searchRecordsRequest.setAvailability(null);
        searchRecordsRequest.setOwningInstitutions(null);
        SolrQuery queryForParentAndChildCriteriaForDeletedDataDump = solrQueryBuilder.getQueryForParentAndChildCriteriaForDeletedDataDump(searchRecordsRequest);
        assertNotNull(queryForParentAndChildCriteriaForDeletedDataDump);
        String solrQueryForOngoingMatching = solrQueryBuilder.solrQueryForOngoingMatching("fieldName", Arrays.asList("1\\"));
        assertNotNull(solrQueryForOngoingMatching);
        String solrQueryForOngoingMatching1 = solrQueryBuilder.solrQueryForOngoingMatching("fieldName", ("1\\"));
        assertNotNull(solrQueryForOngoingMatching1);
    }

    @Test
    @DisplayName("Test Build Build solr query for cgd reports")
    public void buildSolrQueryForCGDReports() {
        SolrQuery solrQuery = solrQueryBuilder.buildSolrQueryForCGDReports("PUL", ScsbCommonConstants.SHARED_CGD);
        assertNotNull(solrQuery);
        assertTrue(solrQuery.getQuery().contains(ScsbCommonConstants.SHARED_CGD));
    }

    @Test
    @DisplayName("Test Build solr query for deaccesion report information")
    public void buildSolrQueryForDeaccesionReportInformation() {
        SolrQuery solrQuery = solrQueryBuilder.buildSolrQueryForDeaccesionReportInformation(new Date().toString(), "PUL", true);
        assertNotNull(solrQuery);
        assertTrue(solrQuery.getQuery().contains("PUL"));
    }

    @Test
    @DisplayName("Test Build solr query for Incomplete reports")
    public void buildSolrQueryForIncompleteReports() {
        SolrQuery solrQuery = solrQueryBuilder.buildSolrQueryForIncompleteReports("PUL");
        assertNotNull(solrQuery);
        assertTrue(solrQuery.getQuery().contains(ScsbConstants.ITEM_STATUS_INCOMPLETE));
    }

    @Test
    @DisplayName("Test Build solr query for accession reports")
    public void buildSolrQueryForAccessionReports() {
        SolrQuery solrQuery = solrQueryBuilder.buildSolrQueryForAccessionReports(new Date().toString(), "PUL", true, ScsbCommonConstants.SHARED_CGD);
        assertNotNull(solrQuery);
        assertTrue(solrQuery.getQuery().contains(ScsbCommonConstants.SHARED_CGD));
    }

    @Test
    @DisplayName("Test Solr query for initial matching for listed matchingpointvale")
    public void solrQueryForInitialMatchingList() {
        String solrQuery = solrQueryBuilder.solrQueryForInitialMatching(ScsbCommonConstants.BARCODE, Arrays.asList("\\123456"));
        assertNotNull(solrQuery);
        assertTrue(solrQuery.contains("123456"));
    }

    @Test
    @DisplayName("Test Solr query for initial matching")
    public void solrQueryForInitialMatching() {
        String solrQuery = solrQueryBuilder.solrQueryForInitialMatching(ScsbCommonConstants.BARCODE, "\\123456");
        assertNotNull(solrQuery);
        assertTrue(solrQuery.contains("123456"));
    }

    @Test
    public void buildQueryTitleMatchedReport() throws Exception {
        String date = "2016-10-21T14:30Z TO NOW";
        String owningInst = "PUL";
        List<String> cgds = new ArrayList<>();
        cgds.add("OPEN");
        String matchingIdentifier = "TEST";
        String match = "Matched";
        solrQueryBuilder.buildQueryTitleMatchedReport(date, owningInst, cgds, matchingIdentifier, match);
    }

    @Test
    public void getQueryForOngoingMatchingForGroupingOrCgdUpdateProcess() throws Exception {
        boolean includeMaQualifier = true;
        boolean isCgdProcess = true;
        solrQueryBuilder.getQueryForOngoingMatchingForGroupingOrCgdUpdateProcess(includeMaQualifier, isCgdProcess);


    }

    @Test
    public void getQueryForOngoingMatchingBasedOnDateForGroupingOrCgdUpdateProcess() throws Exception {
        String date = "30/05/1978";
        boolean includeMaQualifier = true;
        boolean isCgdProcess = true;
        solrQueryBuilder.getQueryForOngoingMatchingBasedOnDateForGroupingOrCgdUpdateProcess(date, includeMaQualifier, isCgdProcess);
    }

    @Test
    public void getQueryForOngoingMatchingBasedOnDateRangeForGroupingOrCgdUpdateProcess() throws Exception {
        String fromDate = "FromDate";
        String toDate = "DateRange";
        boolean includeMaQualifier = true;
        boolean isCgdProcess = true;
        solrQueryBuilder.getQueryForOngoingMatchingBasedOnDateRangeForGroupingOrCgdUpdateProcess(fromDate, toDate, includeMaQualifier, isCgdProcess);
    }

    @Test
    public void getQueryForOngoingMatchingBasedOnBibIdsForGroupingOrCgdUpdateProcess() throws Exception {
        String bibIds = "1";
        boolean includeMaQualifier = true;
        boolean isCgdProcess = true;
        solrQueryBuilder.getQueryForOngoingMatchingBasedOnBibIdsForGroupingOrCgdUpdateProcess(bibIds, includeMaQualifier, isCgdProcess);

    }

    @Test
    public void getQueryForOngoingMatchingBasedOnBibIdRangeForGroupingOrCgdUpdateProcess() throws Exception {
        String fromBibId = "1";
        String toBibId = "10";
        boolean includeMaQualifier = true;
        boolean isCgdProcess = true;
        solrQueryBuilder.getQueryForOngoingMatchingBasedOnBibIdRangeForGroupingOrCgdUpdateProcess(fromBibId, toBibId, includeMaQualifier, isCgdProcess);
    }



@Test
public void getCoreParentFilterQueryForGroupingProcess() throws Exception
{
    ReflectionTestUtils.invokeMethod(solrQueryBuilder,"getCoreParentFilterQueryForGroupingProcess");
}

@Test
    public void  fetchCreatedOrUpdatedBibsByDateRange() throws  Exception
{
    String fromDate = "21/02/2018";
    String toDate = "21/02/2019";
    solrQueryBuilder.fetchCreatedOrUpdatedBibsByDateRange(fromDate,toDate);
}

@Test
public void fetchBibsByBibIds() throws Exception
    {
        String bibIds = "1,1";
        solrQueryBuilder.fetchBibsByBibIds(bibIds);
    }

    @Test
    public void buildQueryTitleMatchCount() throws Exception
    {
        String date = "21/02/2018";
        String owningInst = "PUL";
        String cgd = "Share";
        String matchingIdentifier = "1";
        solrQueryBuilder.buildQueryTitleMatchCount(date,owningInst,cgd,matchingIdentifier);
    }

    @Test
    public void buildQueryForTitleMatchReportPreviewAndExport() throws Exception
    {
        StringBuilder matchingIdentifierAppendResult = new StringBuilder();
        matchingIdentifierAppendResult.append("test");
        solrQueryBuilder.buildQueryForTitleMatchReportPreviewAndExport(matchingIdentifierAppendResult);
    }
    @Test
    public void getQueryForCGDs() throws Exception
    {
        List<String> cgds = new ArrayList<>();
        cgds.add("");
        ReflectionTestUtils.invokeMethod(solrQueryBuilder,"getQueryForCGDs",cgds);
    }
    }
