package org.recap.executors;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.ScsbCommonConstants;
import org.recap.model.jpa.MatchingBibEntity;
import org.recap.repository.jpa.MatchingBibDetailsRepository;
import org.recap.util.MatchingAlgorithmUtil;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;

public class MatchingAlgorithmReportsCallableUT extends BaseTestCaseUT {

@InjectMocks
    MatchingAlgorithmReportsCallable matchingAlgorithmReportsCallable;

@Mock
MatchingBibDetailsRepository matchingBibDetailsRepository;

@Mock
MatchingAlgorithmUtil matchingAlgorithmUtil;

@Test
public  void MatchingAlgorithmReportsCallable() throws Exception
{
    Integer batchSize = 1;
    Map<String, Integer> institutionCounterMap = new HashMap<>();
    institutionCounterMap.put("1",1);
    institutionCounterMap.put("2",2);
    institutionCounterMap.put("3",3);
    MatchingAlgorithmUtil matchingAlgorithmUtil = new MatchingAlgorithmUtil();
    String matchPoint = "OCLCNumber,ISBN";
    Integer matchPointScore = 2;
    matchingAlgorithmReportsCallable = new MatchingAlgorithmReportsCallable(matchingBibDetailsRepository,batchSize,institutionCounterMap,matchingAlgorithmUtil,matchPoint,matchPointScore);
    matchingAlgorithmReportsCallable.call();

}
    @Test
    public void Matchingpoint() throws Exception {

        Integer batchSize = 1;
        Map<String, Integer> institutionCounterMap = new HashMap<>();
        institutionCounterMap.put("1",1);
        institutionCounterMap.put("2",2);
        institutionCounterMap.put("3",3);
        MatchingAlgorithmUtil matchingAlgorithmUtil = new MatchingAlgorithmUtil();
        String matchPoint = "OCLCNumber,ISSN";
        Integer matchPointScore = 2;
        matchingAlgorithmReportsCallable = new MatchingAlgorithmReportsCallable(matchingBibDetailsRepository,batchSize,institutionCounterMap,matchingAlgorithmUtil,matchPoint,matchPointScore);
        matchingAlgorithmReportsCallable.call();
    }

    @Test
    public void Matchingpoint2() throws Exception {

        Integer batchSize = 1;
        Map<String, Integer> institutionCounterMap = new HashMap<>();
        institutionCounterMap.put("1",1);
        institutionCounterMap.put("2",2);
        institutionCounterMap.put("3",3);
        MatchingAlgorithmUtil matchingAlgorithmUtil = new MatchingAlgorithmUtil();
        String matchPoint = "OCLCNumber,LCCN";
        Integer matchPointScore = 2;
        matchingAlgorithmReportsCallable = new MatchingAlgorithmReportsCallable(matchingBibDetailsRepository,batchSize,institutionCounterMap,matchingAlgorithmUtil,matchPoint,matchPointScore);
        matchingAlgorithmReportsCallable.call();
    }

    @Test
    public void Matchingpoint3() throws Exception {

        Integer batchSize = 1;
        Map<String, Integer> institutionCounterMap = new HashMap<>();
        institutionCounterMap.put("1",1);
        institutionCounterMap.put("2",2);
        institutionCounterMap.put("3",3);
        MatchingAlgorithmUtil matchingAlgorithmUtil = new MatchingAlgorithmUtil();
        String matchPoint = "ISBN,ISSN";
        Integer matchPointScore = 2;
        matchingAlgorithmReportsCallable = new MatchingAlgorithmReportsCallable(matchingBibDetailsRepository,batchSize,institutionCounterMap,matchingAlgorithmUtil,matchPoint,matchPointScore);
        matchingAlgorithmReportsCallable.call();
    }

    @Test
    public void Matchingpoint4() throws Exception {

        Integer batchSize = 1;
        Map<String, Integer> institutionCounterMap = new HashMap<>();
        institutionCounterMap.put("1",1);
        institutionCounterMap.put("2",2);
        institutionCounterMap.put("3",3);
        MatchingAlgorithmUtil matchingAlgorithmUtil = new MatchingAlgorithmUtil();
        String matchPoint = "ISBN,LCCN";
        Integer matchPointScore = 2;
        matchingAlgorithmReportsCallable = new MatchingAlgorithmReportsCallable(matchingBibDetailsRepository,batchSize,institutionCounterMap,matchingAlgorithmUtil,matchPoint,matchPointScore);
        matchingAlgorithmReportsCallable.call();
    }

    @Test
    public void Matchingpoint5() throws Exception {

        Integer batchSize = 1;
        Map<String, Integer> institutionCounterMap = new HashMap<>();
        institutionCounterMap.put("1",1);
        institutionCounterMap.put("2",2);
        institutionCounterMap.put("3",3);
        MatchingAlgorithmUtil matchingAlgorithmUtil = new MatchingAlgorithmUtil();
        String matchPoint = "ISSN,LCCN";
        Integer matchPointScore = 2;
        matchingAlgorithmReportsCallable = new MatchingAlgorithmReportsCallable(matchingBibDetailsRepository,batchSize,institutionCounterMap,matchingAlgorithmUtil,matchPoint,matchPointScore);
        matchingAlgorithmReportsCallable.call();
    }


    @Test
    public void Matchingpoint6() throws Exception {

        Integer batchSize = 1;
        Map<String, Integer> institutionCounterMap = new HashMap<>();
        institutionCounterMap.put("1",1);
        institutionCounterMap.put("2",2);
        institutionCounterMap.put("3",3);
        MatchingAlgorithmUtil matchingAlgorithmUtil = new MatchingAlgorithmUtil();
        String matchPoint = "OCLCNumber,Title_match";
        Integer matchPointScore = 2;
        matchingAlgorithmReportsCallable = new MatchingAlgorithmReportsCallable(matchingBibDetailsRepository,batchSize,institutionCounterMap,matchingAlgorithmUtil,matchPoint,matchPointScore);
        matchingAlgorithmReportsCallable.call();
    }

    @Test
    public void Matchingpoint7() throws Exception {

        Integer batchSize = 1;
        Map<String, Integer> institutionCounterMap = new HashMap<>();
        institutionCounterMap.put("1",1);
        institutionCounterMap.put("2",2);
        institutionCounterMap.put("3",3);
        MatchingAlgorithmUtil matchingAlgorithmUtil = new MatchingAlgorithmUtil();
        String matchPoint = "ISBN,Title_match";
        Integer matchPointScore = 2;
        matchingAlgorithmReportsCallable = new MatchingAlgorithmReportsCallable(matchingBibDetailsRepository,batchSize,institutionCounterMap,matchingAlgorithmUtil,matchPoint,matchPointScore);
        matchingAlgorithmReportsCallable.call();
    }


    @Test
    public void Matchingpoint8() throws Exception {

        Integer batchSize = 1;
        Map<String, Integer> institutionCounterMap = new HashMap<>();
        institutionCounterMap.put("1",1);
        institutionCounterMap.put("2",2);
        institutionCounterMap.put("3",3);
        MatchingAlgorithmUtil matchingAlgorithmUtil = new MatchingAlgorithmUtil();
        String matchPoint = "LCCN,Title_match";
        Integer matchPointScore = 2;
        matchingAlgorithmReportsCallable = new MatchingAlgorithmReportsCallable(matchingBibDetailsRepository,batchSize,institutionCounterMap,matchingAlgorithmUtil,matchPoint,matchPointScore);
        matchingAlgorithmReportsCallable.call();
    }

    @Test
    public void Matchingpoint9() throws Exception {

        Integer batchSize = 1;
        Map<String, Integer> institutionCounterMap = new HashMap<>();
        institutionCounterMap.put("1",1);
        institutionCounterMap.put("2",2);
        institutionCounterMap.put("3",3);
        MatchingAlgorithmUtil matchingAlgorithmUtil = new MatchingAlgorithmUtil();
        String matchPoint = "ISSN,Title_match";
        Integer matchPointScore = 2;
        matchingAlgorithmReportsCallable = new MatchingAlgorithmReportsCallable(matchingBibDetailsRepository,batchSize,institutionCounterMap,matchingAlgorithmUtil,matchPoint,matchPointScore);
        matchingAlgorithmReportsCallable.call();
    }

    @Test
    public void buildBibIdAndBibEntityMap() throws Exception
    {
        List<Integer> multiMatchBibIdsForMatchPoint1AndMatchPoint2 = new ArrayList<>();
        multiMatchBibIdsForMatchPoint1AndMatchPoint2.add(1);
        List<List<Integer>> multipleMatchBibIds = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();
        integers.add(0);
        integers.add(1);
        multipleMatchBibIds.add(0,integers);
        multipleMatchBibIds.add(1,integers);
        Map<String, Set<Integer>> matchPoint1AndBibIdMap = new HashMap<>();
        Set<Integer> integers1 = new HashSet<>();
        integers1.add(1);
        integers1.add(2);
        matchPoint1AndBibIdMap.put("1",integers1);
        List<MatchingBibEntity> bibEntitiesBasedOnBibIds = new ArrayList<>();
        Map<Integer, MatchingBibEntity> bibEntityMap =new HashMap<>();
        MatchingBibEntity matchingBibEntity = new MatchingBibEntity();
        matchingBibEntity.setStatus("test");
        matchingBibEntity.setMatching("test1");
        matchingBibEntity.setBibId(1);
        matchingBibEntity.setIsbn("123456");
        matchingBibEntity.setLccn("12345");
        matchingBibEntity.setIssn("123456");
        matchingBibEntity.setMaterialType("test");
        bibEntityMap.put(1,matchingBibEntity);
        bibEntitiesBasedOnBibIds.add(matchingBibEntity);
        String matchPoint1 = "ISSN";
        String matchPoint2 = "LCCN";
//        Mockito.when(matchingBibDetailsRepository.getMultiMatchBibIdsForOclcAndIsbn()).thenReturn(multiMatchBibIdsForMatchPoint1AndMatchPoint2);
        Mockito.when(matchingBibDetailsRepository.getMultiMatchBibEntitiesBasedOnBibIds(any(), any(), any())).thenReturn(bibEntitiesBasedOnBibIds);
      // Mockito.doCallRealMethod(matchingAlgorithmUtil.populateBibIdWithMatchingCriteriaValue(matchPoint1AndBibIdMap, bibEntitiesBasedOnBibIds, matchPoint1, bibEntityMap)));
     ReflectionTestUtils.invokeMethod(matchingAlgorithmReportsCallable,"buildBibIdAndBibEntityMap",multipleMatchBibIds,matchPoint1AndBibIdMap,bibEntityMap,matchPoint1,matchPoint2);

    }

//    @Ignore
//    @Test
//    public void Match() throws  Exception {
//
//        Map<String, Set<Integer>> matchPoint1AndBibIdMap = new HashMap<>();
//        Set<Integer> set = new HashSet<>();
//        set.add(1);
//        set.add(2);
//        matchPoint1AndBibIdMap.put("test", set);
//
//        Map<Integer, MatchingBibEntity> bibEntityMap = new HashMap<>();
//        MatchingBibEntity matchingBibEntity = new MatchingBibEntity();
//        matchingBibEntity.setTitle("test");
//        matchingBibEntity.setOclc("67336");
//        matchingBibEntity.setStatus("test");
//        matchingBibEntity.setMatching("ISBN");
//        matchingBibEntity.setId(1);
//        matchingBibEntity.setBibId(1);
//        matchingBibEntity.setIsbn("123456");
//        matchingBibEntity.setOwningInstBibId("1");
//        matchingBibEntity.setOwningInstitution("PUL");
//        matchingBibEntity.setLccn("12345");
//        matchingBibEntity.setIssn("1234");
//        matchingBibEntity.setMaterialType("Monograph");
//        matchingBibEntity.setRoot("test4");
//        bibEntityMap.put(1, matchingBibEntity);
//
//        List<MatchingBibEntity> bibEntitiesBasedOnBibIds = new ArrayList<>();
//        MatchingBibEntity matchingBibEntity1 = new MatchingBibEntity();
//        MatchingBibEntity matchingBibEntity2 = new MatchingBibEntity();
//        matchingBibEntity1.setTitle("test");
//        matchingBibEntity1.setOclc("67336");
//        matchingBibEntity1.setStatus("test");
//        matchingBibEntity1.setMatching("OCLCNumber");
//        matchingBibEntity1.setId(1);
//        matchingBibEntity1.setBibId(1);
//        matchingBibEntity1.setIsbn("123456");
//        matchingBibEntity1.setOwningInstBibId("1");
//        matchingBibEntity1.setOwningInstitution("PUL");
//        matchingBibEntity1.setLccn("12345");
//        matchingBibEntity1.setIssn("1234");
//        matchingBibEntity1.setMaterialType("Monograph");
//        matchingBibEntity1.setRoot("test4");
//
//
//        matchingBibEntity2.setTitle("Matched");
//        matchingBibEntity2.setStatus("test2");
//        matchingBibEntity2.setMatching("OCLCNumber");
//        matchingBibEntity2.setIsbn("123456");
//        matchingBibEntity2.setLccn("12345");
//        matchingBibEntity2.setIssn("1234");
//        matchingBibEntity2.setOclc("67336");
//        matchingBibEntity2.setOwningInstBibId("2");
//        matchingBibEntity2.setOwningInstitution("CUL");
//        matchingBibEntity2.setRoot("test5");
//        matchingBibEntity2.setBibId(2);
//        matchingBibEntity2.setId(2);
//        matchingBibEntity2.setMaterialType("Monograph");
//        bibEntitiesBasedOnBibIds.add(matchingBibEntity1);
//        bibEntitiesBasedOnBibIds.add(matchingBibEntity2);
//        Integer batchSize = 1;
//        Map<String, Integer> institutionCounterMap = new HashMap<>();
//        institutionCounterMap.put("1", 1);
//        institutionCounterMap.put("2", 2);
//        institutionCounterMap.put("3", 3);
//        MatchingAlgorithmUtil matchingAlgorithmUtil = new MatchingAlgorithmUtil();
//        String matchPoint = "OCLCNumber,ISBN";
//        String matchPoint1 = "OCLCNumber";
//        Integer matchPointScore = 2;
//
//        Mockito.when(matchingBibDetailsRepository.getMultiMatchBibIdsForOclcAndIsbn()).thenReturn(Arrays.asList(1, 2));
//    Mockito.when(matchingBibDetailsRepository.getMultiMatchBibIdsForOclcAndIssn()).thenReturn(Arrays.asList(1, 2));
//    Mockito.when(matchingBibDetailsRepository.getMultiMatchBibIdsForOclcAndLccn()).thenReturn(Arrays.asList(1, 2));
//    Mockito.when(matchingBibDetailsRepository.getMultiMatchBibIdsForIsbnAndIssn()).thenReturn(Arrays.asList(1, 2));
//    Mockito.when(matchingBibDetailsRepository.getMultiMatchBibIdsForIsbnAndLccn()).thenReturn(Arrays.asList(1, 2));
//    Mockito.when(matchingBibDetailsRepository.getMultiMatchBibIdsForIssnAndLccn()).thenReturn(Arrays.asList(1, 2));
//    Mockito.when(matchingBibDetailsRepository.getMultiMatchBibIdsForOclcAndTitle()).thenReturn(Arrays.asList(1, 2));
//    Mockito.when(matchingBibDetailsRepository.getMultiMatchBibIdsForIsbnAndTitle()).thenReturn(Arrays.asList(1, 2));
//    Mockito.when(matchingBibDetailsRepository.getMultiMatchBibIdsForIssnAndTitle()).thenReturn(Arrays.asList(1, 2));
//    Mockito.when(matchingBibDetailsRepository.getMultiMatchBibIdsForLccnAndTitle()).thenReturn(Arrays.asList(1, 2));
//    Mockito.when(matchingBibDetailsRepository.getMultiMatchBibEntitiesBasedOnBibIds(any(), any(), any())).thenReturn(bibEntitiesBasedOnBibIds);
//// Mockito.when(matchingAlgorithmUtil.getBibIdsForCriteriaValue(any(), any(), any(), any(), any(), any(), any())).thenReturn(set);
////          Mockito.when(matchingAlgorithmUtil.populateAndSaveReportEntity(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(institutionCounterMap);
//        matchingAlgorithmReportsCallable = new MatchingAlgorithmReportsCallable(matchingBibDetailsRepository, batchSize, institutionCounterMap, matchingAlgorithmUtil, matchPoint, matchPointScore);
//        matchingAlgorithmReportsCallable.call();
//    }
    }
