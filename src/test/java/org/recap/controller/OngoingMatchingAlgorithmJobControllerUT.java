package org.recap.controller;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.recap.BaseTestCaseUT;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.matchingalgorithm.service.MatchingBibInfoDetailService;
import org.recap.model.solr.SolrIndexRequest;
import org.recap.util.DateUtil;
import org.recap.util.OngoingMatchingAlgorithmUtil;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Created by angelind on 13/6/17.
 */
public class OngoingMatchingAlgorithmJobControllerUT extends BaseTestCaseUT {

    @InjectMocks
    OngoingMatchingAlgorithmJobController ongoingMatchingAlgorithmJobController = new OngoingMatchingAlgorithmJobController();

    @Mock
    OngoingMatchingAlgorithmJobController ongoingMatchingAlgoJobController;

    @Mock
    OngoingMatchingAlgorithmUtil ongoingMatchingAlgorithmUtil;

    @Mock
    MatchingBibInfoDetailService matchingBibInfoDetailService;

    @Mock
    DateUtil dateUtil;

    @Mock
    QueryResponse queryResponse;

    private String batchSize="1000";

    @Test
    public void startMatchingAlgorithmJobForBothCgdAndGroupingProcessByBibIdsAndStartProcess() throws Exception {
        SolrIndexRequest solrIndexRequest = getSolrIndexRequest();
        solrIndexRequest.setMatchBy(ScsbConstants.BIB_ID_LIST);
        solrIndexRequest.setBibIds("1");
        Mockito.when(ongoingMatchingAlgoJobController.getBatchSize()).thenCallRealMethod();
        ReflectionTestUtils.setField(ongoingMatchingAlgoJobController,"batchSize",batchSize);
        Mockito.when(ongoingMatchingAlgoJobController.getLogger()).thenCallRealMethod();
        Mockito.when(ongoingMatchingAlgoJobController.getOngoingMatchingAlgorithmUtil()).thenReturn(ongoingMatchingAlgorithmUtil);
        Mockito.when(ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartCgdUpdateProcessBasedOnCriteria(solrIndexRequest, Integer.valueOf(batchSize))).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartGroupingProcessBasedOnCriteria(solrIndexRequest, Integer.valueOf(batchSize))).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest)).thenCallRealMethod();
        String status = ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest);
        assertTrue(status.contains(ScsbCommonConstants.SUCCESS));
    }

    @Test
    public void startMatchingAlgorithmJobForBothCgdAndGroupingProcessByDateRangeAndStartProcess() throws Exception {
        SolrIndexRequest solrIndexRequest = getSolrIndexRequest();
        solrIndexRequest.setMatchBy(ScsbConstants.DATE_RANGE);
        solrIndexRequest.setBibIds("1");
        solrIndexRequest.setDateFrom("06-06-2020 00:00");
        solrIndexRequest.setDateTo("06-06-2020 00:00");
        Mockito.when(ongoingMatchingAlgoJobController.getBatchSize()).thenCallRealMethod();
        ReflectionTestUtils.setField(ongoingMatchingAlgoJobController,"batchSize",batchSize);
        Mockito.when(ongoingMatchingAlgoJobController.getLogger()).thenCallRealMethod();
        Mockito.when(ongoingMatchingAlgoJobController.getOngoingMatchingAlgorithmUtil()).thenReturn(ongoingMatchingAlgorithmUtil);
        Mockito.when(ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartCgdUpdateProcessBasedOnCriteria(solrIndexRequest, Integer.valueOf(batchSize))).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartGroupingProcessBasedOnCriteria(solrIndexRequest, Integer.valueOf(batchSize))).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest)).thenCallRealMethod();
        String status = ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest);
        assertTrue(status.contains(ScsbCommonConstants.SUCCESS));
    }

    @Test
    public void startMatchingAlgorithmJobForFetchUpdatedRecordsByDateRangeAndStartProcessEmptyDate() throws Exception {
        SolrIndexRequest solrIndexRequest = getSolrIndexRequest();
        solrIndexRequest.setMatchBy(ScsbConstants.DATE_RANGE);
        solrIndexRequest.setBibIds("1");
        Mockito.when(ongoingMatchingAlgoJobController.getBatchSize()).thenCallRealMethod();
        //Mockito.when(ongoingMatchingAlgoJobController.getDateUtil()).thenReturn(dateUtil);
        //Mockito.when(dateUtil.getFromDate(Mockito.any())).thenCallRealMethod();
        //Mockito.when(dateUtil.getToDate(Mockito.any())).thenCallRealMethod();
        ReflectionTestUtils.setField(ongoingMatchingAlgoJobController,"batchSize",batchSize);
        Mockito.when(ongoingMatchingAlgoJobController.getLogger()).thenCallRealMethod();
        Mockito.when(ongoingMatchingAlgoJobController.getOngoingMatchingAlgorithmUtil()).thenReturn(ongoingMatchingAlgorithmUtil);
        Mockito.when(ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartCgdUpdateProcessBasedOnCriteria(solrIndexRequest, Integer.valueOf(batchSize))).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartGroupingProcessBasedOnCriteria(solrIndexRequest, Integer.valueOf(batchSize))).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest)).thenCallRealMethod();
        String status = ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest);
        assertTrue(status.contains(ScsbCommonConstants.SUCCESS));
    }

    @Test
    public void startOngoingMatchingAlgorithmJob() throws Exception {
        SolrIndexRequest solrIndexRequest = getSolrIndexRequest();
        solrIndexRequest.setMatchBy(ScsbConstants.FROM_DATE);
        SolrDocument solrDocument = new SolrDocument();
        SolrDocumentList solrDocumentList = new SolrDocumentList();
        solrDocumentList.add(solrDocument);
        Integer rows = Integer.valueOf(batchSize);
        Date date = solrIndexRequest.getCreatedDate();
        Mockito.when(ongoingMatchingAlgoJobController.getOngoingMatchingAlgorithmUtil()).thenReturn(ongoingMatchingAlgorithmUtil);
        Mockito.when(ongoingMatchingAlgoJobController.getDateUtil()).thenReturn(dateUtil);
        Mockito.when(ongoingMatchingAlgoJobController.getBatchSize()).thenReturn(batchSize);
        Mockito.when(ongoingMatchingAlgoJobController.getLogger()).thenCallRealMethod();
        Mockito.when(ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartCgdUpdateProcessBasedOnCriteria(solrIndexRequest, Integer.valueOf(batchSize))).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartGroupingProcessBasedOnCriteria(solrIndexRequest, Integer.valueOf(batchSize))).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest)).thenCallRealMethod();
        String status = ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest);
        assertTrue(status.contains(ScsbCommonConstants.SUCCESS));
        Mockito.when(ongoingMatchingAlgoJobController.getOngoingMatchingAlgorithmUtil()).thenCallRealMethod();
        assertNotEquals(ongoingMatchingAlgorithmUtil, ongoingMatchingAlgoJobController.getOngoingMatchingAlgorithmUtil());
        Mockito.when(ongoingMatchingAlgoJobController.getDateUtil()).thenCallRealMethod();
        assertNotEquals(dateUtil, ongoingMatchingAlgoJobController.getDateUtil());
    }

    private SolrIndexRequest getSolrIndexRequest() {
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        Date processDate = new Date();
        solrIndexRequest.setCreatedDate(processDate);
        solrIndexRequest.setFromDate("2020/06/06");
        solrIndexRequest.setProcessType(ScsbCommonConstants.ONGOING_MATCHING_ALGORITHM_JOB);
        solrIndexRequest.setMaProcessType(ScsbConstants.ONGOING_MA_BOTH_GROUPING_CGD_PROCESS);
        return solrIndexRequest;
    }


    @Test
    public void startOngoingMatchingAlgorithmJobForBibIdRange() throws Exception {
        SolrIndexRequest solrIndexRequest = getSolrIndexRequest();
        solrIndexRequest.setMatchBy(ScsbConstants.BIB_ID_RANGE);
        SolrDocument solrDocument = new SolrDocument();
        SolrDocumentList solrDocumentList = new SolrDocumentList();
        solrDocumentList.add(solrDocument);
        Mockito.when(ongoingMatchingAlgoJobController.getOngoingMatchingAlgorithmUtil()).thenReturn(ongoingMatchingAlgorithmUtil);
        Mockito.when(ongoingMatchingAlgoJobController.getBatchSize()).thenReturn(batchSize);
        Mockito.when(ongoingMatchingAlgoJobController.getLogger()).thenCallRealMethod();
        Mockito.when(ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartCgdUpdateProcessBasedOnCriteria(solrIndexRequest, Integer.valueOf(batchSize))).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartGroupingProcessBasedOnCriteria(solrIndexRequest, Integer.valueOf(batchSize))).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest)).thenCallRealMethod();
        String status = ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest);
        assertTrue(status.contains(ScsbCommonConstants.SUCCESS));
    }

    @Test
    public void startJobToPopulateDataDumpMatchingBibs() {
        Date processDate = new Date();
        Date fromDate = dateUtil.getFromDate(processDate);
        Date toDate = dateUtil.getToDate(processDate);
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setCreatedDate(processDate);
        solrIndexRequest.setFromDate("2020/06/06");
        solrIndexRequest.setMatchBy(ScsbConstants.FROM_DATE);
        solrIndexRequest.setProcessType(ScsbConstants.POPULATE_DATA_FOR_DATA_DUMP_JOB);
        Mockito.when(ongoingMatchingAlgoJobController.getMatchingBibInfoDetailService()).thenReturn(matchingBibInfoDetailService);
        Mockito.when(ongoingMatchingAlgoJobController.getDateUtil()).thenReturn(dateUtil);
        Mockito.when(ongoingMatchingAlgoJobController.getLogger()).thenCallRealMethod();
        Mockito.when(matchingBibInfoDetailService.populateMatchingBibInfo(fromDate, toDate)).thenReturn(ScsbCommonConstants.SUCCESS);
        //Mockito.when(ongoingMatchingAlgoJobController.getBatchSize()).thenReturn(batchSize);
        Mockito.when(ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest)).thenCallRealMethod();
        String status = ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest);
        assertTrue(status.contains(ScsbCommonConstants.SUCCESS));
        Mockito.when(ongoingMatchingAlgoJobController.getMatchingBibInfoDetailService()).thenCallRealMethod();
        assertNotEquals(matchingBibInfoDetailService, ongoingMatchingAlgoJobController.getMatchingBibInfoDetailService());
    }

    @Test
    public void startMatchingAlgorithmJobException() {
        Date processDate = new Date();
        Date fromDate = dateUtil.getFromDate(processDate);
        Date toDate = dateUtil.getToDate(processDate);
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setFromDate("2020/06/06");
        solrIndexRequest.setProcessType(ScsbConstants.POPULATE_DATA_FOR_DATA_DUMP_JOB);
        Mockito.when(ongoingMatchingAlgoJobController.getLogger()).thenCallRealMethod();
        Mockito.when(ongoingMatchingAlgoJobController.getMatchingBibInfoDetailService()).thenReturn(matchingBibInfoDetailService);
        Mockito.when(ongoingMatchingAlgoJobController.getDateUtil()).thenReturn(dateUtil);
        Mockito.when(matchingBibInfoDetailService.populateMatchingBibInfo(fromDate, toDate)).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest)).thenCallRealMethod();
        String status = ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest);
        assertTrue(status.contains(ScsbCommonConstants.SUCCESS));
    }

    @Test
    public void matchingJob(){
        Model model= PowerMockito.mock(Model.class);
        Mockito.when(ongoingMatchingAlgoJobController.matchingJob(model)).thenCallRealMethod();
        String job=ongoingMatchingAlgoJobController.matchingJob(model);
        assertEquals("ongoingMatchingJob",job);
    }

    @Test
    public void startMatchingAlgorithmJobForCgdProcessByBibIdsAndStartProcess() throws Exception {
        SolrIndexRequest solrIndexRequest = getSolrIndexRequest();
        solrIndexRequest.setMaProcessType(ScsbConstants.ONGOING_MA_UPDATE_CGD_PROCESS);
        solrIndexRequest.setMatchBy(ScsbConstants.BIB_ID_LIST);
        solrIndexRequest.setBibIds("1");
        Mockito.when(ongoingMatchingAlgoJobController.getBatchSize()).thenCallRealMethod();
        ReflectionTestUtils.setField(ongoingMatchingAlgoJobController,"batchSize",batchSize);
        Mockito.when(ongoingMatchingAlgoJobController.getLogger()).thenCallRealMethod();
        Mockito.when(ongoingMatchingAlgoJobController.getOngoingMatchingAlgorithmUtil()).thenReturn(ongoingMatchingAlgorithmUtil);
        Mockito.when(ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartCgdUpdateProcessBasedOnCriteria(solrIndexRequest, Integer.valueOf(batchSize))).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest)).thenCallRealMethod();
        String status = ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest);
        assertTrue(status.contains(ScsbCommonConstants.SUCCESS));
    }

    @Test
    public void startMatchingAlgorithmJobForGroupingProcessByBibIdsAndStartProcess() throws Exception {
        SolrIndexRequest solrIndexRequest = getSolrIndexRequest();
        solrIndexRequest.setMaProcessType(ScsbConstants.ONGOING_MA_ONLY_GROUPING);
        solrIndexRequest.setMatchBy(ScsbConstants.BIB_ID_LIST);
        solrIndexRequest.setBibIds("1");
        Mockito.when(ongoingMatchingAlgoJobController.getBatchSize()).thenCallRealMethod();
        ReflectionTestUtils.setField(ongoingMatchingAlgoJobController,"batchSize",batchSize);
        Mockito.when(ongoingMatchingAlgoJobController.getLogger()).thenCallRealMethod();
        Mockito.when(ongoingMatchingAlgoJobController.getOngoingMatchingAlgorithmUtil()).thenReturn(ongoingMatchingAlgorithmUtil);
        Mockito.when(ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartGroupingProcessBasedOnCriteria(solrIndexRequest, Integer.valueOf(batchSize))).thenReturn(ScsbCommonConstants.SUCCESS);
        Mockito.when(ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest)).thenCallRealMethod();
        String status = ongoingMatchingAlgoJobController.startMatchingAlgorithmJob(solrIndexRequest);
        assertTrue(status.contains(ScsbCommonConstants.SUCCESS));
    }

}