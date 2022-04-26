package org.recap.matchingalgorithm.service;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spi.RouteController;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.recap.*;
import org.recap.matchingalgorithm.MatchingCounter;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.jpa.MatchingAlgorithmReportDataEntity;
import org.recap.model.jpa.MatchingAlgorithmReportEntity;
import org.recap.model.matchingreports.MatchingSummaryReport;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.repository.jpa.MatchingAlgorithmReportDetailRepository;
import org.recap.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.recap.ScsbConstants.*;
import static org.recap.ScsbConstants.MATCHING_COUNTER_UPDATED_OPEN;

/**
 * Created by Anitha on 10/10/20.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({SolrTemplate.class, SolrClient.class})
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
public class OngoingMatchingReportsServiceUT extends BaseTestCaseUT4{

    @InjectMocks
    OngoingMatchingReportsService ongoingMatchingReportsService;

    @Mock
    OngoingMatchingReportsService mockOngoingMatchingReportsService;

    @Mock
    SolrQueryBuilder solrQueryBuilder;

    @Mock
    CamelContext camelContext;

    @Mock
    RouteController routeController;

    @Mock
    ProducerTemplate producerTemplate;

    @Mock
    MatchingAlgorithmReportDetailRepository matchingAlgorithmReportDetailRepository;

    @Mock
    DateUtil dateUtil;

    @Mock
    CsvUtil csvUtil;

    @Mock
    InstitutionDetailsRepository institutionDetailsRepository;

    @Value("${" + PropertyKeyConstants.ONGOING_MATCHING_REPORT_DIRECTORY + "}")
    String matchingReportsDirectory;

    @Mock
    MatchingCounter matchingCounter;

    @Mock
    PropertyUtil propertyUtil;

    @Value("${" + PropertyKeyConstants.SCSB_SUPPORT_INSTITUTION + "}")
    private String supportInstitution;

    @Mock
    CommonUtil commonUtil;

    List<String> scsbInstitutions=Arrays.asList("HTC");

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        Map<String,Integer> cgdCounterMap=new HashMap<>();
        cgdCounterMap.put(MATCHING_COUNTER_SHARED,1);
        cgdCounterMap.put(MATCHING_COUNTER_OPEN,1);
        cgdCounterMap.put(MATCHING_COUNTER_UPDATED_SHARED,0);
        cgdCounterMap.put(MATCHING_COUNTER_UPDATED_OPEN,0);
        List<String> institutions= Arrays.asList("PUL","CUL","NYPL","HL","UC");
        Map<String, Map<String, Integer>> institutionCounterMap=new HashMap<>();
        for (String institution : institutions) {
            institutionCounterMap.put(institution,cgdCounterMap);
        }
        ReflectionTestUtils.setField(commonUtil,"institutionDetailsRepository",institutionDetailsRepository);
        ReflectionTestUtils.setField(matchingCounter,"scsbInstitutions",scsbInstitutions);
        ReflectionTestUtils.setField(matchingCounter,"institutionCounterMap",institutionCounterMap);
        ReflectionTestUtils.setField(ongoingMatchingReportsService,"matchingReportsDirectory",matchingReportsDirectory);
    }

    @Test
    public void generateTitleExceptionReport() throws Exception {
        List<MatchingAlgorithmReportEntity> reportEntities=new ArrayList<>();
        List<MatchingAlgorithmReportDataEntity> reportDataEntities=new ArrayList<>();
        MatchingAlgorithmReportDataEntity reportDataEntity=new MatchingAlgorithmReportDataEntity();
        reportDataEntity.setHeaderName("Title");
        reportDataEntity.setHeaderValue("test");
        reportDataEntities.add(reportDataEntity);
        MatchingAlgorithmReportEntity reportEntity=new MatchingAlgorithmReportEntity();
        reportEntity.addAll(reportDataEntities);
        reportEntities.add(reportEntity);
        File file=new File("Test");
        Page<MatchingAlgorithmReportEntity> page=Mockito.mock(Page.class);
        Mockito.when(page.getContent()).thenReturn(reportEntities);
        Mockito.when(page.getTotalPages()).thenReturn(2);
        Mockito.when(mockOngoingMatchingReportsService.getCamelContext()).thenReturn(camelContext);
        Mockito.when(camelContext.getRouteController()).thenReturn(routeController);
        Mockito.doNothing().when(routeController).startRoute(Mockito.anyString());
        Mockito.when(mockOngoingMatchingReportsService.getMatchingAlgorithmReportDetailRepository()).thenReturn(matchingAlgorithmReportDetailRepository);
        Mockito.when(mockOngoingMatchingReportsService.getDateUtil()).thenReturn(dateUtil);
        Mockito.when(mockOngoingMatchingReportsService.getCsvUtil()).thenReturn(csvUtil);
        Mockito.when(mockOngoingMatchingReportsService.getMatchingReportsDirectory()).thenReturn(matchingReportsDirectory);
        Mockito.when(matchingAlgorithmReportDetailRepository.findByFileAndTypeAndDateRangeWithPaging(Mockito.any(),Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any())).thenReturn(page);
        Mockito.when(csvUtil.createTitleExceptionReportFile(Mockito.anyString(),Mockito.anyInt(),Mockito.anyList())).thenReturn(file);
        String file1=ongoingMatchingReportsService.generateTitleExceptionReport(new Date(), 1);
        assertEquals(String.valueOf(file),file1);
    }

    @Test
    public void generateTitleExceptionReportException() throws Exception {
        List<MatchingAlgorithmReportEntity> reportEntities=new ArrayList<>();
        List<MatchingAlgorithmReportDataEntity> reportDataEntities=new ArrayList<>();
        MatchingAlgorithmReportDataEntity reportDataEntity=new MatchingAlgorithmReportDataEntity();
        reportDataEntity.setHeaderName("Title");
        reportDataEntity.setHeaderValue("test");
        reportDataEntities.add(reportDataEntity);
        MatchingAlgorithmReportEntity reportEntity=new MatchingAlgorithmReportEntity();
        reportEntity.addAll(reportDataEntities);
        reportEntities.add(reportEntity);
        File file=new File("Test");
        Page<MatchingAlgorithmReportEntity> page=Mockito.mock(Page.class);
        Mockito.when(page.getContent()).thenReturn(reportEntities);
        Mockito.when(page.getTotalPages()).thenReturn(2);
        Mockito.when(mockOngoingMatchingReportsService.getCamelContext()).thenReturn(camelContext);
        Mockito.when(mockOngoingMatchingReportsService.getMatchingAlgorithmReportDetailRepository()).thenReturn(matchingAlgorithmReportDetailRepository);
        Mockito.when(mockOngoingMatchingReportsService.getDateUtil()).thenReturn(dateUtil);
        Mockito.when(mockOngoingMatchingReportsService.getCsvUtil()).thenReturn(csvUtil);
        Mockito.when(mockOngoingMatchingReportsService.getMatchingReportsDirectory()).thenReturn(matchingReportsDirectory);
        Mockito.when(matchingAlgorithmReportDetailRepository.findByFileAndTypeAndDateRangeWithPaging(Mockito.any(),Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any())).thenReturn(page);
        Mockito.when(csvUtil.createTitleExceptionReportFile(Mockito.anyString(),Mockito.anyInt(),Mockito.anyList())).thenReturn(file);
        String file1=ongoingMatchingReportsService.generateTitleExceptionReport(new Date(), 1);
        assertEquals(String.valueOf(file),file1);
    }

        @Test
        public void generateSerialAndMVMsReportException() throws IOException, SolrServerException {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse= Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingReportsService,"solrTemplate",mocksolrTemplate1);
        SolrQuery solrQuery = new SolrQuery("testquery");
        solrQuery.setStart(1);
        solrQuery.setRows(1);
        SolrDocumentList solrDocumentList =new SolrDocumentList();
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField("",String.valueOf(1));
        solrDocument.setField(ScsbConstants.MATERIAL_TYPE, ScsbCommonConstants.MONOGRAPH);
        solrDocumentList.add(solrDocument);
        Mockito.when(mockOngoingMatchingReportsService.getSolrTemplate()).thenReturn(mocksolrTemplate1);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(Mockito.any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        List<Integer> serialMvmBibIds= Arrays.asList(1);
        ongoingMatchingReportsService.generateSerialAndMVMsReport(serialMvmBibIds);
        assertNotNull(serialMvmBibIds);
    }

    @Test
    public void generateSerialAndMVMsReportSolrException() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse= Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingReportsService,"solrTemplate",mocksolrTemplate1);
        ReflectionTestUtils.setField(ongoingMatchingReportsService,"solrQueryBuilder",solrQueryBuilder);
        SolrQuery solrQuery = new SolrQuery("testquery");
        solrQuery.setStart(1);
        solrQuery.setRows(1);
        SolrDocumentList solrDocumentList =new SolrDocumentList();
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.DOCTYPE,ScsbCommonConstants.ITEM);
        solrDocument.setField(ScsbCommonConstants.IS_DELETED_ITEM,false);
        solrDocument.setField(ScsbConstants.ITEM_CATALOGING_STATUS,ScsbCommonConstants.COMPLETE_STATUS);
        solrDocument.setField(ScsbCommonConstants.HOLDINGS_ID,Arrays.asList(1));
        solrDocumentList.add(solrDocument);
        solrDocumentList.setNumFound(11);
        Mockito.when(mockOngoingMatchingReportsService.getSolrTemplate()).thenReturn(mocksolrTemplate1);
        Mockito.when(mockOngoingMatchingReportsService.getSolrQueryBuilder()).thenReturn(solrQueryBuilder);
        Mockito.when(mockOngoingMatchingReportsService.getCamelContext()).thenReturn(camelContext);
        Mockito.when(mockOngoingMatchingReportsService.getProducerTemplate()).thenReturn(producerTemplate);
        Mockito.when(solrQueryBuilder.getSolrQueryForBibItem(Mockito.anyString())).thenReturn(solrQuery);
        Mockito.doNothing().when(producerTemplate).sendBodyAndHeader(String.valueOf(Mockito.anyInt()),Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(Mockito.any(SolrQuery.class))).thenReturn(queryResponse).thenThrow(NullPointerException.class);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        List<Integer> serialMvmBibIds= Arrays.asList(1);
        ongoingMatchingReportsService.generateSerialAndMVMsReport(serialMvmBibIds);
        assertNotNull(serialMvmBibIds);
    }

    @Test
    public void generateSerialAndMVMsReportCamelException() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse= Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingReportsService,"solrTemplate",mocksolrTemplate1);
        ReflectionTestUtils.setField(ongoingMatchingReportsService,"solrQueryBuilder",solrQueryBuilder);
        SolrQuery solrQuery = new SolrQuery("testquery");
        solrQuery.setStart(1);
        solrQuery.setRows(1);
        SolrDocumentList solrDocumentList =new SolrDocumentList();
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.DOCTYPE,ScsbCommonConstants.ITEM);
        solrDocument.setField(ScsbCommonConstants.IS_DELETED_ITEM,false);
        solrDocument.setField(ScsbConstants.ITEM_CATALOGING_STATUS,ScsbCommonConstants.COMPLETE_STATUS);
        solrDocument.setField(ScsbCommonConstants.HOLDINGS_ID,Arrays.asList(1));
        solrDocumentList.add(solrDocument);
        solrDocumentList.setNumFound(11);
        Mockito.when(mockOngoingMatchingReportsService.getSolrTemplate()).thenReturn(mocksolrTemplate1);
        Mockito.when(mockOngoingMatchingReportsService.getSolrQueryBuilder()).thenReturn(solrQueryBuilder);
        Mockito.when(mockOngoingMatchingReportsService.getCamelContext()).thenReturn(camelContext);
        Mockito.when(mockOngoingMatchingReportsService.getProducerTemplate()).thenReturn(producerTemplate);
        Mockito.when(solrQueryBuilder.getSolrQueryForBibItem(Mockito.anyString())).thenReturn(solrQuery);
        Mockito.doNothing().when(producerTemplate).sendBodyAndHeader(String.valueOf(Mockito.anyInt()),Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(Mockito.any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        List<Integer> serialMvmBibIds= Arrays.asList(1);
        ongoingMatchingReportsService.getInstitutionDetailsRepository();
        ongoingMatchingReportsService.generateSerialAndMVMsReport(serialMvmBibIds);
        assertNotNull(serialMvmBibIds);
    }

    @Test
    public void generateSerialAndMVMsReport() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse= Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingReportsService,"solrTemplate",mocksolrTemplate1);
        ReflectionTestUtils.setField(ongoingMatchingReportsService,"solrQueryBuilder",solrQueryBuilder);
        SolrQuery solrQuery = new SolrQuery("testquery");
        solrQuery.setStart(1);
        solrQuery.setRows(1);
        SolrDocumentList solrDocumentList =new SolrDocumentList();
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.DOCTYPE,ScsbCommonConstants.ITEM);
        solrDocument.setField(ScsbCommonConstants.IS_DELETED_ITEM,false);
        solrDocument.setField(ScsbConstants.ITEM_CATALOGING_STATUS,ScsbCommonConstants.COMPLETE_STATUS);
        solrDocument.setField(ScsbCommonConstants.HOLDINGS_ID,Arrays.asList(1));
        solrDocumentList.add(solrDocument);
        solrDocumentList.setNumFound(11);
        Mockito.when(mockOngoingMatchingReportsService.getSolrTemplate()).thenReturn(mocksolrTemplate1);
        Mockito.when(mockOngoingMatchingReportsService.getSolrQueryBuilder()).thenReturn(solrQueryBuilder);
        Mockito.when(mockOngoingMatchingReportsService.getCamelContext()).thenReturn(camelContext);
        Mockito.when(mockOngoingMatchingReportsService.getProducerTemplate()).thenReturn(producerTemplate);
        Mockito.when(camelContext.getRouteController()).thenReturn(routeController);
        Mockito.doNothing().when(routeController).startRoute(Mockito.anyString());
        Mockito.when(solrQueryBuilder.getSolrQueryForBibItem(Mockito.anyString())).thenReturn(solrQuery);
        Mockito.doNothing().when(producerTemplate).sendBodyAndHeader(String.valueOf(Mockito.anyInt()),Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(Mockito.any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        List<Integer> serialMvmBibIds= Arrays.asList(1);
        ongoingMatchingReportsService.generateSerialAndMVMsReport(serialMvmBibIds);
        assertNotNull(serialMvmBibIds);
    }


    @Test
    public void generateSerialAndMVMsReportForHolding() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse= Mockito.mock(QueryResponse.class);
        ReflectionTestUtils.setField(ongoingMatchingReportsService,"solrTemplate",mocksolrTemplate1);
        ReflectionTestUtils.setField(ongoingMatchingReportsService,"solrQueryBuilder",solrQueryBuilder);
        SolrQuery solrQuery = new SolrQuery("testquery");
        solrQuery.setStart(1);
        solrQuery.setRows(1);
        SolrDocumentList solrDocumentList =new SolrDocumentList();
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.DOCTYPE,ScsbCommonConstants.HOLDINGS);
        solrDocument.setField(ScsbCommonConstants.HOLDING_ID,345);
        solrDocument.setField(ScsbConstants.SUMMARY_HOLDINGS,"45");
        solrDocumentList.add(solrDocument);
        solrDocumentList.setNumFound(11);
        Mockito.when(mockOngoingMatchingReportsService.getSolrTemplate()).thenReturn(mocksolrTemplate1);
        Mockito.when(mockOngoingMatchingReportsService.getSolrQueryBuilder()).thenReturn(solrQueryBuilder);
        Mockito.when(solrQueryBuilder.getSolrQueryForBibItem(Mockito.anyString())).thenReturn(solrQuery);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(Mockito.any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        List<Integer> serialMvmBibIds= Arrays.asList(1);
        ongoingMatchingReportsService.generateSerialAndMVMsReport(serialMvmBibIds);
        assertNotNull(serialMvmBibIds);
    }

    @Test
    public void generateSummaryReport() throws Exception {
        String[] institutions={ScsbCommonConstants.PRINCETON,ScsbCommonConstants.COLUMBIA,ScsbCommonConstants.NYPL};
        for (String institution:institutions) {
            List<String> allInstitutionCodeExceptSupportInstitution=Arrays.asList(ScsbCommonConstants.COLUMBIA,ScsbCommonConstants.PRINCETON,ScsbCommonConstants.NYPL);
            Mockito.when(commonUtil.findAllInstitutionCodesExceptSupportInstitution()).thenReturn(allInstitutionCodeExceptSupportInstitution);
            List<MatchingSummaryReport> matchingSummaryReports=new ArrayList<>();
        matchingSummaryReports.add(getMatchingSummaryReport(institution));
        SolrQuery solrQuery= new SolrQuery();
        Mockito.when(solrQueryBuilder.getCountQueryForParentAndChildCriteria(Mockito.any())).thenReturn(solrQuery);
        Mockito.when(solrQueryBuilder.getCountQueryForChildAndParentCriteria(Mockito.any())).thenReturn(solrQuery);
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(ongoingMatchingReportsService,"solrTemplate",mocksolrTemplate1);
        Mockito.when(mockOngoingMatchingReportsService.getSolrTemplate()).thenReturn(mocksolrTemplate1);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse= Mockito.mock(QueryResponse.class);
        SolrDocumentList solrDocumentList =new SolrDocumentList();
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.DOCTYPE,ScsbCommonConstants.HOLDINGS);
        solrDocument.setField(ScsbCommonConstants.HOLDING_ID,345);
        solrDocument.setField(ScsbConstants.SUMMARY_HOLDINGS,"45");
        solrDocumentList.add(solrDocument);
        solrDocumentList.setNumFound(11);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(Mockito.any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        Mockito.when(camelContext.getRouteController()).thenReturn(routeController);
        Mockito.when(propertyUtil.getAllInstitutions()).thenReturn(Arrays.asList("PUL","CUL","NYPL","HL", supportInstitution));
        ongoingMatchingReportsService.generateSummaryReport(matchingSummaryReports);
        assertNotNull(matchingSummaryReports);
        }
    }

    @Test
    public void generateSummaryReportException() throws Exception {
        String[] institutions={ScsbCommonConstants.PRINCETON,ScsbCommonConstants.COLUMBIA,ScsbCommonConstants.NYPL};
        for (String institution:institutions) {
            List<String> allInstitutionCodeExceptSupportInstitution=Arrays.asList(ScsbCommonConstants.COLUMBIA,ScsbCommonConstants.PRINCETON,ScsbCommonConstants.NYPL);
            Mockito.when(commonUtil.findAllInstitutionCodesExceptSupportInstitution()).thenReturn(allInstitutionCodeExceptSupportInstitution);
            List<MatchingSummaryReport> matchingSummaryReports=new ArrayList<>();
            matchingSummaryReports.add(getMatchingSummaryReport(institution));
            SolrQuery solrQuery= new SolrQuery();
            Mockito.when(solrQueryBuilder.getCountQueryForParentAndChildCriteria(Mockito.any())).thenReturn(solrQuery);
            Mockito.when(solrQueryBuilder.getCountQueryForChildAndParentCriteria(Mockito.any())).thenReturn(solrQuery);
            SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
            ReflectionTestUtils.setField(ongoingMatchingReportsService,"solrTemplate",mocksolrTemplate1);
            Mockito.when(mockOngoingMatchingReportsService.getSolrTemplate()).thenReturn(mocksolrTemplate1);
            SolrClient solrClient=PowerMockito.mock(SolrClient.class);
            QueryResponse queryResponse= Mockito.mock(QueryResponse.class);
            SolrDocumentList solrDocumentList =new SolrDocumentList();
            SolrDocument solrDocument = new SolrDocument();
            solrDocument.setField(ScsbCommonConstants.DOCTYPE,ScsbCommonConstants.HOLDINGS);
            solrDocument.setField(ScsbCommonConstants.HOLDING_ID,345);
            solrDocument.setField(ScsbConstants.SUMMARY_HOLDINGS,"45");
            solrDocumentList.add(solrDocument);
            solrDocumentList.setNumFound(11);
            PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
            Mockito.when(solrClient.query(Mockito.any(SolrQuery.class))).thenThrow(NullPointerException.class);
            Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
            Mockito.when(propertyUtil.getAllInstitutions()).thenReturn(Arrays.asList("PUL","CUL","NYPL","HL", supportInstitution));
            ongoingMatchingReportsService.generateSummaryReport(matchingSummaryReports);
            assertNotNull(matchingSummaryReports);
        }
    }

    @Test
    public void populateSummaryReport() throws Exception {
        List<String> allInstitutionCodeExceptSupportInstitution=Arrays.asList(ScsbCommonConstants.COLUMBIA,ScsbCommonConstants.PRINCETON,ScsbCommonConstants.NYPL);
        Mockito.when(commonUtil.findAllInstitutionCodesExceptSupportInstitution()).thenReturn(allInstitutionCodeExceptSupportInstitution);
        List<InstitutionEntity> institutionEntities=new ArrayList<>();
        institutionEntities.add(TestUtil.getInstitutionEntity(3,ScsbCommonConstants.NYPL,ScsbCommonConstants.NYPL));
        institutionEntities.add(TestUtil.getInstitutionEntity(2,ScsbCommonConstants.COLUMBIA,ScsbCommonConstants.COLUMBIA));
        institutionEntities.add(TestUtil.getInstitutionEntity(1,ScsbCommonConstants.PRINCETON,ScsbCommonConstants.PRINCETON));
        Mockito.when(institutionDetailsRepository.findByInstitutionCodeNotIn(Mockito.anyList())).thenReturn(institutionEntities);
        List<MatchingSummaryReport> matchingSummaryReports=ongoingMatchingReportsService.populateSummaryReportBeforeMatching();
        assertNotNull(matchingSummaryReports);
    }

    private MatchingSummaryReport getMatchingSummaryReport(String institution) {
        MatchingSummaryReport matchingSummaryReport=new MatchingSummaryReport();
        matchingSummaryReport.setInstitution(institution);
        matchingSummaryReport.setOpenItemsBeforeMatching("0");
        matchingSummaryReport.setSharedItemsBeforeMatching("0");
        return matchingSummaryReport;
    }
}
