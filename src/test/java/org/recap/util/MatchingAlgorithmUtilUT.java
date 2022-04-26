package org.recap.util;


import org.apache.camel.ProducerTemplate;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.FacetField;
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
import org.recap.BaseTestCaseUT;
import org.recap.BaseTestCaseUT4;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.controller.SolrIndexController;
import org.recap.matchingalgorithm.MatchScoreReport;
import org.recap.model.jpa.*;
import org.recap.model.solr.BibItem;
import org.recap.repository.jpa.*;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;


/**
 * Created by Anitha on 10/10/20.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({SolrTemplate.class,SolrClient.class})
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
public class MatchingAlgorithmUtilUT extends BaseTestCaseUT {

    @InjectMocks
    MatchingAlgorithmUtil mockMatchingAlgorithmUtil;

    @Mock
    BibliographicDetailsRepositoryForMatching bibliographicDetailsRepositoryForMatching;

    @Mock
    private SolrQueryBuilder solrQueryBuilder;

    @Mock
    MatchingBibDetailsRepository matchingBibDetailsRepository;

    @Mock
    ProducerTemplate producerTemplate;

    @Mock
    MatchingAlgorithmReportDataDetailsRepository matchingAlgorithmReportDataDetailsRepository;

    @Mock
    ReportDetailRepository reportDetailRepository;

    @Mock
    CommonUtil commonUtil;

    @Mock
    InstitutionDetailsRepository institutionDetailsRepository;

    @Mock
    MatchingAlgorithmReportDetailRepository matchingAlgorithmReportDetailRepository;

    @Mock
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Mock
    BibliographicEntity bibliographicEntity;

    @Mock
    SolrIndexController bibItemIndexExecutorService;

    @Mock
    MatchingAlgorithmReportDataEntity matchingAlgorithmReportDataEntity;

    @Mock
    SolrQuery solrQuery;

    @Mock
    Collection<BibliographicEntity> bibliographicEntities;

    @Mock
    EntityManager entityManager;

    @Mock
    SolrTemplate solrTemplate;

    @Mock
    CoreAdminRequest coreAdminRequest;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(mockMatchingAlgorithmUtil,"matchingHeaderValueLength",8000);
        ReflectionTestUtils.setField(commonUtil,"institutionDetailsRepository",institutionDetailsRepository);
    }

    @Test
    public void updateBibForMatchingIdentifier() throws Exception {
        List<Integer> bibIdList=new ArrayList<>();
        bibIdList.add(1);
        List<BibliographicEntity> bibliographicEntityList=new ArrayList<>();
        bibliographicEntityList.add(bibliographicEntity);
        Mockito.when(bibliographicEntity.getMatchingIdentity()).thenReturn("");
        Mockito.when(bibliographicDetailsRepository.findByIdIn(Mockito.anyList())).thenReturn(bibliographicEntityList);
        Optional<Set<Integer>> id= mockMatchingAlgorithmUtil.updateBibForMatchingIdentifier(bibIdList);
        assertNotNull(id);
    }




    @Test
    public void getReportDataEntity() throws Exception {
        List<MatchingAlgorithmReportDataEntity> reportDataEntities=new ArrayList<>();
        reportDataEntities.add(matchingAlgorithmReportDataEntity);
        String headerValues = new String(new char[10005]);
        mockMatchingAlgorithmUtil.getReportDataEntity("headerName",headerValues,reportDataEntities);
        mockMatchingAlgorithmUtil.getTitleToMatch("an an an an an");
    }

    @Test
    public void getBibIdAndBibEntityMap() throws Exception {
        Set<String> bibIdsList=new HashSet<>();
        bibIdsList.add("1");
        List<BibliographicEntity> bibliographicEntityList=new ArrayList<>();
        bibliographicEntityList.add(bibliographicEntity);
        Mockito.when(bibliographicEntity.getId()).thenReturn(1);
        Mockito.when(bibliographicDetailsRepository.findByIdIn(Mockito.anyList())).thenReturn(bibliographicEntityList);
        mockMatchingAlgorithmUtil.getBibIdAndBibEntityMap(bibIdsList);
    }

    @Test
    public void groupCGDForExistingEntries() throws Exception {
        List<Integer> bibIds=new ArrayList<>();
        for (int i = 1;i<=1001;i++)
        {
            bibIds.add(i);
        }
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        BibItem bibid = new BibItem();
        bibid.setMatchScore(1);
        bibItemMap.put(1,bibid);
        BibliographicEntityForMatching bibliographicEntity = new BibliographicEntityForMatching();
        //bibliographicEntity.setId(1);
        bibliographicEntity.setMatchScore(2);
        mockMatchingAlgorithmUtil.indexBibs(bibIds);
        mockMatchingAlgorithmUtil.removeMatchingIdsInDB();
        Map<Boolean,List<BibliographicEntityForMatching>> partionedByMatchingIdentity=new HashMap<>();
        List<BibliographicEntityForMatching> bibliographicEntities=new ArrayList<>();
        bibliographicEntity.setAnamolyFlag(false);
        bibliographicEntities.add(bibliographicEntity);
        bibliographicEntity.setBibliographicId(1);
        partionedByMatchingIdentity.put(false,bibliographicEntities);
        ReflectionTestUtils.invokeMethod(mockMatchingAlgorithmUtil,"groupCGDForExistingEntries",bibItemMap,partionedByMatchingIdentity,"matchingIdentity");
    }

    @Test
    public void extractBibIdsFromMatchScoreReports() throws Exception {
        List<MatchScoreReport> reportDataEntities=new ArrayList<>();
        Set<Integer> result=mockMatchingAlgorithmUtil.extractBibIdsFromMatchScoreReports(reportDataEntities);
        assertNotNull(result);
    }

    @Test
    public void getSingleMatchBibsAndSaveReport() throws Exception {
        Map<String, Set<Integer>> criteriaMap = getStringSetMap();
        Set<String> criteriaValueSet =new HashSet<>();
        String[] criteriaValueList={"1","2","3"};
        Map<Integer, MatchingBibEntity> bibEntityMap=getIntegerMatchingBibEntityMap();
        StringBuilder matchPointValue=new StringBuilder();
        String[] matchpoints={ScsbCommonConstants.MATCH_POINT_FIELD_OCLC,ScsbCommonConstants.MATCH_POINT_FIELD_ISSN,ScsbCommonConstants.MATCH_POINT_FIELD_LCCN,""};
        for (String re:matchpoints){
            Set<Integer> getBibIdsForCriteriaValue=mockMatchingAlgorithmUtil.getBibIdsForCriteriaValue(criteriaMap,criteriaValueSet,re,re,criteriaValueList,bibEntityMap,matchPointValue);
        }
        List<Integer> bibIds = Arrays.asList(4,5,6);
        List<MatchingBibEntity> matchingBibEntities = new ArrayList<>();
        matchingBibEntities.addAll(Arrays.asList(getMatchingBibEntity(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN,1,"PUL","Middleware for SCSB"),getMatchingBibEntity(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN,2,"CUL","Middleware for SCSB"),getMatchingBibEntity(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN,3,"NYPL","Middleware for SCSB")));
        Mockito.when(matchingBibDetailsRepository.getSingleMatchBibIdsBasedOnMatching(Mockito.anyString())).thenReturn(bibIds);
        Mockito.when(matchingBibDetailsRepository.getBibEntityBasedOnBibIds(Mockito.anyList())).thenReturn(matchingBibEntities);
        Map countsMap= mockMatchingAlgorithmUtil.getSingleMatchBibsAndSaveReport(1,ScsbCommonConstants.MATCH_POINT_FIELD_ISBN, getStringIntegerMap(),1);
        assertNotNull(countsMap);
    }

    @Test
    public void getMatchingMatchPointsEntity() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        SolrClient solrClient=PowerMockito.mock(SolrClient.class);
        ReflectionTestUtils.setField(mockMatchingAlgorithmUtil,"solrTemplate",mocksolrTemplate1);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        QueryResponse queryResponse= Mockito.mock(QueryResponse.class);

        List<FacetField> facetFields=new ArrayList<>();
        FacetField facetField=new FacetField(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN);
        facetField.add(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN,93930);
        FacetField facetField1=new FacetField(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN);
        facetField.add(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN,93931);
        facetFields.add(facetField);
        facetFields.add(facetField1);
        Mockito.when(queryResponse.getFacetFields()).thenReturn(facetFields);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        List<MatchingMatchPointsEntity> countsMap= mockMatchingAlgorithmUtil.getMatchingMatchPointsEntity(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN);
        assertNotNull(countsMap);
    }

    @Test
    public void updateMonographicSetRecords() {
        List<Integer> nonMonographRecordNums=Arrays.asList(1,2,3);
        List<MatchingAlgorithmReportDataEntity> reportDataEntitiesToUpdate=new ArrayList<>();
        MatchingAlgorithmReportDataEntity reportDataEntity = new MatchingAlgorithmReportDataEntity();
        reportDataEntity.setHeaderName("Test");
        reportDataEntity.setHeaderValue("Test");
        reportDataEntitiesToUpdate.add(reportDataEntity);
        List<MatchingMatchPointsEntity> matchingMatchPointsEntities=new ArrayList<>();
        MatchingMatchPointsEntity matchingMatchPointsEntity=new MatchingMatchPointsEntity();
        matchingMatchPointsEntity.setCriteriaValue("test");
        matchingMatchPointsEntities.add(matchingMatchPointsEntity);
        List<MatchingAlgorithmReportEntity> reportEntities=new ArrayList<>();
        MatchingAlgorithmReportEntity reportEntity=new MatchingAlgorithmReportEntity();
        reportEntities.add(reportEntity);
        Collection headerValues=new ArrayList();
        headerValues.add("test");
        ReflectionTestUtils.setField(mockMatchingAlgorithmUtil,"matchingHeaderValueLength",3);
        MatchingAlgorithmReportDataEntity reportDataEntityEmpty=mockMatchingAlgorithmUtil.getReportDataEntityForCollectionValues(Arrays.asList(""),"test");
        MatchingAlgorithmReportDataEntity reportDataEntity1=mockMatchingAlgorithmUtil.getReportDataEntityForCollectionValues(headerValues,"test");
        Mockito.when(matchingAlgorithmReportDetailRepository.findByIdIn(Mockito.anyList())).thenReturn(reportEntities);
        Mockito.when(matchingAlgorithmReportDataDetailsRepository.getReportDataEntityByRecordNumIn(Mockito.anyList(),Mockito.anyString())).thenReturn(reportDataEntitiesToUpdate);
        mockMatchingAlgorithmUtil.updateMonographicSetRecords(nonMonographRecordNums,1);
        mockMatchingAlgorithmUtil.saveMatchingMatchPointEntities(matchingMatchPointsEntities);
        mockMatchingAlgorithmUtil.updateExceptionRecords(Arrays.asList(1,2,3),1);
        assertNotNull(reportDataEntityEmpty);
        assertNotNull(reportDataEntity1);
    }

    @Test
    public void getbibIdAndBibMap() throws Exception {
        Set<Integer> bibIdsList=new HashSet<>();
        Map<Integer, BibliographicEntity> bibliographicEntityMap=mockMatchingAlgorithmUtil.getbibIdAndBibMap(bibIdsList);
        assertNotNull(bibliographicEntityMap);
    }

    @Test
    public void saveGroupedBibsToDb() throws Exception {
        mockMatchingAlgorithmUtil.saveGroupedBibsToDb(bibliographicEntities);
    }

    @Test
    public void saveGroupedBibsToDbForOngoing() throws Exception {
        Collection<BibliographicEntityForMatching> bibliographicEntities = new ArrayList<>();
        BibliographicEntityForMatching bibliographicEntityForMatching = new BibliographicEntityForMatching();
        bibliographicEntityForMatching.setBibliographicId(1);
        bibliographicEntityForMatching.setMatchingIdentity("test");
        bibliographicEntities.add(bibliographicEntityForMatching);
        mockMatchingAlgorithmUtil.saveGroupedBibsToDbForOngoing(bibliographicEntities);
    }


    @Test
    public void getBibIdsToRemoveMatchingIdsInSolr() throws Exception {
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(mockMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        SolrDocumentList solrDocumentList = getSolrDocuments();
        solrDocumentList.setNumFound(1);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        Mockito.when(solrQueryBuilder.solrQueryToFetchMatchedRecords()).thenReturn(solrQuery);
        Set<Integer> bibIdsList=mockMatchingAlgorithmUtil.getBibIdsToRemoveMatchingIdsInSolr();
        assertNotNull(bibIdsList);
    }

    @Test
    public void populateAndSaveReportEntity() throws Exception {
        List<Integer> bibIds = Arrays.asList(1,2,3);
        Set<Integer> bibIdSet = new HashSet<>();
        bibIdSet.addAll(bibIds);
        Map<Integer, MatchingBibEntity> matchingBibEntityMap = getIntegerMatchingBibEntityMap();
        Map countsMap= mockMatchingAlgorithmUtil.populateAndSaveReportEntity(bibIdSet,matchingBibEntityMap, ScsbCommonConstants.OCLC_CRITERIA, ScsbCommonConstants.MATCH_POINT_FIELD_ISBN,
                "2939384", "883939",getStringIntegerMap(),1);
        assertNotNull(countsMap);
    }

    @Test
    public void populateMatchingCounter() throws Exception {
        Mockito.when(solrQueryBuilder.buildSolrQueryForCGDReports(Mockito.anyString(),Mockito.anyString())).thenReturn(new SolrQuery());
        SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
        ReflectionTestUtils.setField(mockMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
        SolrClient solrClient = PowerMockito.mock(SolrClient.class);
        PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
        Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
        List<String> allInstitutionCodeExceptSupportInstitution=Arrays.asList(ScsbCommonConstants.COLUMBIA,ScsbCommonConstants.PRINCETON,ScsbCommonConstants.NYPL);
        Mockito.when(commonUtil.findAllInstitutionCodesExceptSupportInstitution()).thenReturn(allInstitutionCodeExceptSupportInstitution);
        SolrDocumentList solrDocumentList = getSolrDocuments();
        solrDocumentList.setNumFound(1);
        Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
        mockMatchingAlgorithmUtil.populateMatchingCounter();
        mockMatchingAlgorithmUtil.saveCGDUpdatedSummaryReport("test");
        assertNotNull(solrDocumentList);
    }

    @Test
    public void processPendingMatchingBibs() throws Exception {
        String[] matchpoints={ScsbCommonConstants.MATCH_POINT_FIELD_OCLC,ScsbCommonConstants.MATCH_POINT_FIELD_ISSN,ScsbCommonConstants.MATCH_POINT_FIELD_LCCN,ScsbCommonConstants.MATCH_POINT_FIELD_ISBN};
        for (String re:matchpoints) {
            SolrTemplate mocksolrTemplate1 = PowerMockito.mock(SolrTemplate.class);
            SolrClient solrClient = PowerMockito.mock(SolrClient.class);
            QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
            SolrDocumentList solrDocumentList = getSolrDocuments();
            List<MatchingBibEntity> bibEntities = new ArrayList<>();
            bibEntities.addAll(Arrays.asList(getMatchingBibEntity(re, 1, "PUL", "Middleware for 1"), getMatchingBibEntity(re, 2, "CUL", "Middleware for 2"), getMatchingBibEntity(re, 3, "NYPL", "Middleware for 3")));
            ReflectionTestUtils.setField(mockMatchingAlgorithmUtil, "solrTemplate", mocksolrTemplate1);
            PowerMockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
            Mockito.when(solrClient.query(any(SolrQuery.class))).thenReturn(queryResponse);
            Mockito.when(queryResponse.getResults()).thenReturn(solrDocumentList);
            Mockito.when(solrQueryBuilder.solrQueryForOngoingMatching(re, Arrays.asList("129393"))).thenReturn("test");
            Mockito.when(matchingBibDetailsRepository.findByMatchingAndBibIdIn(Mockito.anyString(), Mockito.anyList())).thenReturn(bibEntities);
            List<Integer> bibIds = Arrays.asList(4, 5, 6);
            Set<Integer> bibIdSet = new HashSet<>();
            bibIdSet.addAll(bibIds);
            List<MatchingBibEntity> matchingBibEntities = new ArrayList<>();
            matchingBibEntities.addAll(Arrays.asList(getMatchingBibEntity(re, 1, "PUL", "Middleware for SCSB"), getMatchingBibEntity(re, 2, "CUL", "Middleware for ReCAP"), getMatchingBibEntity(re, 3, "NYPL", "Middleware for ReCAP")));
            Map countsMap = mockMatchingAlgorithmUtil.processPendingMatchingBibs(matchingBibEntities, bibIdSet, getStringIntegerMap());
            assertNotNull(countsMap);
        }
    }

    private Map<String, Integer> getStringIntegerMap() {
        Map<String, Integer> matchingAlgoMap = new HashMap<>();
        matchingAlgoMap.put("PUL", 1);
        matchingAlgoMap.put("CUL", 2);
        matchingAlgoMap.put("NYPL", 3);
        return matchingAlgoMap;
    }

    private SolrDocumentList getSolrDocuments() {
        SolrDocumentList solrDocumentList =new SolrDocumentList();
        SolrDocument solrDocument = new SolrDocument();
        solrDocument.setField(ScsbCommonConstants.BIB_ID,Integer.valueOf(1));
        SolrDocument solrDocument1 = new SolrDocument();
        solrDocument1.setField(ScsbCommonConstants.BIB_ID,Integer.valueOf(2));
        SolrDocument solrDocument2 = new SolrDocument();
        solrDocument2.setField(ScsbCommonConstants.BIB_ID,Integer.valueOf(3));
        solrDocumentList.add(0,solrDocument);
        solrDocumentList.add(1,solrDocument1);
        solrDocumentList.add(2,solrDocument2);
        solrDocumentList.setNumFound(4);
        return solrDocumentList;
    }

    private Map<Integer, MatchingBibEntity> getIntegerMatchingBibEntityMap() {
        Map<Integer, MatchingBibEntity> matchingBibEntityMap = new HashMap<>();
        matchingBibEntityMap.put(1, getMatchingBibEntity(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN,1,"PUL","Middleware for SCSB"));
        matchingBibEntityMap.put(2, getMatchingBibEntity(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN,2,"CUL","Middleware for SCSB"));
        matchingBibEntityMap.put(3, getMatchingBibEntity(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN,3,"NYPL","Middleware for SCSB"));
        return matchingBibEntityMap;
    }

    private Map<String, Set<Integer>> getStringSetMap() {
        Set<Integer> criteria=new HashSet<>();
        criteria.add(1);
        criteria.add(2);
        criteria.add(3);
        Map<String, Set<Integer>> criteriaMap=new HashMap<>();
        criteriaMap.put("1",criteria);
        criteriaMap.put("2",criteria);
        criteriaMap.put("3",criteria);
        return criteriaMap;
    }

    private MatchingBibEntity getMatchingBibEntity(String matching,Integer bib,String inst,String title) {
        MatchingBibEntity matchingBibEntity = new MatchingBibEntity();
        matchingBibEntity.setMatching(matching);
        matchingBibEntity.setBibId(bib);
        matchingBibEntity.setId(10);
        matchingBibEntity.setOwningInstitution(inst);
        matchingBibEntity.setOwningInstBibId("N1029");
        matchingBibEntity.setTitle(title);
        matchingBibEntity.setOclc("129393");
        matchingBibEntity.setIsbn("93930");
        matchingBibEntity.setIssn("12283");
        matchingBibEntity.setLccn("039329");
        matchingBibEntity.setMaterialType("monograph");
        matchingBibEntity.setRoot("31");
        matchingBibEntity.setStatus(ScsbConstants.PENDING);
        return matchingBibEntity;
    }

    @Test
    public void groupBibsForInitialMatching() throws  Exception
    {
        List<BibliographicEntity> bibliographicEntityList = new ArrayList<>();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setId(6);
        bibliographicEntity.setMatchingIdentity("test");
        bibliographicEntity.setMatchScore(10);
        bibliographicEntityList.add(0,bibliographicEntity);
        Integer matchScore = 18;
        Set<String> matchingIdentities = new HashSet<>();
        matchingIdentities.add("test");
        Optional<Map<Integer,BibliographicEntity>> integerBibliographicEntityMap =  mockMatchingAlgorithmUtil.groupBibsForInitialMatching(bibliographicEntityList,matchScore);
        assertNotNull(integerBibliographicEntityMap);
    }

    @Test
    public void resetMAQualifier() throws Exception
    {
        List<Integer> bibIds =  new ArrayList<>();
        bibIds.add(15667);
        boolean isCGDProcess = true;
//       Mockito.when(bibliographicDetailsRepository.resetMAQualifier(any())).thenReturn(1);
//       Mockito.when(bibliographicDetailsRepository.resetMAQualifierForGrouping(bibIds)).thenReturn(1);
        mockMatchingAlgorithmUtil.resetMAQualifier(bibIds,isCGDProcess);
    }

    @Test
    public  void updateBibsForMatchingIdentifier() throws  Exception
    {
        Set<String> matchingIdentities  = new HashSet<>();
        matchingIdentities.add("title");
        List<BibliographicEntityForMatching> bibliographicEntityList = new ArrayList<>();
        BibliographicEntityForMatching bibliographicEntity = new BibliographicEntityForMatching();
        bibliographicEntity.setMatchScore(1);
        bibliographicEntity.setMatchingIdentity("PUL");
        bibliographicEntityList.add(0,bibliographicEntity);
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        BibItem bibItem = new BibItem();
        bibItem.setBibId(1);
        bibItem.setOwningInstitution("PUL");
        bibItem.setBarcode("123456");
        bibItemMap.put(1,bibItem);
        List<BibliographicEntityForMatching> newlyGroupedBibs=new ArrayList<>();
        List<BibliographicEntityForMatching> updatedWithExistingGroupedBibs=new ArrayList<>();
        List<BibliographicEntityForMatching> combinedBibs=new ArrayList<>();
        newlyGroupedBibs.add(0,bibliographicEntity);
        updatedWithExistingGroupedBibs.add(0,bibliographicEntity);
        combinedBibs.add(0,bibliographicEntity);
        try {
            Optional<Map<Integer, BibliographicEntityForMatching>> integerBibliographicEntityMap = mockMatchingAlgorithmUtil.updateBibsForMatchingIdentifier(bibliographicEntityList, bibItemMap);
            assertNotNull(integerBibliographicEntityMap);
        }catch (Exception e){}
    }

    @Test
    public void combineGroupedBibs() throws  Exception
    {
        List<Integer> integers =new ArrayList<>();
        integers.add(1);
        integers.add(2);
        integers.add(3);
        Set<String> matchingIdentities = new HashSet<>();
        matchingIdentities.add("test");
        List<BibliographicEntityForMatching> bibliographicEntityList = new ArrayList<>();
        BibliographicEntityForMatching bibliographicEntity = new BibliographicEntityForMatching();
        bibliographicEntity.setMatchScore(2);
        bibliographicEntity.setMatchingIdentity("PUL");
        bibliographicEntity.setAnamolyFlag(true);
        bibliographicEntity.setBibliographicId(1);
        bibliographicEntityList.add(0,bibliographicEntity);
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        BibItem bibItem = new BibItem();
        bibItem.setBibId(1);
        bibItem.setMatchScore(1);
        bibItem.setMaterialType("test");
        bibItemMap.put(1,bibItem);
        Mockito.when( bibliographicDetailsRepositoryForMatching.findByOwningInstitutionIdInAndMatchingIdentityIn(any(), any())).thenReturn(bibliographicEntityList);
        Mockito.when(commonUtil.findAllInstitutionIdsExceptSupportInstitution()).thenReturn(integers);
        ReflectionTestUtils.invokeMethod(mockMatchingAlgorithmUtil,"combineGroupedBibs",matchingIdentities,bibliographicEntityList,bibItemMap);
    }

    @Test
    public  void combineGroupedBibsForInitialMatching() throws  Exception
    {
        Set<String> matchingIdentities = new HashSet<>();
        matchingIdentities.add("test");
        matchingIdentities.add("sample");
        matchingIdentities.add("data");
        List<BibliographicEntity> bibliographicEntityList = new ArrayList<>();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setId(1);
        bibliographicEntity.setMatchScore(2);
        bibliographicEntity.setMatchingIdentity("PUL");
        bibliographicEntity.setAnamolyFlag(true);
        bibliographicEntityList.add(0,bibliographicEntity);
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        BibItem bibItem = new BibItem();
        bibItem.setBibId(1);
        bibItem.setMatchScore(2);
        bibItem.setMaterialType("test");
        bibItemMap.put(1,bibItem);
        Integer matchscore = 1;
        ReflectionTestUtils.invokeMethod(mockMatchingAlgorithmUtil,"combineGroupedBibsForInitialMatching",matchingIdentities,bibliographicEntityList,matchscore);
    }

    @Test
    public void initialMatchingroupBibsForNewEntries() throws  Exception
    {
        Integer matchScore = 1;
        String matchingIdentity = "PUL";
        Map<Boolean, List<BibliographicEntity>> partionedByMatchingIdentity = new HashMap<>();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setMatchScore(1);
        bibliographicEntity.setId(1);
        bibliographicEntity.setAnamolyFlag(true);
        bibliographicEntity.setMatchingIdentity("PUL");
        partionedByMatchingIdentity.put(false,Arrays.asList(bibliographicEntity));
        partionedByMatchingIdentity.put(true,Arrays.asList(bibliographicEntity));
        ReflectionTestUtils.invokeMethod(mockMatchingAlgorithmUtil,"initialMatchingroupBibsForNewEntries",matchScore,matchingIdentity,partionedByMatchingIdentity);
    }

    @Test
    public void groupCGDForNewEntries() throws  Exception
    {
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionCode("PUL");
        institutionEntity.setInstitutionCode("CUL");
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        BibItem bibItem = new BibItem();
        bibItem.setBibId(2);
        bibItem.setId("1");
        bibItem.setMatchScore(1);
        bibItemMap.put(1,bibItem);
        String matchingIdentity = "test";
        Map<Boolean, List<BibliographicEntityForMatching>> partionedByMatchingIdentity= new HashMap<>();
        List<BibliographicEntityForMatching> bibliographicEntities = new ArrayList<>();
        BibliographicEntityForMatching bibliographicEntity = new BibliographicEntityForMatching();
        //  bibliographicEntity.setId(1);
        bibliographicEntity.setMatchScore(1);
        bibliographicEntity.setMatchingIdentity("test");
        bibliographicEntity.setAnamolyFlag(true);
        //   bibliographicEntity.setInstitutionEntity(institutionEntity);
        bibliographicEntities.add(0,bibliographicEntity);
        partionedByMatchingIdentity.put(true,bibliographicEntities);
        ReflectionTestUtils.invokeMethod(mockMatchingAlgorithmUtil,"groupCGDForNewEntries",bibItemMap,matchingIdentity,partionedByMatchingIdentity);
    }

     @Test
    public void indexBibs() throws Exception
     {
         List<Integer> bibIds = new ArrayList<>();
         bibIds.add(999);
         mockMatchingAlgorithmUtil.indexBibs(bibIds);
     }

     @Test
    public void updateAnamolyFlagForBibs() throws Exception
     {
         List<Integer> bibIds = new ArrayList<>();
         bibIds.add(1);
         mockMatchingAlgorithmUtil.updateAnamolyFlagForBibs(bibIds);
     }

     @Test
    public void getMatchPointsCombinationMap() throws Exception
     {
         Map<String, Integer> matchPointsCombinationMap = new HashMap<>();
         matchPointsCombinationMap.put("OCLCNumber,ISBN",24);
         Map<String, Integer> points = mockMatchingAlgorithmUtil.getMatchPointsCombinationMap();
         assertNotNull(points);

     }

     @Test
    public void setTrimmedHeaderValue() throws Exception
     {
         String headerName = "test";
         MatchingAlgorithmReportDataEntity bibIdReportDataEntity = new MatchingAlgorithmReportDataEntity();
         bibIdReportDataEntity.setId(1);
         bibIdReportDataEntity.setHeaderName("test");
         bibIdReportDataEntity.setHeaderValue("testdata");
         String joinedHeaderValue = "test";
         ReflectionTestUtils.invokeMethod(mockMatchingAlgorithmUtil,"setTrimmedHeaderValue", headerName,bibIdReportDataEntity,joinedHeaderValue);

     }
     
     @Test
    public void setTrimmedHeaderValue1() throws Exception
     {
         String headerName = "test";
         String joinedHeaderValue = "Our expert bid writers will do the writing while you tell us about your business and your vision for the new contract. They know how to draw out the nuanced details which tell your story and present a unique strategy for the buyer.\n" +
                 "Exceptional bid writing means going beyond compliance, and our bid writers are here to challenge you to create something truly compelling with your bid. Our research will uncover the deeper issues surrounding the contract, and together we can design a solution to the needs of the buyer.\n" +
                 "We think deeply about our writing, and our whole organisation is built around using the written word to get the outcomes we desire. Every aspect of our bid writers work is deliberate ? from the initial ideas which underpin the story we?re telling, through to the structure and shape of the answers, and the tone of voice which defines each word choice.\n" +
                 "Your bid will go through multiple review stages, at each point getting objective critique from the wider business. This networked approach means you?re never engaging a tender writer ? every bid has the full force of our company behind it.\n" +
                 "Our expert bid writers will do the writing while you tell us about your business and your vision for the new contract. They know how to draw out the nuanced details which tell your story and present a unique strategy for the buyer.\n" +
                 "Exceptional bid writing means going beyond compliance, and our bid writers are here to challenge you to create something truly compelling with your bid. Our research will uncover the deeper issues surrounding the contract, and together we can design a solution to the needs of the buyer.\n" +
                 "We think deeply about our writing, and our whole organisation is built around using the written word to get the outcomes we desire. Every aspect of our bid writers work is deliberate ? from the initial ideas which underpin the story we?re telling, through to the structure and shape of the answers, and the tone of voice which defines each word choice.\n" +
                 "Your bid will go through multiple review stages, at each point getting objective critique from the wider business. This networked approach means you?re never engaging a tender writer ? every bid has the full force of our company behind it.\n" +
                 "Our expert bid writers will do the writing while you tell us about your business and your vision for the new contract. They know how to draw out the nuanced details which tell your story and present a unique strategy for the buyer.\n" +
                 "Exceptional bid writing means going beyond compliance, and our bid writers are here to challenge you to create something truly compelling with your bid. Our research will uncover the deeper issues surrounding the contract, and together we can design a solution to the needs of the buyer.\n" +
                 "We think deeply about our writing, and our whole organisation is built around using the written word to get the outcomes we desire. Every aspect of our bid writers work is deliberate ? from the initial ideas which underpin the story we?re telling, through to the structure and shape of the answers, and the tone of voice which defines each word choice.\n" +
                 "Your bid will go through multiple review stages, at each point getting objective critique from the wider business. This networked approach means you?re never engaging a tender writer ? every bid has the full force of our company behind it.\n" +
                 "Our expert bid writers will do the writing while you tell us about your business and your vision for the new contract. They know how to draw out the nuanced details which tell your story and present a unique strategy for the buyer.\n" +
                 "Exceptional bid writing means going beyond compliance, and our bid writers are here to challenge you to create something truly compelling with your bid. Our research will uncover the deeper issues surrounding the contract, and together we can design a solution to the needs of the buyer.\n" +
                 "We think deeply about our writing, and our whole organisation is built around using the written word to get the outcomes we desire. Every aspect of our bid writers work is deliberate ? from the initial ideas which underpin the story we?re telling, through to the structure and shape of the answers, and the tone of voice which defines each word choice.\n" +
                 "Your bid will go through multiple review stages, at each point getting objective critique from the wider business. This networked approach means you?re never engaging a tender writer ? every bid has the full force of our company behind it.\n" +
                 "Our expert bid writers will do the writing while you tell us about your business and your vision for the new contract. They know how to draw out the nuanced details which tell your story and present a unique strategy for the buyer.\n" +
                 "Exceptional bid writing means going beyond compliance, and our bid writers are here to challenge you to create something truly compelling with your bid. Our research will uncover the deeper issues surrounding the contract, and together we can design a solution to the needs of the buyer.\n" +
                 "We think deeply about our writing, and our whole organisation is built around using the written word to get the outcomes we desire. Every aspect of our bid writers work is deliberate ? from the initial ideas which underpin the story we?re telling, through to the structure and shape of the answers, and the tone of voice which defines each word choice.\n" +
                 "Your bid will go through multiple review stages, at each point getting objective critique from the wider business. This networked approach means you?re never engaging a tender writer ? every bid has the full force of our company behind it.\n" +
                 "Our expert bid writers will do the writing while you tell us about your business and your vision for the new contract. They know how to draw out the nuanced details which tell your story and present a unique strategy for the buyer.\n" +
                 "Exceptional bid writing means going beyond compliance, and our bid writers are here to challenge you to create something truly compelling with your bid. Our research will uncover the deeper issues surrounding the contract, and together we can design a solution to the needs of the buyer.\n" +
                 "We think deeply about our writing, and our whole organisation is built around using the written word to get the outcomes we desire. Every aspect of our bid writers work is deliberate ? from the initial ideas which underpin the story we?re telling, through to the structure and shape of the answers, and the tone of voice which defines each word choice.\n" +
                 "Your bid will go through multiple review stages, at each point getting objective critique from the wider business. This networked approach means you?re never engaging a tender writer ? every bid has the full force of our company behind it.\n" +
                 "Our expert bid writers will do the writing while you tell us about your business and your vision for the new contract. They know how to draw out the nuanced details which tell your story and present a unique strategy for the buyer.\n" +
                 "Exceptional bid writing means going beyond compliance, and our bid writers are here to challenge you to create something truly compelling with your bid. Our research will uncover the deeper issues surrounding the contract, and together we can design a solution to the needs of the buyer.\n" +
                 "We think deeply about our writing, and our whole organisation is built around using the written word to get the outcomes we desire. Every aspect of our bid writers work is deliberate ? from the initial ideas which underpin the story we?re telling, through to the structure and shape of the answers, and the tone of voice which defines each word choice.\n" +
                 "Your bid will go through multiple review stages, at each point getting objective critique from the wider business. This networked approach means you?re never engaging a tender writer ? every bid has the full force of our company behind it.\n" +
                 "Our expert bid writers will do the writing while you tell us about your business and your vision for the new contract. They know how to draw out the nuanced details which tell your story and present a unique strategy for the buyer.\n" +
                 "Exceptional bid writing means going beyond compliance, and our bid writers are here to challenge you to create something truly compelling with your bid. Our research will uncover the deeper issues surrounding the contract, and together we can design a solution to the needs of the buyer.\n" +
                 "We think deeply about our writing, and our whole organisation is built around using the written word to get the outcomes we desire. Every aspect of our bid writers work is deliberate ? from the initial ideas which underpin the story we?re telling, through to the structure and shape of the answers, and the tone of voice which defines each word choice.\n" +
                 "Your bid will go through multiple review stages, at each point getting objective critique from the wider business. This networked approach means you?re never engaging a tender writer ? every bid has the full force of our company behind it.\n" +
                 "Our expert bid writers will do the writing while you tell us about your business and your vision for the new contract. They know how to draw out the nuanced details which tell your story and present a unique strategy for the buyer.\n" +
                 "Exceptional bid writing means going beyond compliance, and our bid writers are here to challenge you to create something truly compelling with your bid. Our research will uncover the deeper issues surrounding the contract, and together we can design a solution to the needs of the buyer.\n" +
                 "We think deeply about our writing, and our whole organisation is built around using the written word to get the outcomes we desire. Every aspect of our bid writers work is deliberate ? from the initial ideas which underpin the story we?re telling, through to the structure and shape of the answers, and the tone of voice which defines each word choice.\n" +
                 "Your bid will go through multiple review stages, at each point getting objective critique from the wider business. This networked approach means you?re never engaging a tender writer ? every bid has the full force of our company behind it.";
         MatchingAlgorithmReportDataEntity bibIdReportDataEntity = new MatchingAlgorithmReportDataEntity();
         ReflectionTestUtils.invokeMethod(mockMatchingAlgorithmUtil,"setTrimmedHeaderValue", headerName,bibIdReportDataEntity,joinedHeaderValue);

     }
     @Test
    public void populateBibIdWithMatchingCriteriaValue() throws Exception
     {
         Map<String, Set<Integer>> criteria1Map = new HashMap<>();
         Set<Integer> integers = new HashSet<>();
         integers.add(1);
         criteria1Map.put("test",integers);
         List<MatchingBibEntity> matchingBibEntities = new ArrayList<>();
         MatchingBibEntity matchingBibEntity = new MatchingBibEntity();
         matchingBibEntity.setMatching("OCLC");
         matchingBibEntity.setBibId(1);
         matchingBibEntities.add(matchingBibEntity);
         String matchCriteria1 = "OCLC";
         Map<Integer, MatchingBibEntity> bibEntityMap = new HashMap<>();
         MatchingBibEntity matchingBibEntity1 = new MatchingBibEntity();
         matchingBibEntity1.setMatching("OlCL");
         matchingBibEntity1.setBibId(2);
         bibEntityMap.put(1,matchingBibEntity1);
         mockMatchingAlgorithmUtil.populateBibIdWithMatchingCriteriaValue(criteria1Map,matchingBibEntities,matchCriteria1,bibEntityMap);

     }
}
