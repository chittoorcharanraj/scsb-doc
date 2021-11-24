package org.recap.util;


import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.recap.BaseTestCase;
import org.recap.BaseTestCaseUT;
import org.recap.BaseTestCaseUT4;
import org.recap.model.solr.SolrIndexRequest;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

public class OngoingMatchingAlgorithmQueryUtilUT extends BaseTestCaseUT4 {


    @InjectMocks
    OngoingMatchingAlgorithmQueryUtil ongoingMatchingAlgorithmQueryUtil;

    @Mock
    DateUtil dateUtil;

    @Mock
    SolrQueryBuilder solrQueryBuilder;

    @Mock
    SolrTemplate solrTemplate;

    @Test
    public void  prepareQueryForongoingMatchingGroupingOrCgdUpdateProcessBasedOnCriteria() throws Exception {

        String toDate = "10/01/2010";
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Date oldDate = (Date)formatter.parse(toDate);
        String toDate1 = "2010/01/12 21:10";
        DateFormat formatter1 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date oldDate1 = (Date)formatter.parse(toDate1);
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setMatchBy("test");
        solrIndexRequest.setIncludeMaQualifier(true);
        solrIndexRequest.setFromDate("2012/12/01");
        solrIndexRequest.setToDate(oldDate);
        solrIndexRequest.setDateTo("DateRange");
        solrIndexRequest.setFromDate("FromDate");
        solrIndexRequest.setDateFrom("DateRange");
        boolean isCgdProcess = true;
     String query =  ongoingMatchingAlgorithmQueryUtil.prepareQueryForOngoingMatchingGroupingOrCgdUpdateProcessBasedOnCriteria(solrIndexRequest,isCgdProcess);
    }

    @Test
    public void  prepareQueryForOngoingMatchingGroupingOrCgdUpdateProcessBasedOncriteria() throws Exception {
        try {
            SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
            solrIndexRequest.setMatchBy("FromDate");
            solrIndexRequest.setIncludeMaQualifier(true);
            solrIndexRequest.setFromDate("1998/04/24");
            boolean isCgdProcess = true;
            String query = ongoingMatchingAlgorithmQueryUtil.prepareQueryForOngoingMatchingGroupingOrCgdUpdateProcessBasedOnCriteria(solrIndexRequest, isCgdProcess);
        } catch (Exception e) {
        }
    }
    @Test
    public void  prepareQueryForOngoingMatchingGroupingorCgdUpdateProcessBasedOnCriteria() throws Exception {
try {
    SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
    solrIndexRequest.setMatchBy("DateRange");
    solrIndexRequest.setIncludeMaQualifier(true);
    boolean isCgdProcess = true;
    String query = ongoingMatchingAlgorithmQueryUtil.prepareQueryForOngoingMatchingGroupingOrCgdUpdateProcessBasedOnCriteria(solrIndexRequest, isCgdProcess);
}catch (Exception e){}
    }

    @Test
    public void  prepareQueryForOngoingMatchingGroupingOrCgdUpdateProcessBasedOnCriteria() throws Exception {

        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setMatchBy("BibIdList");
        solrIndexRequest.setIncludeMaQualifier(true);
        boolean isCgdProcess = true;
        String query =  ongoingMatchingAlgorithmQueryUtil.prepareQueryForOngoingMatchingGroupingOrCgdUpdateProcessBasedOnCriteria(solrIndexRequest,isCgdProcess);
    }

    @Test
    public void  prepareQueryForOngoingMatchinggroupingOrCgdUpdateProcessBasedOnCriteria() throws Exception {

        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setMatchBy("BibIdRange");
        solrIndexRequest.setIncludeMaQualifier(true);
        boolean isCgdProcess = true;
        String query =  ongoingMatchingAlgorithmQueryUtil.prepareQueryForOngoingMatchingGroupingOrCgdUpdateProcessBasedOnCriteria(solrIndexRequest,isCgdProcess);
    }
    @Test
    public  void getFormattedDateTo() throws Exception
    {
        try {
            String dateTo = "dd-MM-yyyy hh:mm";
            ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmQueryUtil, "getFormattedDateTo", dateTo);
        }catch (Exception e){}
    }

    @Test
    public void getUTCFormatDateString() throws Exception {

            String sDate1 = "31/12/1998";
            Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
            ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmQueryUtil, "getUTCFormatDateString", date1);
    }

    @Test
    public  void fetchDataByQuery() throws Exception {
        try {
            String query = "test";
            Integer batchSize = 1000;
            Integer start = 1;
            SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
            SolrClient solrClient = PowerMockito.mock(SolrClient.class);
            PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
            QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
            Mockito.when(solrTemplate.getSolrClient().query(any(), SolrRequest.METHOD.POST)).thenReturn(queryResponse);
            ongoingMatchingAlgorithmQueryUtil.fetchDataByQuery(query, batchSize, start);
        } catch (Exception e) {
        }
    }
}
