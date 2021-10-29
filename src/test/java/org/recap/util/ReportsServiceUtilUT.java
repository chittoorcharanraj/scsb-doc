package org.recap.util;

import io.swagger.models.auth.In;
import org.apache.poi.ss.formula.functions.T;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.GroupResponse;
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
import org.recap.BaseTestCaseUT4;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.jpa.DeaccessionItemChangeLog;
import org.recap.model.jpa.MatchingScoreTranslationEntity;
import org.recap.model.reports.ReportsRequest;
import org.recap.model.reports.ReportsResponse;
import org.recap.model.reports.TitleMatchedReport;
import org.recap.model.reports.TitleMatchedReports;
import org.recap.model.search.DeaccessionItemResultsRow;
import org.recap.model.solr.BibItem;
import org.recap.model.solr.Item;
import org.recap.repository.jpa.DeaccesionItemChangeLogDetailsRepository;
import org.recap.repository.jpa.MatchingScoreTranslationRepository;
import org.recap.repository.solr.impl.BibSolrDocumentRepositoryImpl;
import org.recap.service.TitleMatchReportExportService;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;

/**
 * Created by rajeshbabuk on 13/1/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({SolrTemplate.class, SolrClient.class})
public class ReportsServiceUtilUT extends BaseTestCaseUT4 {

    @InjectMocks
    ReportsServiceUtil reportsServiceUtil;


    @Mock
    private DateUtil dateUtil;

    @Mock
    private SolrQueryBuilder solrQueryBuilder;

    @Mock
    private CommonUtil commonUtil;

    @Mock
    private DeaccesionItemChangeLogDetailsRepository deaccesionItemChangeLogDetailsRepository;

    @Mock
    TitleMatchedReport titleMatchedReport;

    @Mock
    SolrDocumentList solrDocumentList;

    @Mock
    SolrDocument bibSolrDocument;

    @Mock
    BibSolrDocumentRepositoryImpl bibSolrDocumentRepository;

    @Mock
    MatchingScoreTranslationRepository matchingScoreTranslationRepository;

    @Mock
    MatchingScoreTranslationEntity matchingScoreTranslationEntity;

    @Mock
    BibItem bibItem;

    @Mock
    Item item;

    @Mock
    TitleMatchReportExportService titleMatchReportExportService;

    @Mock
    QueryResponse queryResponse;

    @Before
    public void setup()throws Exception{
        MockitoAnnotations.initMocks(this);
        Mockito.when(dateUtil.getFromDateAccession(any())).thenCallRealMethod();
        Mockito.when(dateUtil.getToDateAccession(any())).thenCallRealMethod();
    }

    @Test
    public void titleMatchReports() throws Exception {
        MatchingScoreTranslationEntity matchingScoreTranslationEntity1=new MatchingScoreTranslationEntity();
        List<String> titleMatch=new ArrayList<>();
        titleMatch.add(ScsbConstants.TITLE_MATCHED);
        Mockito.when(titleMatchedReport.getTitleMatch()).thenReturn(String.valueOf(titleMatch));
        List<String> owningInst=new ArrayList<>();
        owningInst.add(ScsbCommonConstants.PRINCETON);
        Mockito.when(titleMatchedReport.getOwningInst()).thenReturn(String.valueOf(owningInst));
        List<String> cgd=new ArrayList<>();
        cgd.add(ScsbConstants.SHARED);
        Mockito.when(titleMatchedReport.getCgd()).thenReturn(cgd);
        Mockito.when(titleMatchedReport.getFromDate()).thenReturn(new Date());
        Mockito.when(titleMatchedReport.getToDate()).thenReturn(new Date());
        Mockito.when(titleMatchedReport.getPageSize()).thenReturn(1);
        Mockito.when(titleMatchedReport.getPageNumber()).thenReturn(1);
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(reportsServiceUtil,"solrTemplate",mocksolrTemplate1);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse=Mockito.mock(QueryResponse.class);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(solrQueryBuilder.buildQueryTitleMatchedReport(Mockito.anyString(), any(), any(), any(), Mockito.anyString())).thenCallRealMethod();

        SolrDocumentList solrDocumentList=new SolrDocumentList();
        solrDocumentList.add(bibSolrDocument);
        solrDocumentList.setNumFound(1l);
        Collection<String> fieldNames=new ArrayList<>();
        fieldNames.add("bib");
        Mockito.when(bibSolrDocument.getFieldNames()).thenReturn(fieldNames);
        Mockito.when(bibSolrDocument.getFieldValue(Mockito.anyString())).thenReturn(fieldNames);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        ReflectionTestUtils.setField(bibSolrDocumentRepository,"commonUtil",commonUtil);
        Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(), anyList(), any())).thenCallRealMethod();
        Mockito.doCallRealMethod().when(bibSolrDocumentRepository).populateBibItem(any(), any());
        List<MatchingScoreTranslationEntity> msList=new ArrayList<>();
        msList.add(matchingScoreTranslationEntity);
        Mockito.when(matchingScoreTranslationEntity.getDecMaScore()).thenReturn(1);
        Mockito.when(matchingScoreTranslationEntity.getStringMaScore()).thenReturn("1");
        Mockito.when(matchingScoreTranslationRepository.findAll()).thenReturn(msList);
        TitleMatchedReport titleMatchCount=reportsServiceUtil.titleMatchReportsPreview(titleMatchedReport);
        assertNotNull(titleMatchCount);
    }

    @Test
    public void prepareTitleReport(){
        Map<Integer, String> msMap=new HashMap<>();
        Mockito.when(bibItem.getMatchingIdentifier()).thenReturn("match");
        Mockito.when(bibItem.getLccn()).thenReturn("lccn");
        Mockito.when(bibItem.getTitle()).thenReturn("title");
        Mockito.when(bibItem.getIsbn()).thenReturn(Arrays.asList("ISBN"));
        Mockito.when(bibItem.getIssn()).thenReturn(Arrays.asList("ISSN"));
        List<Item> items=new ArrayList<>();
        items.add(item);
        Mockito.when(item.getBarcode()).thenReturn("123456");
        Mockito.when(bibItem.getBarcode()).thenReturn("123456");
        Mockito.when(bibItem.getItems()).thenReturn(items);
        List<String> cgd=new ArrayList<>();
        cgd.add(ScsbConstants.SHARED);
        ReflectionTestUtils.invokeMethod(reportsServiceUtil,"prepareTitleReport",bibItem,item,msMap,false);
        ReflectionTestUtils.invokeMethod(reportsServiceUtil,"setItemDetails",bibItem);
        ReflectionTestUtils.invokeMethod(reportsServiceUtil,"setCGD",bibItem);
        ReflectionTestUtils.invokeMethod(reportsServiceUtil,"setDataToTitleMatchReports",bibItem,msMap);
        assertNotNull(items);
    }

    @Test
    public void titleMatchReportsExportS3() throws Exception {
        List<String> titleMatch=new ArrayList<>();
        titleMatch.add(ScsbConstants.TITLE_MATCHED);
        Mockito.when(titleMatchedReport.getTitleMatch()).thenReturn(String.valueOf(titleMatch));
        List<String> owningInst=new ArrayList<>();
        owningInst.add(ScsbCommonConstants.PRINCETON);
        Mockito.when(titleMatchedReport.getOwningInst()).thenReturn(String.valueOf(owningInst));
        Mockito.when(titleMatchReportExportService.process(titleMatchedReport)).thenReturn(titleMatchedReport);
        List<String> cgd=new ArrayList<>();
        cgd.add(ScsbConstants.SHARED);
        Mockito.when(titleMatchedReport.getCgd()).thenReturn(cgd);
        Mockito.when(titleMatchedReport.getFromDate()).thenReturn(new Date());
        Mockito.when(titleMatchedReport.getToDate()).thenReturn(new Date());
        Mockito.when(titleMatchedReport.getPageSize()).thenReturn(1);
        Mockito.when(titleMatchedReport.getPageNumber()).thenReturn(1);
        Mockito.when(titleMatchedReport.getTotalRecordsCount()).thenReturn(1L);
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(reportsServiceUtil,"titleReportExportBibsLimitPerFile",1);
        ReflectionTestUtils.setField(reportsServiceUtil,"solrTemplate",mocksolrTemplate1);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse=Mockito.mock(QueryResponse.class);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(solrQueryBuilder.buildQueryTitleMatchedReport(Mockito.anyString(), any(), any(), any(),Mockito.anyString())).thenCallRealMethod();

        SolrDocumentList solrDocumentList=new SolrDocumentList();
        solrDocumentList.add(bibSolrDocument);
        solrDocumentList.setNumFound(1l);
        Collection<String> fieldNames=new ArrayList<>();
        fieldNames.add("bib");
        Mockito.when(bibSolrDocument.getFieldNames()).thenReturn(fieldNames);
        Mockito.when(bibSolrDocument.getFieldValue(Mockito.anyString())).thenReturn(fieldNames);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        ReflectionTestUtils.setField(bibSolrDocumentRepository,"commonUtil",commonUtil);
        Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(), anyList(), any())).thenCallRealMethod();
        Mockito.doCallRealMethod().when(bibSolrDocumentRepository).populateBibItem(any(), any());
        reportsServiceUtil.titleMatchReportsExport(titleMatchedReport);
    }
   @Test
    public void titleMatchReportsExport() throws Exception {
        List<String> titleMatch=new ArrayList<>();
        titleMatch.add(ScsbConstants.TITLE_MATCHED);
        Mockito.when(titleMatchedReport.getTitleMatch()).thenReturn(String.valueOf(titleMatch));
        List<String> owningInst=new ArrayList<>();
        owningInst.add(ScsbCommonConstants.PRINCETON);
        Mockito.when(titleMatchedReport.getOwningInst()).thenReturn(String.valueOf(owningInst));
        Mockito.when(titleMatchReportExportService.process(titleMatchedReport)).thenReturn(titleMatchedReport);
        List<String> cgd=new ArrayList<>();
        cgd.add(ScsbConstants.SHARED);
        Mockito.when(titleMatchedReport.getCgd()).thenReturn(cgd);
        Mockito.when(titleMatchedReport.getFromDate()).thenReturn(new Date());
        Mockito.when(titleMatchedReport.getToDate()).thenReturn(new Date());
        Mockito.when(titleMatchedReport.getPageSize()).thenReturn(1);
        Mockito.when(titleMatchedReport.getPageNumber()).thenReturn(1);
        Mockito.when(titleMatchedReport.getTotalRecordsCount()).thenReturn(1L);
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(reportsServiceUtil,"solrTemplate",mocksolrTemplate1);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse=Mockito.mock(QueryResponse.class);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(solrQueryBuilder.buildQueryTitleMatchedReport(Mockito.anyString(), any(), any(), any(),Mockito.anyString())).thenCallRealMethod();

        SolrDocumentList solrDocumentList=new SolrDocumentList();
        solrDocumentList.add(bibSolrDocument);
        solrDocumentList.setNumFound(1l);
        Collection<String> fieldNames=new ArrayList<>();
        fieldNames.add("bib");
        Mockito.when(bibSolrDocument.getFieldNames()).thenReturn(fieldNames);
        Mockito.when(bibSolrDocument.getFieldValue(Mockito.anyString())).thenReturn(fieldNames);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        ReflectionTestUtils.setField(bibSolrDocumentRepository,"commonUtil",commonUtil);
        Mockito.when(commonUtil.getBibItemFromSolrFieldNames(any(), anyList(), any())).thenCallRealMethod();
        Mockito.doCallRealMethod().when(bibSolrDocumentRepository).populateBibItem(any(), any());
        TitleMatchedReport titleMatchCount=reportsServiceUtil.titleMatchReportsExport(titleMatchedReport);
        assertNotNull(titleMatchCount);
    }

    @Test
    public void titleMatchCount() throws Exception {
        List<String> titleMatch=new ArrayList<>();
        titleMatch.add(ScsbConstants.TITLE_MATCHED);
        Mockito.when(titleMatchedReport.getTitleMatch()).thenReturn(String.valueOf(titleMatch));
        List<String> owningInst=new ArrayList<>();
        owningInst.add(ScsbCommonConstants.PRINCETON);
        Mockito.when(titleMatchedReport.getOwningInst()).thenReturn(String.valueOf(owningInst));
        List<String> cgd=new ArrayList<>();
        cgd.add(ScsbConstants.SHARED);
        Mockito.when(titleMatchedReport.getCgd()).thenReturn(cgd);
        Mockito.when(titleMatchedReport.getFromDate()).thenReturn(new Date());
        Mockito.when(titleMatchedReport.getToDate()).thenReturn(new Date());
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(reportsServiceUtil,"solrTemplate",mocksolrTemplate1);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse=Mockito.mock(QueryResponse.class);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        Mockito.when(solrDocumentList.getNumFound()).thenReturn(1l);
        Mockito.when(solrQueryBuilder.buildQueryTitleMatchCount(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenCallRealMethod();
        TitleMatchedReport titleMatchCount=reportsServiceUtil.titleMatchCount(titleMatchedReport);
        assertNotNull(titleMatchCount);
    }

    @Test
    public void populateAccessionDeaccessionItemCounts() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String requestedFromDate = simpleDateFormat.format(new Date());
        String requestedToDate = simpleDateFormat.format(new Date());

        ReportsRequest reportsRequest = new ReportsRequest();
        reportsRequest.setAccessionDeaccessionFromDate(requestedFromDate);
        reportsRequest.setAccessionDeaccessionToDate(requestedToDate);
        reportsRequest.setOwningInstitutions(Arrays.asList("CUL", "PUL", "NYPL"));
        reportsRequest.setCollectionGroupDesignations(Arrays.asList("Private", "Open", "Shared"));

        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(reportsServiceUtil,"solrTemplate",mocksolrTemplate1);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse=Mockito.mock(QueryResponse.class);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        GroupResponse groupResponse=Mockito.mock(GroupResponse.class);
        Mockito.when(queryResponse.getGroupResponse()).thenReturn(groupResponse);
        List<GroupCommand> values=new ArrayList<>();
        GroupCommand groupCommand=new GroupCommand(ScsbCommonConstants.IS_DELETED_ITEM,1);
        SolrDocumentList solrDocumentList=new SolrDocumentList();
        SolrDocument solrDocument=new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.IS_DELETED_ITEM,true);
        solrDocumentList.add(solrDocument);
        Group group=new Group(ScsbCommonConstants.IS_DELETED_ITEM,solrDocumentList);
        groupCommand.add(group);
        values.add(groupCommand);
        Mockito.when(groupResponse.getValues()).thenReturn(values);
        Map<String, FieldStatsInfo> getFieldStatsInfo=new HashMap<>();
        FieldStatsInfo fieldStatsInfo=Mockito.mock(FieldStatsInfo.class);;
        getFieldStatsInfo.put(ScsbCommonConstants.BARCODE,fieldStatsInfo);
        Mockito.when(queryResponse.getFieldStatsInfo()).thenReturn(getFieldStatsInfo);
        SolrQuery query=new SolrQuery("testquery");
        Mockito.when(solrQueryBuilder.buildSolrQueryForAccessionReports(any(),Mockito.anyString(),Mockito.anyBoolean(),Mockito.anyString())).thenReturn(query);
        Mockito.when(solrQueryBuilder.buildSolrQueryForDeaccessionReports(any(),Mockito.anyString(),Mockito.anyBoolean(),Mockito.anyString())).thenReturn(query);
        ReportsResponse reportsResponse = reportsServiceUtil.populateAccessionDeaccessionItemCounts(reportsRequest);
        assertNotNull(reportsResponse);
    }

    @Test
    public void populateCGDItemCounts() throws Exception {
        ReportsRequest reportsRequest = new ReportsRequest();
        reportsRequest.setOwningInstitutions(Arrays.asList("CUL", "PUL", "NYPL"));
        reportsRequest.setCollectionGroupDesignations(Arrays.asList(ScsbCommonConstants.REPORTS_PRIVATE,ScsbCommonConstants.REPORTS_OPEN,ScsbCommonConstants.REPORTS_SHARED,ScsbCommonConstants.REPORTS_COMMITTED,ScsbCommonConstants.REPORTS_UNCOMMITTABLE));
        SolrQuery query=new SolrQuery("testquery");
        Mockito.when(solrQueryBuilder.buildSolrQueryForCGDReports(Mockito.anyString(),Mockito.anyString())).thenReturn(query);
        Mockito.when(solrQueryBuilder.buildSolrQueryForDeaccesionReportInformation(any(),Mockito.anyString(),Mockito.anyBoolean())).thenReturn(query);
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(reportsServiceUtil,"solrTemplate",mocksolrTemplate1);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse=Mockito.mock(QueryResponse.class);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        GroupResponse groupResponse=Mockito.mock(GroupResponse.class);
        Mockito.when(queryResponse.getGroupResponse()).thenReturn(groupResponse);
        List<GroupCommand> values=new ArrayList<>();
        GroupCommand groupCommand=new GroupCommand(ScsbCommonConstants.IS_DELETED_ITEM,1);
        SolrDocumentList solrDocumentList=new SolrDocumentList();
        SolrDocument solrDocument=new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.IS_DELETED_ITEM,true);
        solrDocumentList.add(solrDocument);
        Group group=new Group(ScsbCommonConstants.IS_DELETED_ITEM,solrDocumentList);
        groupCommand.add(group);
        values.add(groupCommand);
        Mockito.when(groupResponse.getValues()).thenReturn(values);
        Map<String, FieldStatsInfo> getFieldStatsInfo=new HashMap<>();
        FieldStatsInfo fieldStatsInfo=Mockito.mock(FieldStatsInfo.class);;
        getFieldStatsInfo.put(ScsbCommonConstants.BARCODE,fieldStatsInfo);
        Mockito.when(queryResponse.getFieldStatsInfo()).thenReturn(getFieldStatsInfo);
        ReportsResponse reportsResponse = reportsServiceUtil.populateCgdItemCounts(reportsRequest);
        assertNotNull(reportsResponse);
    }

    @Test
    public void populateIncompleteRecordsReport() throws Exception {
        Boolean[] booleans={true,false};
        for (Boolean b: booleans) {
            ReportsRequest reportsRequest = new ReportsRequest();
        reportsRequest.setIncompletePageSize(1);
        reportsRequest.setIncompletePageNumber(1);
        reportsRequest.setExport(b);
        reportsRequest.setOwningInstitutions(Arrays.asList("CUL", "PUL", "NYPL"));
        reportsRequest.setCollectionGroupDesignations(Arrays.asList("Private", "Open", "Shared"));
        SolrQuery query = new SolrQuery("testquery");
        Mockito.when(solrQueryBuilder.buildSolrQueryForIncompleteReports(reportsRequest.getIncompleteRequestingInstitution())).thenReturn(query);
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(reportsServiceUtil, "solrTemplate", mocksolrTemplate1);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.when(solrClient.query(query, SolrRequest.METHOD.POST)).thenReturn(queryResponse);
        GroupResponse groupResponse = Mockito.mock(GroupResponse.class);
        Mockito.when(queryResponse.getGroupResponse()).thenReturn(groupResponse);
        List<GroupCommand> values = new ArrayList<>();
        GroupCommand groupCommand = new GroupCommand(ScsbCommonConstants.IS_DELETED_ITEM, 1);
        SolrDocumentList solrDocumentList = new SolrDocumentList();
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.IS_DELETED_ITEM, true);
        solrDocument.setField(ScsbCommonConstants.BIB_ID, 1);
        solrDocumentList.add(solrDocument);
        Group group = new Group(ScsbCommonConstants.IS_DELETED_ITEM, solrDocumentList);
        groupCommand.add(group);
        values.add(groupCommand);
        Mockito.when(groupResponse.getValues()).thenReturn(values);
        Map<String, FieldStatsInfo> getFieldStatsInfo = new HashMap<>();
        FieldStatsInfo fieldStatsInfo = Mockito.mock(FieldStatsInfo.class);
        getFieldStatsInfo.put(ScsbCommonConstants.BARCODE, fieldStatsInfo);
        Mockito.when(queryResponse.getFieldStatsInfo()).thenReturn(getFieldStatsInfo);
            Item item = getItem();
            item.setItemCreatedDate(new Date());
        Mockito.when(commonUtil.getItem(any())).thenReturn(item);
        Mockito.when(solrQueryBuilder.buildSolrQueryToGetBibDetails(anyList(),Mockito.anyInt())).thenReturn(query);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        ReportsResponse reportsResponse = reportsServiceUtil.populateIncompleteRecordsReport(reportsRequest);
        assertNotNull(reportsResponse);
        }
    }

    @Test
    public void populateDeaccessionResults() throws Exception {
        ReportsResponse reportsResponse1 = new ReportsResponse();
        List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = new ArrayList<>();
        deaccessionItemResultsRowList.add(new DeaccessionItemResultsRow());
        reportsResponse1.setDeaccessionItemResultsRows(deaccessionItemResultsRowList);
        SolrQuery query=new SolrQuery("testquery");
        Mockito.when(solrQueryBuilder.buildSolrQueryForDeaccesionReportInformation(any(),Mockito.anyString(),Mockito.anyBoolean())).thenReturn(query);
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(reportsServiceUtil,"solrTemplate",mocksolrTemplate1);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse=Mockito.mock(QueryResponse.class);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        GroupResponse groupResponse=Mockito.mock(GroupResponse.class);
        Mockito.when(queryResponse.getGroupResponse()).thenReturn(groupResponse);
        Mockito.when(groupResponse.getValues()).thenReturn(getGroupCommands());
        Map<String, FieldStatsInfo> getFieldStatsInfo=new HashMap<>();
        FieldStatsInfo fieldStatsInfo=Mockito.mock(FieldStatsInfo.class);
        getFieldStatsInfo.put(ScsbCommonConstants.BARCODE,fieldStatsInfo);
        Mockito.when(queryResponse.getFieldStatsInfo()).thenReturn(getFieldStatsInfo);
        Mockito.when(commonUtil.getItem(any())).thenReturn(getItem());
        Mockito.when(queryResponse.getResults()).thenReturn(getSolrDocuments());
        Mockito.when(deaccesionItemChangeLogDetailsRepository.findByRecordIdAndOperationTypeAndOrderByUpdatedDateDesc(Mockito.anyInt(),Mockito.anyString())).thenReturn(getDeaccessionItemChangeLogs());
        ReportsResponse reportsResponse = reportsServiceUtil.populateDeaccessionResults(getReportsRequest());
        assertNotNull(reportsResponse);
        assertNotNull(reportsResponse.getDeaccessionItemResultsRows());
        assertTrue(reportsResponse.getDeaccessionItemResultsRows().size() > 0);
        List<DeaccessionItemResultsRow> deaccessionItemResultsRows = reportsResponse.getDeaccessionItemResultsRows();
        assertNotNull(deaccessionItemResultsRows);
        assertTrue(deaccessionItemResultsRows.size() > 0);
    }

    private List<DeaccessionItemChangeLog> getDeaccessionItemChangeLogs() {
        List<DeaccessionItemChangeLog> itemChangeLogEntityList = new ArrayList<>();
        DeaccessionItemChangeLog deaccessionItemChangeLog = new DeaccessionItemChangeLog();
        deaccessionItemChangeLog.setRecordId(1);
        itemChangeLogEntityList.add(deaccessionItemChangeLog);
        return itemChangeLogEntityList;
    }

    private List<GroupCommand> getGroupCommands() {
        List<GroupCommand> values = new ArrayList<>();
        GroupCommand groupCommand = new GroupCommand(ScsbCommonConstants.IS_DELETED_ITEM, 1);
        Group group = new Group(ScsbCommonConstants.IS_DELETED_ITEM, getSolrDocuments());
        groupCommand.add(group);
        values.add(groupCommand);
        return values;
    }

    private SolrDocumentList getSolrDocuments() {
        SolrDocumentList solrDocumentList = new SolrDocumentList();
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.IS_DELETED_ITEM, true);
        solrDocument.setField(ScsbCommonConstants.BIB_ID, 1);
        solrDocument.setField(ScsbConstants.TITLE_DISPLAY, "test");
        solrDocumentList.add(solrDocument);
        return solrDocumentList;
    }

    @Test
    public void testPopulateDeaccessionResultsForPageCount() throws Exception {
        ReportsResponse reportsResponse1 = new ReportsResponse();
        List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = new ArrayList<>();
        deaccessionItemResultsRowList.add(new DeaccessionItemResultsRow());
        reportsResponse1.setDeaccessionItemResultsRows(deaccessionItemResultsRowList);
        SolrQuery query=new SolrQuery("testquery");
        Mockito.when(solrQueryBuilder.buildSolrQueryForDeaccesionReportInformation(any(),Mockito.anyString(),Mockito.anyBoolean())).thenReturn(query);
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(reportsServiceUtil,"solrTemplate",mocksolrTemplate1);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        QueryResponse queryResponse=Mockito.mock(QueryResponse.class);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        GroupResponse groupResponse=Mockito.mock(GroupResponse.class);
        Mockito.when(queryResponse.getGroupResponse()).thenReturn(groupResponse);
        Mockito.when(groupResponse.getValues()).thenReturn(getGroupCommands());
        Map<String, FieldStatsInfo> getFieldStatsInfo=new HashMap<>();
        FieldStatsInfo fieldStatsInfo=Mockito.mock(FieldStatsInfo.class);
        getFieldStatsInfo.put(ScsbCommonConstants.BARCODE,fieldStatsInfo);
        Mockito.when(queryResponse.getFieldStatsInfo()).thenReturn(getFieldStatsInfo);
        Mockito.when(getFieldStatsInfo.get(ScsbCommonConstants.BARCODE).getCountDistinct()).thenReturn(10l);
        Mockito.when(commonUtil.getItem(any())).thenReturn(getItem());
        Mockito.when(queryResponse.getResults()).thenReturn(getSolrDocuments());
        Mockito.when(deaccesionItemChangeLogDetailsRepository.findByRecordIdAndOperationTypeAndOrderByUpdatedDateDesc(Mockito.anyInt(),Mockito.anyString())).thenReturn(getDeaccessionItemChangeLogs());
        ReportsResponse reportsResponse = reportsServiceUtil.populateDeaccessionResults(getReportsRequest());
        assertNotNull(reportsResponse);
        assertNotNull(reportsResponse.getDeaccessionItemResultsRows());
        assertTrue(reportsResponse.getDeaccessionItemResultsRows().size() > 0);
        List<DeaccessionItemResultsRow> deaccessionItemResultsRows = reportsResponse.getDeaccessionItemResultsRows();
        assertNotNull(deaccessionItemResultsRows);
        assertTrue(deaccessionItemResultsRows.size() > 0);
    }

    private ReportsRequest getReportsRequest() {
        ReportsRequest reportsRequest = new ReportsRequest();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        reportsRequest.setAccessionDeaccessionFromDate(simpleDateFormat.format(new Date()));
        reportsRequest.setAccessionDeaccessionToDate(simpleDateFormat.format(new Date()));
        reportsRequest.setDeaccessionOwningInstitution("PUL");
        return reportsRequest;
    }

    private Item getItem() {
        Item item = new Item();
        item.setItemId(1);
        item.setItemBibIdList(Arrays.asList(1, 2, 3));
        item.setItemLastUpdatedDate(new Date());
        return item;
    }

    @Test
    public  void getTitleMatchedReportsExportS3() throws  Exception
    {
        long num = 234l;
        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        Date dateobj = new Date();
        List<String> cgd = new ArrayList<>();
        cgd.add(0,"open");
        List<TitleMatchedReports> titleMatched = new ArrayList<>();
        TitleMatchedReports titleMatchedReports = new TitleMatchedReports();
        titleMatchedReports.setTitle("test");
        TitleMatchedReport titleMatchedReport = new TitleMatchedReport();
        titleMatchedReport.setTitleMatch("test");
        titleMatchedReport.setTotalPageCount(100);
        titleMatchedReport.setPageNumber(10);
        titleMatchedReport.setOwningInst("PUL");
        titleMatchedReport.setCgd(cgd);
        titleMatchedReport.setMessage("test");
        titleMatchedReport.setTotalRecordsCount(num);
        titleMatchedReport.setFromDate(dateobj);
        titleMatchedReport.setToDate(dateobj);
        titleMatchedReport.setTitleMatchedReports(titleMatched);
        titleMatchedReport.setReportMessage("test");
        titleMatched.add(0,titleMatchedReports);
        SolrQuery query = new SolrQuery();
        Integer Rows = 10;
        Integer start = 5;
        query.setRows(Rows);
        query.setStart(start);
        ReflectionTestUtils.setField(reportsServiceUtil,"titleReportExportBibsLimitPerFile",1);
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(reportsServiceUtil,"solrTemplate",mocksolrTemplate1);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
       //QueryResponse queryResponse=Mockito.mock(QueryResponse.class);
        SolrDocumentList solrDocumentList = new SolrDocumentList();
        solrDocumentList.setNumFound(1l);
        Mockito.doNothing().when(bibSolrDocumentRepository).populateBibItem(any(), any());
        Mockito.doNothing().when(bibSolrDocumentRepository).populateItemHoldingsInfo(any(), anyBoolean(), anyString());
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        Mockito.doReturn(solrDocumentList).when(queryResponse).getResults();
        Mockito.when(solrQueryBuilder.buildQueryTitleMatchedReport(any(),any(),any(),any(),any())).thenReturn(query);
        TitleMatchedReport report =  reportsServiceUtil.getTitleMatchedReportsExportS3(titleMatchedReport);
        assertNotNull(report);
    }

}
