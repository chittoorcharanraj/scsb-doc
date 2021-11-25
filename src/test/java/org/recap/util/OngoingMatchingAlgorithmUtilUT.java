package org.recap.util;

import org.apache.camel.ProducerTemplate;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.bouncycastle.asn1.bc.PbkdMacIntegrityCheck;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.recap.BaseTestCaseUT4;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.matchingalgorithm.MatchScoreUtil;
import org.recap.matchingalgorithm.MatchingAlgorithmCGDProcessor;
import org.recap.matchingalgorithm.MatchingCounter;
import org.recap.matchingalgorithm.service.OngoingMatchingReportsService;
import org.recap.model.jpa.*;
import org.recap.model.matchingreports.MatchingSummaryReport;
import org.recap.model.solr.BibItem;
import org.recap.model.solr.SolrIndexRequest;
import org.recap.repository.jpa.*;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Created by angelind on 6/2/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({SolrTemplate.class,SolrClient.class})
public class OngoingMatchingAlgorithmUtilUT extends BaseTestCaseUT4 {


    @InjectMocks
    OngoingMatchingAlgorithmUtil ongoingMatchingAlgorithmUtil;


    @Mock
    MatchingAlgorithmUtil mockedmatchingAlgorithmUtil;


    @Mock
    OngoingMatchingReportsService ongoingMatchingReportsService;

    @Mock
    SolrQueryBuilder solrQueryBuilder;

    @Mock
    CommonUtil commonUtil;

    @Mock
    CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Mock
    ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    @Mock
    InstitutionDetailsRepository institutionDetailsRepository;

    @Mock
    ProducerTemplate producerTemplate;

    @Mock
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Mock
    ItemDetailsRepository itemDetailsRepository;

    @Mock
    MatchingAlgorithmCGDProcessor matchingAlgorithmCGDProcessor;

    @Mock
    UpdateCgdUtil updateCgdUtil;

    @Mock
    MatchingCounter matchingCounter;

    @Mock
    MatchingAlgorithmReportDataDetailsRepository reportDataDetailsRepository;

    @Mock
    ReportDetailRepository reportDetailRepository;

    @Mock
    MatchingAlgorithmReportDetailRepository matchingAlgorithmReportDetailRepository;

    @Mock
    MatchingAlgorithmReportEntity unMatchReportEntity;

    @Mock
    OngoingMatchingAlgorithmQueryUtil ongoingMatchingAlgorithmQueryUtil;

    @Mock
    SolrDocument solrDocument;

    @Mock
    BibliographicDetailsRepositoryForMatching bibliographicDetailsRepositoryForMatching;

    @Mock
    DateUtil  dateUtil;


    @Test
    public void processCGDAndReportsForUnMatchingTitles() throws Exception {
        Map<String, String> titleMap = new HashMap<>();
        Set<String> unMatchingTitleHeaderSet = new HashSet<>();
        unMatchingTitleHeaderSet.add("1");
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        Mockito.doCallRealMethod().when(mockedmatchingAlgorithmUtil).prepareReportForUnMatchingTitles(Mockito.anyMap(), Mockito.anyList(), Mockito.anyList(), Mockito.anyList(), Mockito.anyList(), Mockito.anySet(), Mockito.anyList(), Mockito.anyList(), Mockito.anyList(), Mockito.anyList(), Mockito.anyList());
        Mockito.when(mockedmatchingAlgorithmUtil.buildReportEntity(Mockito.anyString())).thenReturn(unMatchReportEntity);
        MatchingAlgorithmReportEntity unMatchReportEntity1 = ongoingMatchingAlgorithmUtil.processCGDAndReportsForUnMatchingTitles("filename", titleMap, Arrays.asList(1), Arrays.asList("shared"), Arrays.asList("pul"), Arrays.asList("1"), "matchPointValue", unMatchingTitleHeaderSet, "matchPointString");
        assertNotNull(unMatchReportEntity1);
    }



    @Test
    public void fetchUpdatedRecordsByDateRangeAndStartProcess() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "commonUtil", commonUtil);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        String[] matchPoints = {ScsbCommonConstants.OCLC_NUMBER, ScsbCommonConstants.ISBN_CRITERIA, ScsbCommonConstants.ISSN_CRITERIA, ScsbCommonConstants.LCCN_CRITERIA};
        for (String matchPoint : matchPoints) {
            SolrDocumentList solrDocumentList = getSolrDocumentsSingle(matchPoint);
            PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
            Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
            Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
            Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(ScsbCommonConstants.OCLC_NUMBER, "test")).thenReturn("testquery");
            Mockito.when(solrQueryBuilder.fetchCreatedOrUpdatedBibsByDateRange(any(), any())).thenCallRealMethod();
            Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(SolrDocument.class), Mockito.anyCollection(), any(BibItem.class))).thenReturn(getBibItemSingle("PUL", 1, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("CUL", 2, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("NYPL", 3, ScsbCommonConstants.MONOGRAPH));
//            String status = ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsByDateRangeAndStartProcess(new Date(), new Date(), 1,true);
            assertNotNull(ScsbCommonConstants.SUCCESS);
        }
    }

    @Test
    public void fetchUpdatedRecordsByBibIdsAndStartProcess() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "commonUtil", commonUtil);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        String[] matchPoints = {ScsbCommonConstants.OCLC_NUMBER, ScsbCommonConstants.ISBN_CRITERIA, ScsbCommonConstants.ISSN_CRITERIA, ScsbCommonConstants.LCCN_CRITERIA};
        for (String matchPoint : matchPoints) {
            SolrDocumentList solrDocumentList = getSolrDocumentsSingle(matchPoint);
            PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
            Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
            Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
            Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(ScsbCommonConstants.OCLC_NUMBER, "test")).thenReturn("testquery");
            Mockito.when(solrQueryBuilder.fetchBibsByBibIds(Mockito.anyString())).thenCallRealMethod();
            Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(SolrDocument.class), Mockito.anyCollection(), any(BibItem.class))).thenReturn(getBibItemSingle("PUL", 1, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("CUL", 2, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("NYPL", 3, ScsbCommonConstants.MONOGRAPH));
            //  String status = ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsByBibIdsAndStartProcess("1",1,true);
            assertNotNull(ScsbCommonConstants.SUCCESS);
        }
    }

    @Test
    public void fetchUpdatedRecordsByBibIdRangeAndStartProcess() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "commonUtil", commonUtil);
        String[] matchPoints = {ScsbCommonConstants.OCLC_NUMBER, ScsbCommonConstants.ISBN_CRITERIA, ScsbCommonConstants.ISSN_CRITERIA, ScsbCommonConstants.LCCN_CRITERIA};
        for (String matchPoint : matchPoints) {
            SolrDocumentList solrDocumentList = getSolrDocumentsSingle(matchPoint);
            Mockito.when(solrQueryBuilder.fetchBibsByBibIdRange(Mockito.anyString(), Mockito.anyString())).thenCallRealMethod();
            PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
            Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
            Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
            Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(SolrDocument.class), Mockito.anyCollection(), any(BibItem.class))).thenReturn(getBibItemSingle("PUL", 1, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("CUL", 2, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("NYPL", 3, ScsbCommonConstants.MONOGRAPH));
            // String status = ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsByBibIdRangeAndStartProcess("1", "3", 1,true);
            assertNotNull(ScsbCommonConstants.SUCCESS);
        }
    }

    @Test
    public void fetchUpdatedRecordsByBibIdRangeAndStartProcessMultiMatch() throws Exception {
        List<String> nonHoldingInstitutionList = new ArrayList<>();
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "commonUtil", commonUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "nonHoldingInstitutionList", nonHoldingInstitutionList);
        SolrDocumentList solrDocumentList = getSolrDocumentsMulti();
        Mockito.when(solrQueryBuilder.fetchBibsByBibIdRange(Mockito.anyString(), Mockito.anyString())).thenReturn("test");
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(SolrDocument.class), Mockito.anyCollection(), any(BibItem.class))).thenReturn(getBibItemSingle("PUL", 1, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("CUL", 2, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("NYPL", 3, ScsbCommonConstants.MONOGRAPH));
        List<BibliographicEntity> bibliographicEntities = new ArrayList<>();
        BibliographicEntity bibliographicEntity = getBibliographicEntity(1);
        List<HoldingsEntity> holdingsEntities = new ArrayList<>();
        HoldingsEntity holdingsEntity = getHoldingsEntity(1);
        holdingsEntities.add(holdingsEntity);
        bibliographicEntity.setHoldingsEntities(holdingsEntities);
        List<ItemEntity> itemEntities1 = new ArrayList<>();
        ItemEntity itemEntity = getItemEntity(1);
        ItemEntity itemEntity1 = getItemEntity(1);
        itemEntities1.add(itemEntity);
        itemEntities1.add(itemEntity1);
        bibliographicEntity.setItemEntities(itemEntities1);
        bibliographicEntities.add(bibliographicEntity);
        Mockito.when(bibliographicDetailsRepository.findByIdIn(Mockito.anyList())).thenReturn(bibliographicEntities);
        //     String status = ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsByBibIdRangeAndStartProcess("1", "3", 1,true);
        assertNotNull(ScsbCommonConstants.SUCCESS);
    }

    @Test
    public void fetchDataForOngoingMatchingBasedOnBibIdRangeException() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "commonUtil", commonUtil);
        SolrDocumentList solrDocumentList = getSolrDocumentsMulti();
        Mockito.when(solrQueryBuilder.fetchBibsByBibIdRange(Mockito.anyString(), Mockito.anyString())).thenReturn("test");
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenThrow(SolrServerException.class);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        //QueryResponse queryResponses = ongoingMatchingAlgorithmUtil.fetchDataForOngoingMatchingBasedOnBibIdRange("1", "3", 1,0);
        //  assertNull(queryResponses);
    }

    @Test
    public void fetchDataForOngoingMatchingBasedOnDateException() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "commonUtil", commonUtil);
        SolrDocumentList solrDocumentList = getSolrDocumentsMulti();
        Mockito.when(solrQueryBuilder.fetchBibsByBibIdRange(Mockito.anyString(), Mockito.anyString())).thenReturn("test");
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenThrow(SolrServerException.class);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        //  QueryResponse queryResponses = ongoingMatchingAlgorithmUtil.fetchDataForOngoingMatchingBasedOnDate("date", 1,0,true,true);
        // assertNull(queryResponses);
    }

    @Test
    public void getBibsFromSolrException() throws Exception {
        Map<Integer, BibItem> existingBibItemMap = new HashMap<>();
        BibItem bibItem = new BibItem();
        existingBibItemMap.put(1, bibItem);
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        SolrDocumentList solrDocumentList = getSolrDocumentsMulti();
        Mockito.when(solrQueryBuilder.fetchBibsByBibIdRange(Mockito.anyString(), Mockito.anyString())).thenReturn("test");
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenThrow(SolrServerException.class);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        Set<String> matchPointString = new HashSet<>();
        matchPointString.add("match");
        Map<Integer, BibItem> bibItemMap = ongoingMatchingAlgorithmUtil.getBibsFromSolr("test", "fieldName", 1, existingBibItemMap);

        assertNotNull(bibItemMap);
    }

    @Test
    public void fetchUpdatedRecordsAndStartProcessSingleMatch() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        Date processDate = new Date();
        Date fromDate = getFromDate(processDate);
        Set<String> unMatchingTitleHeaderSet = new HashSet<>();
        unMatchingTitleHeaderSet.add("test");
        List<CollectionGroupEntity> collectionGroupEntities = getCollectionGroupEntities();
        List<InstitutionEntity> institutionEntities = getInstitutionEntities();
        List<Integer> itemIds = new ArrayList<>();
        itemIds.add(1);
        List<String> inst = new ArrayList<>();
        inst.add("PUL");
        List<MatchingSummaryReport> matchingSummaryReports = new ArrayList<>();
        MatchingSummaryReport matchingSummaryReport = new MatchingSummaryReport();
        matchingSummaryReport.setInstitution("PUL");
        matchingSummaryReport.setOpenItemsDiff("2");
        matchingSummaryReports.add(0, matchingSummaryReport);
        String[] matchPoints = {ScsbCommonConstants.OCLC_NUMBER, ScsbCommonConstants.ISBN_CRITERIA, ScsbCommonConstants.ISSN_CRITERIA, ScsbCommonConstants.LCCN_CRITERIA};
        for (String matchPoint : matchPoints) {
            SolrDocumentList solrDocumentList = getSolrDocumentsSingle(matchPoint);
            ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
            ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "ongoingMatchingReportsService", ongoingMatchingReportsService);
            ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrQueryBuilder", solrQueryBuilder);
            ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "commonUtil", commonUtil);
            ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "collectionGroupDetailsRepository", collectionGroupDetailsRepository);
            PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
            Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
            Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
            Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(SolrDocument.class), Mockito.anyCollection(), any(BibItem.class))).thenReturn(getBibItemSingle("PUL", 1, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("CUL", 2, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("NYPL", 3, ScsbCommonConstants.MONOGRAPH));
            Mockito.doCallRealMethod().when(mockedmatchingAlgorithmUtil).populateMatchingCounter();
            Mockito.when(solrQueryBuilder.fetchBibsForGroupingProcess(Mockito.anyString(), Mockito.anyBoolean())).thenReturn("testquery");
            Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(ScsbCommonConstants.OCLC_NUMBER, "test")).thenReturn("testquery");
            Mockito.when(mockedmatchingAlgorithmUtil.getMatchingAndUnMatchingBibsOnTitleVerification(Mockito.anyMap())).thenReturn(unMatchingTitleHeaderSet);
            Mockito.when(collectionGroupDetailsRepository.findAll()).thenReturn(collectionGroupEntities);
            Mockito.when(institutionDetailsRepository.findAll()).thenReturn(institutionEntities);
            Mockito.when(commonUtil.findAllInstitutionCodesExceptSupportInstitution()).thenReturn(inst);
            Mockito.doNothing().when(mockedmatchingAlgorithmUtil).populateMatchingCounter();
            Mockito.when(ongoingMatchingReportsService.populateSummaryReportBeforeMatching()).thenReturn(matchingSummaryReports);
            ongoingMatchingAlgorithmUtil.updateCGDForItemInSolr(itemIds);
            List<ReportDataEntity> reportDataEntitiesToUpdate = new ArrayList<>();
            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntity.setHeaderName("Test");
            reportDataEntity.setHeaderValue("Test");
            reportDataEntitiesToUpdate.add(reportDataEntity);
            //   String status = ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartProcess(fromDate, 1, Boolean.TRUE, true);
            assertNotNull(ScsbCommonConstants.SUCCESS);

        }
    }

    @Test
    public void fetchUpdatedRecordsAndStartProcessSingleMatchForNonMonograph() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        SolrQuery solrQuery = new SolrQuery("testquery");
        solrQuery.setStart(1);
        solrQuery.setRows(1);
        Date processDate = new Date();
        Date fromDate = getFromDate(processDate);
        Set<String> unMatchingTitleHeaderSet = new HashSet<>();
        unMatchingTitleHeaderSet.add("test");
        List<String> inst = new ArrayList<>();
        inst.add("PUL");
        List<MatchingSummaryReport> matchingSummaryReports = new ArrayList<>();
        MatchingSummaryReport matchingSummaryReport = new MatchingSummaryReport();
        matchingSummaryReport.setInstitution("PUL");
        matchingSummaryReport.setOpenItemsDiff("2");
        matchingSummaryReports.add(0, matchingSummaryReport);
        List<CollectionGroupEntity> collectionGroupEntities = getCollectionGroupEntities();
        List<InstitutionEntity> institutionEntities = getInstitutionEntities();
        List<Integer> itemIds = new ArrayList<>();
        itemIds.add(1);
        SolrDocumentList solrDocumentList = getSolrDocumentsSingle(ScsbCommonConstants.OCLC_NUMBER);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "ongoingMatchingReportsService", ongoingMatchingReportsService);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrQueryBuilder", solrQueryBuilder);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "commonUtil", commonUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "collectionGroupDetailsRepository", collectionGroupDetailsRepository);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(SolrDocument.class), Mockito.anyCollection(), any(BibItem.class))).thenReturn(getBibItemSingle("PUL", 1, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("CUL", 2, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("NYPL", 3, ScsbCommonConstants.MONOGRAPH));
        Mockito.doCallRealMethod().when(mockedmatchingAlgorithmUtil).populateMatchingCounter();
        Mockito.when(solrQueryBuilder.fetchBibsForGroupingProcess(Mockito.anyString(), Mockito.anyBoolean())).thenReturn("testquery");
        Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(ScsbCommonConstants.OCLC_NUMBER, "test")).thenReturn("testquery");
        Mockito.when(mockedmatchingAlgorithmUtil.getMatchingAndUnMatchingBibsOnTitleVerification(Mockito.anyMap())).thenReturn(unMatchingTitleHeaderSet);
        Mockito.when(collectionGroupDetailsRepository.findAll()).thenReturn(collectionGroupEntities);
        Mockito.when(institutionDetailsRepository.findAll()).thenReturn(institutionEntities);
        Mockito.when(commonUtil.findAllInstitutionCodesExceptSupportInstitution()).thenReturn(inst);
        Mockito.doNothing().when(mockedmatchingAlgorithmUtil).populateMatchingCounter();
        Mockito.when(ongoingMatchingReportsService.populateSummaryReportBeforeMatching()).thenReturn(matchingSummaryReports);
        List<BibliographicEntity> bibliographicEntities = new ArrayList<>();
        BibliographicEntity bibliographicEntity = getBibliographicEntity(1);
        List<HoldingsEntity> holdingsEntities = new ArrayList<>();
        HoldingsEntity holdingsEntity = getHoldingsEntity(1);
        holdingsEntities.add(holdingsEntity);
        bibliographicEntity.setHoldingsEntities(holdingsEntities);
        List<ItemEntity> itemEntities1 = new ArrayList<>();
        ItemEntity itemEntity = getItemEntity(1);
        ItemEntity itemEntity1 = getItemEntity(1);
        itemEntities1.add(itemEntity);
        itemEntities1.add(itemEntity1);
        bibliographicEntity.setItemEntities(itemEntities1);
        bibliographicEntities.add(bibliographicEntity);
        Mockito.when(bibliographicDetailsRepository.findByIdIn(Mockito.anyList())).thenReturn(bibliographicEntities);
        ongoingMatchingAlgorithmUtil.updateCGDForItemInSolr(itemIds);
        //   String status = ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartProcess(fromDate, 1, Boolean.TRUE, true);
        assertNotNull(ScsbCommonConstants.SUCCESS);
    }

    @Test
    public void fetchUpdatedRecordsAndStartProcessSingleMatchForNonMonographException() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        SolrQuery solrQuery = new SolrQuery("testquery");
        solrQuery.setStart(1);
        solrQuery.setRows(1);
        Date processDate = new Date();
        Date fromDate = getFromDate(processDate);
        Set<String> unMatchingTitleHeaderSet = new HashSet<>();
        unMatchingTitleHeaderSet.add("test");
        List<CollectionGroupEntity> collectionGroupEntities = getCollectionGroupEntities();
        List<InstitutionEntity> institutionEntities = getInstitutionEntities();
        List<Integer> itemIds = new ArrayList<>();
        itemIds.add(1);
        List<String> inst = new ArrayList<>();
        inst.add("PUL");
        List<MatchingSummaryReport> matchingSummaryReports = new ArrayList<>();
        MatchingSummaryReport matchingSummaryReport = new MatchingSummaryReport();
        matchingSummaryReport.setInstitution("PUL");
        matchingSummaryReport.setOpenItemsDiff("2");
        matchingSummaryReports.add(0, matchingSummaryReport);
        SolrDocumentList solrDocumentList = getSolrDocumentsSingle(ScsbCommonConstants.OCLC_NUMBER);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "ongoingMatchingReportsService", ongoingMatchingReportsService);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrQueryBuilder", solrQueryBuilder);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "commonUtil", commonUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "collectionGroupDetailsRepository", collectionGroupDetailsRepository);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(SolrDocument.class), Mockito.anyCollection(), any(BibItem.class))).thenReturn(getBibItemSingle("PUL", 1, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("CUL", 2, ScsbCommonConstants.MONOGRAPH)).thenReturn(getBibItemSingle("NYPL", 3, ScsbCommonConstants.MONOGRAPH));
        Mockito.doCallRealMethod().when(mockedmatchingAlgorithmUtil).populateMatchingCounter();
        Mockito.when(solrQueryBuilder.fetchBibsForGroupingProcess(Mockito.anyString(), Mockito.anyBoolean())).thenReturn("testquery");
        Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(ScsbCommonConstants.OCLC_NUMBER, "test")).thenReturn("testquery");
        Mockito.when(mockedmatchingAlgorithmUtil.getMatchingAndUnMatchingBibsOnTitleVerification(Mockito.anyMap())).thenReturn(unMatchingTitleHeaderSet);
        Mockito.when(collectionGroupDetailsRepository.findAll()).thenReturn(collectionGroupEntities);
        Mockito.when(institutionDetailsRepository.findAll()).thenReturn(institutionEntities);
        Mockito.when(commonUtil.findAllInstitutionCodesExceptSupportInstitution()).thenReturn(inst);
        Mockito.doNothing().when(mockedmatchingAlgorithmUtil).populateMatchingCounter();
        Mockito.when(ongoingMatchingReportsService.populateSummaryReportBeforeMatching()).thenReturn(matchingSummaryReports);
        List<BibliographicEntity> bibliographicEntities = new ArrayList<>();
        BibliographicEntity bibliographicEntity = getBibliographicEntity(1);
        List<HoldingsEntity> holdingsEntities = new ArrayList<>();
        HoldingsEntity holdingsEntity = getHoldingsEntity(1);
        holdingsEntities.add(holdingsEntity);
        bibliographicEntity.setHoldingsEntities(holdingsEntities);
        List<ItemEntity> itemEntities1 = new ArrayList<>();
        ItemEntity itemEntity = getItemEntity(1);
        ItemEntity itemEntity1 = getItemEntity(1);
        itemEntities1.add(itemEntity);
        itemEntities1.add(itemEntity1);
        bibliographicEntity.setItemEntities(itemEntities1);
        bibliographicEntities.add(bibliographicEntity);
        Mockito.when(bibliographicDetailsRepository.findByIdIn(Mockito.anyList())).thenReturn(bibliographicEntities);
        Mockito.doThrow(NullPointerException.class).when(mockedmatchingAlgorithmUtil).getReportDataEntity(Mockito.anyString(), Mockito.anyString(), Mockito.anyList());
        ongoingMatchingAlgorithmUtil.updateCGDForItemInSolr(itemIds);
        //  String status = ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartProcess(fromDate, 1, Boolean.TRUE, true);
        assertNotNull(ScsbCommonConstants.SUCCESS);
    }

    private BibliographicEntity getBibliographicEntity(int inst) {
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("bibContent".getBytes());
        bibliographicEntity.setOwningInstitutionId(inst);
        Random random = new Random();
        String owningInstitutionBibId = String.valueOf(random.nextInt());
        bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("tst");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setLastUpdatedBy("tst");
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setId(inst);
        if (inst == 1) {
            institutionEntity.setInstitutionName("PUL");
            institutionEntity.setInstitutionCode("PUL");
        } else if (inst == 2) {
            institutionEntity.setInstitutionName("CUL");
            institutionEntity.setInstitutionCode("CUL");
        } else if (inst == 3) {
            institutionEntity.setInstitutionName("NYPL");
            institutionEntity.setInstitutionCode("NYPL");
        }
        bibliographicEntity.setInstitutionEntity(institutionEntity);
        return bibliographicEntity;
    }

    private HoldingsEntity getHoldingsEntity(int inst) {
        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("holdingContent".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("etl");
        holdingsEntity.setOwningInstitutionHoldingsId("657");
        holdingsEntity.setOwningInstitutionId(inst);
        return holdingsEntity;
    }

    private ItemEntity getItemEntity(int inst) {
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setId(1);
        itemEntity.setOwningInstitutionId(inst);
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("etl");
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setLastUpdatedBy("etl");
        String barcode = "1234";
        itemEntity.setBarcode(barcode);
        itemEntity.setCallNumber("x.12321");
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCallNumberType("1");
        itemEntity.setCustomerCode("1");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setDeleted(false);
        itemEntity.setCatalogingStatus(ScsbCommonConstants.COMPLETE_STATUS);
        ItemStatusEntity itemStatusEntity = new ItemStatusEntity();
        itemStatusEntity.setId(1);
        itemStatusEntity.setStatusCode("Available");
        itemStatusEntity.setStatusDescription("Available");
        itemEntity.setItemStatusEntity(itemStatusEntity);
        return itemEntity;
    }

    @Test
    public void fetchUpdatedRecordsAndStartProcessSingleMatchSerial() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        SolrQuery solrQuery = new SolrQuery("testquery");
        solrQuery.setStart(1);
        solrQuery.setRows(1);
        Date processDate = new Date();
        Date fromDate = getFromDate(processDate);
        Set<String> unMatchingTitleHeaderSet = new HashSet<>();
        unMatchingTitleHeaderSet.add("test");
        List<String> inst = new ArrayList<>();
        inst.add("PUL");
        List<Integer> serialMvmBibIds = new ArrayList<>();
        serialMvmBibIds.add(1);
        List<MatchingSummaryReport> matchingSummaryReports = new ArrayList<>();
        MatchingSummaryReport matchingSummaryReport = new MatchingSummaryReport();
        matchingSummaryReport.setInstitution("PUL");
        matchingSummaryReport.setOpenItemsDiff("2");
        matchingSummaryReports.add(0, matchingSummaryReport);
        ongoingMatchingReportsService.generateSummaryReport(matchingSummaryReports);
        SolrDocumentList solrDocumentList = getSolrDocumentsSingle(ScsbCommonConstants.OCLC_NUMBER);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "ongoingMatchingReportsService", ongoingMatchingReportsService);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrQueryBuilder", solrQueryBuilder);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "commonUtil", commonUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "collectionGroupDetailsRepository", collectionGroupDetailsRepository);
        Mockito.doNothing().when(ongoingMatchingReportsService).generateSerialAndMVMsReport(serialMvmBibIds);
        Mockito.when(ongoingMatchingReportsService.generateTitleExceptionReport(any(), any())).thenReturn("test");
        Mockito.doNothing().when(ongoingMatchingReportsService).generateSummaryReport(matchingSummaryReports);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(SolrDocument.class), Mockito.anyCollection(), any(BibItem.class))).thenReturn(getBibItemSingle("PUL", 1, ScsbCommonConstants.SERIAL)).thenReturn(getBibItemSingle("CUL", 2, ScsbCommonConstants.SERIAL)).thenReturn(getBibItemSingle("NYPL", 3, ScsbCommonConstants.SERIAL));
        Mockito.doCallRealMethod().when(mockedmatchingAlgorithmUtil).populateMatchingCounter();
        Mockito.when(mockedmatchingAlgorithmUtil.indexBibs(any())).thenReturn("test");
        Mockito.when(solrQueryBuilder.fetchBibsForGroupingProcess(Mockito.anyString(), Mockito.anyBoolean())).thenReturn("testquery");
        Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(ScsbCommonConstants.OCLC_NUMBER, "test")).thenReturn("testquery");
        Mockito.when(mockedmatchingAlgorithmUtil.getMatchingAndUnMatchingBibsOnTitleVerification(Mockito.anyMap())).thenReturn(unMatchingTitleHeaderSet);
        Mockito.when(commonUtil.findAllInstitutionCodesExceptSupportInstitution()).thenReturn(inst);
        Mockito.doNothing().when(mockedmatchingAlgorithmUtil).populateMatchingCounter();
        Mockito.when(ongoingMatchingReportsService.populateSummaryReportBeforeMatching()).thenReturn(matchingSummaryReports);
        //    String status = ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartProcess(fromDate, 1, Boolean.TRUE, true);
        assertNotNull(ScsbCommonConstants.SUCCESS);
    }

    @Test
    public void fetchUpdatedRecordsAndStartProcessSingleMatchSerialException() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        SolrQuery solrQuery = new SolrQuery("testquery");
        solrQuery.setStart(1);
        solrQuery.setRows(1);
        Date processDate = new Date();
        Date fromDate = getFromDate(processDate);
        Set<String> unMatchingTitleHeaderSet = new HashSet<>();
        unMatchingTitleHeaderSet.add("test");
        List<String> inst = new ArrayList<>();
        inst.add("PUL");
        List<MatchingSummaryReport> matchingSummaryReports = new ArrayList<>();
        MatchingSummaryReport matchingSummaryReport = new MatchingSummaryReport();
        matchingSummaryReport.setInstitution("PUL");
        matchingSummaryReport.setOpenItemsDiff("2");
        matchingSummaryReports.add(0, matchingSummaryReport);
        SolrDocumentList solrDocumentList = getSolrDocumentsSingleList(ScsbCommonConstants.OCLC_NUMBER);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "ongoingMatchingReportsService", ongoingMatchingReportsService);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrQueryBuilder", solrQueryBuilder);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "commonUtil", commonUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "collectionGroupDetailsRepository", collectionGroupDetailsRepository);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(SolrDocument.class), Mockito.anyCollection(), any(BibItem.class))).thenReturn(getBibItemSingle("PUL", 1, ScsbCommonConstants.SERIAL)).thenReturn(getBibItemSingle("CUL", 2, ScsbCommonConstants.SERIAL)).thenReturn(getBibItemSingle("NYPL", 3, ScsbCommonConstants.SERIAL));
        Mockito.doCallRealMethod().when(mockedmatchingAlgorithmUtil).populateMatchingCounter();
        Mockito.when(solrQueryBuilder.fetchBibsForGroupingProcess(Mockito.anyString(), Mockito.anyBoolean())).thenReturn("testquery");
        Mockito.when(commonUtil.findAllInstitutionCodesExceptSupportInstitution()).thenReturn(inst);
        Mockito.doNothing().when(mockedmatchingAlgorithmUtil).populateMatchingCounter();
        Mockito.when(ongoingMatchingReportsService.populateSummaryReportBeforeMatching()).thenReturn(matchingSummaryReports);
        Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(ScsbCommonConstants.OCLC_NUMBER, "test")).thenReturn("testquery");
        //Mockito.when(mockedmatchingAlgorithmUtil.getMatchingAndUnMatchingBibsOnTitleVerification(Mockito.anyMap())).thenReturn(unMatchingTitleHeaderSet);
        Mockito.when(collectionGroupDetailsRepository.findAll()).thenThrow(NullPointerException.class);
        // String status = ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartProcess(fromDate, 1, Boolean.TRUE, true);
        assertNotNull(ScsbCommonConstants.SUCCESS);
    }

    @Test
    public void fetchUpdatedRecordsAndStartProcessMultiMatch() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        SolrQuery solrQuery = new SolrQuery("testquery");
        solrQuery.setStart(1);
        solrQuery.setRows(1);
        Date processDate = new Date();
        Date fromDate = getFromDate(processDate);
        SolrDocumentList solrDocumentList = getSolrDocumentsMulti();
        List<String> inst = new ArrayList<>();
        inst.add("PUL");
        List<MatchingSummaryReport> matchingSummaryReports = new ArrayList<>();
        MatchingSummaryReport matchingSummaryReport = new MatchingSummaryReport();
        matchingSummaryReport.setInstitution("PUL");
        matchingSummaryReport.setOpenItemsDiff("2");
        matchingSummaryReports.add(0, matchingSummaryReport);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "matchingAlgorithmUtil", mockedmatchingAlgorithmUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "ongoingMatchingReportsService", ongoingMatchingReportsService);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrQueryBuilder", solrQueryBuilder);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "commonUtil", commonUtil);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "collectionGroupDetailsRepository", collectionGroupDetailsRepository);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(SolrDocument.class), Mockito.anyCollection(), any(BibItem.class))).thenReturn(getBibItemSingle("PUL", 1, ScsbCommonConstants.SERIAL)).thenReturn(getBibItemSingle("CUL", 2, ScsbCommonConstants.SERIAL)).thenReturn(getBibItemSingle("NYPL", 3, ScsbCommonConstants.SERIAL));
        Mockito.doCallRealMethod().when(mockedmatchingAlgorithmUtil).populateMatchingCounter();
        Mockito.when(solrQueryBuilder.fetchBibsForGroupingProcess(Mockito.anyString(), Mockito.anyBoolean())).thenReturn("testquery");
        Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(ScsbCommonConstants.OCLC_NUMBER, "test")).thenReturn("testquery");
        Mockito.when(commonUtil.findAllInstitutionCodesExceptSupportInstitution()).thenReturn(inst);
        Mockito.doNothing().when(mockedmatchingAlgorithmUtil).populateMatchingCounter();
        Mockito.when(ongoingMatchingReportsService.populateSummaryReportBeforeMatching()).thenReturn(matchingSummaryReports);
        //  String status = ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartProcess(fromDate, 1, Boolean.TRUE, true);
        assertNotNull(ScsbCommonConstants.SUCCESS);

    }

    private List<InstitutionEntity> getInstitutionEntities() {
        List<InstitutionEntity> institutionEntities = new ArrayList<>();
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionCode("PUL");
        institutionEntity.setInstitutionName("Princeton");
        institutionEntity.setId(1);
        institutionEntities.add(institutionEntity);
        return institutionEntities;
    }

    private List<CollectionGroupEntity> getCollectionGroupEntities() {
        List<CollectionGroupEntity> collectionGroupEntities = new ArrayList<>();
        CollectionGroupEntity collectionGroupEntity = new CollectionGroupEntity();
        collectionGroupEntity.setId(1);
        collectionGroupEntity.setCollectionGroupCode("Shared");
        collectionGroupEntity.setCollectionGroupDescription("Shared");
        collectionGroupEntities.add(collectionGroupEntity);
        return collectionGroupEntities;
    }

    public ReportEntity buildReportEntity(String fileName) {
        ReportEntity unMatchReportEntity = new ReportEntity();
        unMatchReportEntity.setType("TitleException");
        unMatchReportEntity.setCreatedDate(new Date());
        unMatchReportEntity.setInstitutionName(ScsbCommonConstants.ALL_INST);
        unMatchReportEntity.setFileName(fileName);
        return unMatchReportEntity;
    }

    private BibItem getBibItemSingle(String inst, Integer id, String type) {
        BibItem bibItem = new BibItem();
        bibItem.setIsbn(Collections.singletonList("111"));
        bibItem.setIssn(Collections.singletonList("222"));
        bibItem.setLccn("333");
        bibItem.setOclcNumber(Collections.singletonList("1"));
        bibItem.setBibId(id);
        bibItem.setOwningInstitution(inst);
        bibItem.setOwningInstitutionBibId("987");
        bibItem.setTitleSubFieldA("a");
        bibItem.setMaterialType(type);
        bibItem.setLeaderMaterialType(type);
        return bibItem;
    }

    private SolrDocumentList getSolrDocumentsSingle(String matchPoint) {
        SolrDocumentList solrDocumentList = new SolrDocumentList();
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(matchPoint, String.valueOf(1));
        solrDocument.setField(ScsbConstants.BIB_ID, 1);
        solrDocument.setField(ScsbConstants.MATERIAL_TYPE, ScsbCommonConstants.MONOGRAPH);
        SolrDocument solrDocument1 = new SolrDocument();
        solrDocument1.setField(matchPoint, String.valueOf(2));
        solrDocument1.setField(ScsbConstants.BIB_ID, 2);
        solrDocument1.setField(ScsbConstants.MATERIAL_TYPE, ScsbCommonConstants.MONOGRAPH);
        SolrDocument solrDocument2 = new SolrDocument();
        solrDocument2.setField(matchPoint, String.valueOf(3));
        solrDocument2.setField(ScsbConstants.BIB_ID, 3);
        solrDocument2.setField(ScsbConstants.MATERIAL_TYPE, ScsbCommonConstants.MONOGRAPH);
        solrDocumentList.add(0, solrDocument);
        solrDocumentList.add(1, solrDocument1);
        solrDocumentList.add(2, solrDocument2);
        solrDocumentList.setNumFound(3);
        return solrDocumentList;
    }

    private SolrDocumentList getSolrDocumentsSingleList(String matchPoint) {
        SolrDocumentList solrDocumentList = new SolrDocumentList();
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(matchPoint, Arrays.asList(String.valueOf(1)));
        solrDocument.setField(ScsbConstants.BIB_ID, 1);
        solrDocument.setField(ScsbConstants.MATERIAL_TYPE, Arrays.asList(ScsbCommonConstants.MONOGRAPH));
        SolrDocument solrDocument1 = new SolrDocument();
        solrDocument1.setField(matchPoint, Arrays.asList(String.valueOf(2)));
        solrDocument1.setField(ScsbConstants.BIB_ID, 2);
        solrDocument1.setField(ScsbConstants.MATERIAL_TYPE, Arrays.asList(ScsbCommonConstants.MONOGRAPH));
        SolrDocument solrDocument2 = new SolrDocument();
        solrDocument2.setField(matchPoint, Arrays.asList(String.valueOf(3)));
        solrDocument2.setField(ScsbConstants.BIB_ID, 3);
        solrDocument2.setField(ScsbConstants.MATERIAL_TYPE, Arrays.asList(ScsbCommonConstants.MONOGRAPH));
        solrDocumentList.add(0, solrDocument);
        solrDocumentList.add(1, solrDocument1);
        solrDocumentList.add(2, solrDocument2);
        solrDocumentList.setNumFound(3);
        return solrDocumentList;
    }

    private SolrDocumentList getSolrDocumentsMulti() {
        SolrDocumentList solrDocumentList = new SolrDocumentList();
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.OCLC_NUMBER, String.valueOf(1));
        solrDocument.setField(ScsbCommonConstants.ISBN_CRITERIA, String.valueOf(11));
        solrDocument.setField(ScsbCommonConstants.ISSN_CRITERIA, String.valueOf(111));
        solrDocument.setField(ScsbCommonConstants.LCCN_CRITERIA, String.valueOf(1111));
        solrDocument.setField(ScsbConstants.BIB_ID, 11111);
        solrDocumentList.add(0, solrDocument);
        solrDocumentList.setNumFound(5);
        return solrDocumentList;
    }

    public Date getFromDate(Date createdDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(createdDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    @Test
    public void groupBibsAndUpdateInDB() throws Exception {
        List<Integer> bibIdList = new ArrayList<>();
        bibIdList.add(1);
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setId(1);
        institutionEntity.setInstitutionCode("PUL");
        institutionEntity.setInstitutionName("PRINCETON");
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        Map<Integer, BibliographicEntityForMatching> bibliographicEntityOptional = new HashMap<>();
        BibliographicEntityForMatching bibliographicEntityForMatching = new BibliographicEntityForMatching();
        bibliographicEntityForMatching.setAnamolyFlag(true);
        bibliographicEntityForMatching.setMatchingIdentity("123");
        bibliographicEntityForMatching.setMatchScore(10);
        bibliographicEntityOptional.put(1, bibliographicEntityForMatching);
        BibItem bibItem = new BibItem();
        bibItem.setBibId(1);
        bibItemMap.put(1, bibItem);
        bibIdList.add(1);
        bibItemMap.put(1, bibItem);
        List<BibliographicEntityForMatching> bibliographicEntityList = new ArrayList<>();
        bibliographicEntityList.add(bibliographicEntityForMatching);
        Mockito.when(bibliographicDetailsRepositoryForMatching.findAllById(bibIdList)).thenReturn(bibliographicEntityList);
        Mockito.when(mockedmatchingAlgorithmUtil.updateBibsForMatchingIdentifier(anyList(), anyMap())).thenReturn(Optional.of(bibliographicEntityOptional));
        try {
            ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil, "groupBibsAndUpdateInDB", bibIdList, bibItemMap);
        } catch (Exception e) {
        }
    }

    @Test
    public void saveReportAndUpdateCGDForMultiMatch() throws  Exception
    {
        Map<String, HashMap<Integer, BibItem>>  multiMatchedBibItemMap = new HashMap<>();
        HashMap<Integer, BibItem> hashMap = new HashMap<>();
        List<String> test = new ArrayList<>();
        List<String>  test1 = new ArrayList<>();
        List<String> test2 = new ArrayList<>();
        Set<String> owningInstSet = new HashSet<>();
        owningInstSet.add("123");
        test2.add("1234");
        test1.add("123456");
        test.add("12345");
        BibItem bibItem = new BibItem();
        bibItem.setBibId(1);
        bibItem.setBarcode("1234567");
        bibItem.setMatchScore(1);
        bibItem.setId("1");
        bibItem.setIsbn(test);
        bibItem.setLccn("12345");
        bibItem.setOclcNumber(test1);
        bibItem.setIssn(test2);
        bibItem.setOwningInstitution("PUL");
        BibItem bibItem1 = new BibItem();
        bibItem1.setOwningInstitution("CUL");
        hashMap.put(1,bibItem);
        hashMap.put(2,bibItem1);
        multiMatchedBibItemMap.put("test",hashMap);
        List<Integer> serialMvmBibIds = new ArrayList<>();
        serialMvmBibIds.add(1234);
        Boolean isCGDProcess = true;
        List<Integer> matchedBibIds = new ArrayList<>();
        matchedBibIds.add(1);
        ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil,"saveReportAndUpdateCGDForMultiMatch",multiMatchedBibItemMap,serialMvmBibIds,matchedBibIds,isCGDProcess);
    }
    @Test
    public void saveReportAndUpdateCGDForSingleMatch() throws  Exception
    {
        Map<String, HashMap<Integer, BibItem>>  multiMatchedBibItemMap = new HashMap<>();
        HashMap<Integer, BibItem> hashMap = new HashMap<>();
        List<String> test = new ArrayList<>();
        List<String>  test1 = new ArrayList<>();
        List<String> test2 = new ArrayList<>();
        Set<String> owningInstSet = new HashSet<>();
        owningInstSet.add("123");
        test2.add("1234");
        test1.add("123456");
        test.add("12345");
        BibItem bibItem = new BibItem();
        bibItem.setBibId(1);
        bibItem.setBarcode("1234567");
        bibItem.setMatchScore(1);
        bibItem.setId("1");
        bibItem.setIsbn(test);
        bibItem.setLccn("12345");
        bibItem.setOclcNumber(test1);
        bibItem.setIssn(test2);
        bibItem.setOwningInstitution("PUL");
        BibItem bibItem1 = new BibItem();
        bibItem1.setOwningInstitution("CUL");
        hashMap.put(1,bibItem);
        hashMap.put(2,bibItem1);
        multiMatchedBibItemMap.put("test",hashMap);
        List<Integer> serialMvmBibIds = new ArrayList<>();
        serialMvmBibIds.add(1234);
        Boolean isCGDProcess = true;
        ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil,"saveReportAndUpdateCGDForSingleMatch",multiMatchedBibItemMap,serialMvmBibIds,isCGDProcess);
    }

    @Test
    public void findMatchingBibs() throws Exception {
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.OCLC_NUMBER, String.valueOf(1));
        solrDocument.setField(ScsbCommonConstants.ISBN_CRITERIA, String.valueOf(11));
        solrDocument.setField(ScsbCommonConstants.ISSN_CRITERIA, String.valueOf(111));
        solrDocument.setField(ScsbCommonConstants.LCCN_CRITERIA, String.valueOf(1111));
        solrDocument.setField(ScsbConstants.BIB_ID, 11111);
        String fieldName = "test";
        Integer matchScore = 1;
        Map<Integer, BibItem> existingBibItemMap = new HashMap<>();
        BibItem bibItem = new BibItem();
        bibItem.setBibId(1);
        bibItem.setMatchScore(1);
        existingBibItemMap.put(1, bibItem);
        List<String> fieldValue = new ArrayList<>();
        fieldValue.add("test");
        List<String> oclc = new ArrayList<>();
        oclc.add("12345");
        List<String> Issn = new ArrayList<>();
        Issn.add("34567");
        List<String> Isbn = new ArrayList<>();
        Isbn.add("6768");
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        bibItem.setTitle("test");
        bibItem.setBibId(1);
        bibItem.setMatchScore(1);
        bibItem.setOclcNumber(oclc);
        bibItem.setIssn(Issn);
        bibItem.setLccn("123765");
        bibItem.setIsbn(Isbn);
        bibItemMap.put(1, bibItem);
        Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(any(), anyList())).thenReturn("test");
        ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil, "findMatchingBibs", solrDocument, fieldName, matchScore, existingBibItemMap);


    }

    @Test
    public void checkAndAddReportEntities() throws Exception {
        List<MatchingAlgorithmReportDataEntity> reportDataEntities = new ArrayList<>();
        MatchingAlgorithmReportDataEntity oclcNumberReportDataEntity = new MatchingAlgorithmReportDataEntity();
        oclcNumberReportDataEntity.setId(1);
        Set<String> oclcNumbers = new HashSet<>();
        oclcNumbers.add("123456");
        String oclcCriteria = "test";
        reportDataEntities.add(oclcNumberReportDataEntity);
        Mockito.when(mockedmatchingAlgorithmUtil.getReportDataEntityForCollectionValues(any(), any())).thenReturn(oclcNumberReportDataEntity);
        ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil, "checkAndAddReportEntities", reportDataEntities, oclcNumbers, oclcCriteria);
    }

    @Test
    public  void updateCGDBasedOnMaterialTypes() throws  Exception
    {
        Map<Integer, ItemEntity> itemEntityMap = new HashMap<>();
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setBarcode("123456");
        itemEntityMap.put(1,itemEntity);
        Map parameterMap = new HashMap();
        List<String> materialTypeList = new ArrayList<>();
        materialTypeList.add("test");
        List<Integer> bibIdList = new ArrayList<>();
        bibIdList.add(1);
        parameterMap.put(1,"test");
        parameterMap.put(ScsbConstants.MATCH_POINT,ScsbCommonConstants.OCLC_NUMBER);
        MatchingAlgorithmReportEntity reportEntity = new MatchingAlgorithmReportEntity();
        reportEntity.setId(1);
        reportEntity.setInstitutionName("PUL");
        reportEntity.setType("xml");
        Set<String> materialTypes= new HashSet<>();
        materialTypes.add("Monograph");
        Set<String> materialTypeSet = new HashSet<>();
        materialTypeSet.add("test");
        List<Integer> serialMvmBibIds = new ArrayList<>();
        serialMvmBibIds.add(1);
        String matchType  = "singlematch";
        List<MatchingAlgorithmReportEntity> reportEntityList = new ArrayList<>();
        MatchingAlgorithmReportEntity matchingAlgorithmReportEntity = new MatchingAlgorithmReportEntity();
        matchingAlgorithmReportEntity.setInstitutionName("CUL");
        matchingAlgorithmReportEntity.setId(1);
        reportEntityList.add(0,matchingAlgorithmReportEntity);
        Map<String,String> titleMap = new HashMap<>();
        titleMap.put("test","test");
        Integer matchScore = 1;
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        BibItem bibItem = new BibItem();
        bibItem.setBibId(1);
        bibItem.setId("2");
        bibItem.setOwningInstitution("PUL");
        bibItemMap.put(1,bibItem);
        Boolean isCGDProcess = true;
        Mockito.when(mockedmatchingAlgorithmUtil.buildReportEntity(any())).thenReturn(matchingAlgorithmReportEntity);
        Mockito.when(mockedmatchingAlgorithmUtil.getMatchingAndUnMatchingBibsOnTitleVerification(any())).thenReturn(materialTypeSet);
        Mockito.when(matchingAlgorithmCGDProcessor.checkForMonographAndPopulateValues(any(), anyMap(), anyList(),any())).thenReturn(false);
        ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil,"updateCGDBasedOnMaterialTypes",reportEntity,materialTypes,serialMvmBibIds,matchType,parameterMap,reportEntityList,titleMap,matchScore,bibItemMap);

    }


    @Test
    public void addToBibItemMap() throws Exception {
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.OCLC_NUMBER, MatchScoreUtil.OCLC_SCORE);
        solrDocument.setField(ScsbCommonConstants.ISBN_CRITERIA, MatchScoreUtil.ISBN_SCORE);
        solrDocument.setField(ScsbCommonConstants.ISSN_CRITERIA, MatchScoreUtil.ISSN_SCORE);
        solrDocument.setField(ScsbCommonConstants.LCCN_CRITERIA, MatchScoreUtil.LCCN_SCORE);
        solrDocument.setField(ScsbConstants.TITLE_MATCH_SOLR, MatchScoreUtil.TITLE_SCORE);
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        List<String> oclc = new ArrayList<>();
        oclc.add("12345");
        List<String> Issn = new ArrayList<>();
        Issn.add("34567");
        List<String> Isbn = new ArrayList<>();
        Isbn.add("6768");
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        BibItem bibItem = new BibItem();
        bibItem.setTitle("test");
        bibItem.setBibId(1);
        bibItem.setMatchScore(1);
        bibItem.setOclcNumber(oclc);
        bibItem.setIssn(Issn);
        bibItem.setLccn("123765");
        bibItem.setIsbn(Isbn);
        bibItemMap.put(1, bibItem);
        String matchPointField = "test";
        Integer matchScore = 2;
        ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil, "addToBibItemMap", solrDocument, bibItemMap, matchPointField, matchScore);
    }

    @Test
    public void titleVerificationForSingleMatch() throws Exception {
        List<Integer> bibIds = new ArrayList<>();
        bibIds.add(1);
        List<String> materialTypeList = new ArrayList<>();
        materialTypeList.add("test");
        String fileName = "test";
        Map<String, String> titleMap = new HashMap<>();
        titleMap.put("test", "test");
        Map parameterMap = new HashMap();
        parameterMap.put("test", "test");
        ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil, "titleVerificationForSingleMatch", fileName, titleMap, bibIds, materialTypeList, parameterMap);

    }

    @Test
    public void getMatchingBibsAndMatchPoints() throws Exception {
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.OCLC_NUMBER, "111");
        solrDocument.setField(ScsbCommonConstants.OCLC_NUMBER, "1111");
        solrDocument.setField(ScsbCommonConstants.ISBN_CRITERIA, "1111");
        solrDocument.setField(ScsbCommonConstants.ISSN_CRITERIA, "111111");
        solrDocument.setField(ScsbCommonConstants.LCCN_CRITERIA, "111");
        solrDocument.setField(ScsbConstants.TITLE_MATCH_SOLR, "11111");
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(getSolrDocumentsSingleList("8"));
        //Mockito.when(ongoingMatchingAlgorithmUtil.findMatchingBibs(any(),any(),any(),any())).thenReturn();
        List<String> oclc = new ArrayList<>();
        oclc.add("12345");
        oclc.add("45676");
        List<String> Issn = new ArrayList<>();
        Issn.add("34567");
        Issn.add("67799");
        List<String> Isbn = new ArrayList<>();
        Isbn.add("6768");
        Isbn.add("57689");
        Map<String, HashMap<Integer, BibItem>> bibItemMap = new HashMap<>();
        HashMap<Integer, BibItem> bibItemHashMap = new HashMap<>();
        BibItem bibItem = new BibItem();
        bibItem.setTitle("test");
        bibItem.setId("1");
        bibItem.setBibId(1);
        bibItem.setMatchScore(1);
        bibItem.setOclcNumber(oclc);
        bibItem.setIssn(Issn);
        bibItem.setLccn("123765");
        bibItem.setIsbn(Isbn);
        bibItemHashMap.put(1, bibItem);
        bibItemMap.put("test1", bibItemHashMap);

        HashMap<Integer, BibItem> bibItemHashMap2 = new HashMap<>();
        BibItem bibItem2 = new BibItem();
        bibItem2.setTitle("test2");
        bibItem2.setId("2");
        bibItem2.setBibId(2);
        bibItem2.setMatchScore(1);
        bibItem2.setOclcNumber(oclc);
        bibItem2.setIssn(Issn);
        bibItem2.setLccn("123765");
        bibItem2.setIsbn(Isbn);
        bibItemHashMap2.put(2, bibItem2);
        bibItemMap.put("test2", bibItemHashMap2);

        HashMap<Integer, BibItem> bibItemHashMap3 = new HashMap<>();
        BibItem bibItem3 = new BibItem();
        bibItem3.setTitle("test3");
        bibItem3.setId("3");
        bibItem3.setBibId(3);
        bibItem3.setMatchScore(1);
        bibItem3.setOclcNumber(oclc);
        bibItem3.setIssn(Issn);
        bibItem3.setLccn("123765");
        bibItem3.setIsbn(Isbn);
        bibItemHashMap3.put(3, bibItem3);
        bibItemMap.put("test3", bibItemHashMap3);

        Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(), any(), any())).thenReturn(bibItem);
        Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(any(), anyString())).thenReturn("test");
        ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil, "getMatchingBibsAndMatchPoints", solrDocument, bibItemMap);
    }

    @Test
    public void fetchUpdatedRecordsAndStartCgdUpdateProcessBasedOnCriteria() throws Exception {
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setBibIds("Test");
        solrIndexRequest.setMatchBy("BibIdList");
        Integer rows = 100;
        List<MatchingSummaryReport> matchingSummaryReports = new ArrayList<>();
        MatchingSummaryReport matchingSummaryReport = new MatchingSummaryReport();
        matchingSummaryReport.setInstitution("PUL");
        matchingSummaryReport.setSharedItemsDiff("test");
        matchingSummaryReport.setTotalBibs("7900");
        matchingSummaryReport.setTotalItems("10000");
        SolrDocumentList solrDocumentList = getSolrDocumentsSingle("1");
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);

        Mockito.when(ongoingMatchingAlgorithmQueryUtil.fetchDataByQuery(any(), any(), any())).thenReturn(queryResponse);
        Mockito.when(ongoingMatchingReportsService.populateSummaryReportBeforeMatching()).thenReturn(matchingSummaryReports);

        ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartCgdUpdateProcessBasedOnCriteria(solrIndexRequest, rows);

    }

    @Test
    public void fetchUpdatedRecordsAndStartGroupingProcessBasedOnCriteria() throws Exception {
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setBibIds("Test");
        solrIndexRequest.setMatchBy("BibIdList");
        Integer rows = 100;
        List<MatchingSummaryReport> matchingSummaryReports = new ArrayList<>();
        MatchingSummaryReport matchingSummaryReport = new MatchingSummaryReport();
        matchingSummaryReport.setInstitution("PUL");
        matchingSummaryReport.setSharedItemsDiff("test");
        matchingSummaryReport.setTotalBibs("7900");
        matchingSummaryReport.setTotalItems("10000");
        SolrDocumentList solrDocumentList = getSolrDocumentsSingle("1");
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        Mockito.when(ongoingMatchingAlgorithmQueryUtil.fetchDataByQuery(any(), any(), any())).thenReturn(queryResponse);
        Mockito.when(ongoingMatchingReportsService.populateSummaryReportBeforeMatching()).thenReturn(matchingSummaryReports);
        ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartGroupingProcessBasedOnCriteria(solrIndexRequest, rows);
    }

    @Test
    public void processOngoingMatching() throws Exception {
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.OCLC_NUMBER, "111");
        solrDocument.setField(ScsbCommonConstants.OCLC_NUMBER, "1111");
        solrDocument.setField(ScsbCommonConstants.ISBN_CRITERIA, "1111");
        solrDocument.setField(ScsbCommonConstants.ISSN_CRITERIA, "111111");
        solrDocument.setField(ScsbCommonConstants.LCCN_CRITERIA, "111");
        solrDocument.setField(ScsbConstants.TITLE_MATCH_SOLR, "11111");
        solrDocument.setField(ScsbConstants.TITLE_MATCH_SOLR, "11111");
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setBibIds("Test");
        solrIndexRequest.setMatchBy("BibIdList");
        solrIndexRequest.setFromBibId("1");
        solrIndexRequest.setToBibId("2");
        solrIndexRequest.setCommitInterval(2);
        solrIndexRequest.setDocType("Xml");
        solrIndexRequest.setIncludeMaQualifier(true);
        solrIndexRequest.setMaProcessType("test");
        solrIndexRequest.setMatchingCriteria("LCCN");
        solrIndexRequest.setOwningInstitutionCode("PUL");
        solrIndexRequest.setNumberOfThreads(5);
        solrIndexRequest.setReportType("Xml");
        Integer rows = 9;
        List<Integer> matchedBibIds = new ArrayList<>();
        matchedBibIds.add(1);
        matchedBibIds.add(2);
        List<Integer> bibIdListToIndex = new ArrayList<>();
        bibIdListToIndex.add(10);
        bibIdListToIndex.add(11);
        boolean isCGDProcess = true;
        SolrDocumentList solrDocumentList = getSolrDocumentsSingle("1");
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        queryResponse.setResponse(new NamedList<>(solrDocument));
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        Mockito.when(ongoingMatchingAlgorithmQueryUtil.fetchDataByQuery(any(), any(), any())).thenReturn(queryResponse);
        //  Mockito.when( ongoingMatchingAlgorithmQueryUtil.prepareQueryForOngoingMatchingGroupingProcessBasedOnCriteria(solrIndexRequest)).thenReturn("test");
        ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil, "processOngoingMatching", solrIndexRequest, rows, matchedBibIds, bibIdListToIndex, isCGDProcess);
    }


    @Test
    public void indexForOngoingMatching() throws Exception {
        boolean isIndexBibsForOngoingMa = true;
        List<Integer> bibIdListToIndex = new ArrayList<>();
        bibIdListToIndex.add(1);
        Mockito.when(mockedmatchingAlgorithmUtil.indexBibs(any())).thenReturn("test");
        ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil, "indexForOngoingMatching", isIndexBibsForOngoingMa, bibIdListToIndex);
    }


    @Test
    public void processMatchingForBib() throws Exception {
        BibItem bibItem = new BibItem();
        bibItem.setBibId(1);
        bibItem.setMaterialType("Monograph");
        bibItem.setBarcode("1234568910");
        bibItem.setId("1");
        bibItem.setMatchScore(10);
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.OCLC_NUMBER, "111");
        solrDocument.setField(ScsbCommonConstants.OCLC_NUMBER, "1111");
        solrDocument.setField(ScsbCommonConstants.ISBN_CRITERIA, "1111");
        solrDocument.setField(ScsbCommonConstants.ISSN_CRITERIA, "111111");
        solrDocument.setField(ScsbCommonConstants.LCCN_CRITERIA, "111");
        solrDocument.setField(ScsbConstants.TITLE_MATCH_SOLR, "11111");
        solrDocument.setField(ScsbConstants.TITLE_MATCH_SOLR, "11111");
        List<Integer> serialMvmBibIds = new ArrayList<>();
        serialMvmBibIds.add(1);
        serialMvmBibIds.add(2);
        List<Integer> matchedBibIds = new ArrayList<>();
        matchedBibIds.add(1);
        matchedBibIds.add(2);
        Boolean isCGDProcess = false;
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(getSolrDocumentsSingleList("8"));
        Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(ScsbCommonConstants.OCLC_NUMBER, "test")).thenReturn("testquery");
        Mockito.when(solrQueryBuilder.fetchCreatedOrUpdatedBibsByDateRange(any(), any())).thenCallRealMethod();
        Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(SolrDocument.class), Mockito.anyCollection(), any(BibItem.class))).thenReturn(bibItem);
        ongoingMatchingAlgorithmUtil.processMatchingForBib(solrDocument, serialMvmBibIds, matchedBibIds, isCGDProcess);
    }

    @Test
    public void checkIfReportForSingleMatchExists() throws Exception {
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.OCLC_NUMBER, "111");
        solrDocument.setField(ScsbCommonConstants.OCLC_NUMBER, "1111");
        solrDocument.setField(ScsbCommonConstants.ISBN_CRITERIA, "1111");
        solrDocument.setField(ScsbCommonConstants.ISSN_CRITERIA, "111111");
        solrDocument.setField(ScsbCommonConstants.LCCN_CRITERIA, "111");
        solrDocument.setField(ScsbConstants.TITLE_MATCH_SOLR, "11111");
        solrDocument.setField(ScsbConstants.TITLE_MATCH_SOLR, "11111");
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(getSolrDocumentsSingleList("8"));
        //Mockito.when(ongoingMatchingAlgorithmUtil.findMatchingBibs(any(),any(),any(),any())).thenReturn();
        List<String> oclc = new ArrayList<>();
        oclc.add("12345");
        oclc.add("45676");
        List<String> Issn = new ArrayList<>();
        Issn.add("34567");
        Issn.add("67799");
        List<String> Isbn = new ArrayList<>();
        Isbn.add("6768");
        Isbn.add("57689");
        Map<String, HashMap<Integer, BibItem>> singleMatchBibItemMap = new HashMap<>();
        HashMap<Integer, BibItem> bibItemHashMap = new HashMap<>();
        BibItem bibItem = new BibItem();
        bibItem.setTitle("test");
        bibItem.setId("1");
        bibItem.setBibId(1);
        bibItem.setMatchScore(1);
        bibItem.setOclcNumber(oclc);
        bibItem.setIssn(Issn);
        bibItem.setLccn("123765");
        bibItem.setIsbn(Isbn);
        bibItemHashMap.put(1, bibItem);
        singleMatchBibItemMap.put("test1", bibItemHashMap);

        HashMap<Integer, BibItem> bibItemHashMap2 = new HashMap<>();
        BibItem bibItem2 = new BibItem();
        bibItem2.setTitle("test2");
        bibItem2.setId("2");
        bibItem2.setBibId(2);
        bibItem2.setMatchScore(1);
        bibItem2.setOclcNumber(oclc);
        bibItem2.setIssn(Issn);
        bibItem2.setLccn("123765");
        bibItem2.setIsbn(Isbn);
        bibItemHashMap2.put(2, bibItem2);
        singleMatchBibItemMap.put("test2", bibItemHashMap2);

        HashMap<Integer, BibItem> bibItemHashMap3 = new HashMap<>();
        BibItem bibItem3 = new BibItem();
        bibItem3.setTitle("test3");
        bibItem3.setId("3");
        bibItem3.setBibId(3);
        bibItem3.setMatchScore(1);
        bibItem3.setOclcNumber(oclc);
        bibItem3.setIssn(Issn);
        bibItem3.setLccn("123765");
        bibItem3.setIsbn(Isbn);
        bibItemHashMap3.put(3, bibItem3);
        singleMatchBibItemMap.put("test3", bibItemHashMap3);

        Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(), any(), any())).thenReturn(bibItem);
        Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(any(), anyString())).thenReturn("test");
        ongoingMatchingAlgorithmUtil.checkIfReportForSingleMatchExists(solrDocument, singleMatchBibItemMap);
    }

    @Test
    public void getFromDateFromRequest() throws Exception {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new Date());
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setBibIds("Test");
        solrIndexRequest.setMatchBy("BibIdList");
        solrIndexRequest.setFromBibId("1");
        solrIndexRequest.setToBibId("2");
        solrIndexRequest.setCommitInterval(2);
        solrIndexRequest.setDocType("Xml");
        solrIndexRequest.setIncludeMaQualifier(true);
        solrIndexRequest.setMaProcessType("test");
        solrIndexRequest.setMatchingCriteria("LCCN");
        solrIndexRequest.setOwningInstitutionCode("PUL");
        solrIndexRequest.setNumberOfThreads(5);
        solrIndexRequest.setReportType("Xml");
        //  solrIndexRequest.setFromDate(date);
        ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil, "getFromDateFromRequest", solrIndexRequest);

    }

    @Test
    public void getCollectionGroupMap() throws Exception {
        List<CollectionGroupEntity> collectionGroupEntity = new ArrayList<>();
        CollectionGroupEntity collectionGroupEntity1 = new CollectionGroupEntity();
        collectionGroupEntity1.setCollectionGroupDescription("test");
        collectionGroupEntity1.setId(1);
        collectionGroupEntity1.setCollectionGroupCode("test");
        Mockito.when(collectionGroupDetailsRepository.findAll()).thenReturn(collectionGroupEntity);
        ongoingMatchingAlgorithmUtil.getCollectionGroupMap();
    }

    @Test
    public void getFormattedDate() throws  Exception {
        String fromDate = "1999/12/15";
        Mockito.when(dateUtil.getFromDate(any())).thenCallRealMethod();
        ReflectionTestUtils.invokeMethod(ongoingMatchingAlgorithmUtil, "getFormattedDate", fromDate);
    }}




