package org.recap.matchingalgorithm.service;

import com.google.common.collect.Lists;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.executors.BibItemIndexExecutorService;
import org.recap.executors.SaveMatchingBibsCallable;
import org.recap.matchingalgorithm.MatchScoreReport;
import org.recap.matchingalgorithm.MatchScoreUtil;
import org.recap.model.jpa.*;
import org.recap.model.solr.SolrIndexRequest;
import org.recap.repository.jpa.*;
import org.recap.service.ActiveMqQueuesInfo;
import org.recap.util.CommonUtil;
import org.recap.util.MatchingAlgorithmUtil;
import org.recap.util.SolrQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.recap.ScsbConstants.MATCHING_ALGORITHM_GROUPING_INDEX;


/**
 * Created by angelind on 11/7/16.
 */
@Service
public class MatchingAlgorithmHelperService {

    private static final Logger logger = LoggerFactory.getLogger(MatchingAlgorithmHelperService.class);

    @Autowired
    private MatchingBibDetailsRepository matchingBibDetailsRepository;

    @Autowired
    private MatchingMatchPointsDetailsRepository matchingMatchPointsDetailsRepository;

    @Autowired
    private MatchingAlgorithmUtil matchingAlgorithmUtil;

    @Autowired
    private SolrQueryBuilder solrQueryBuilder;

    @Resource(name = "recapSolrTemplate")
    private SolrTemplate solrTemplate;

    @Autowired
    private ProducerTemplate producerTemplate;

    private ExecutorService executorService;

    @Autowired
    private ActiveMqQueuesInfo activeMqQueuesInfo;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    MatchingAlgorithmReportDataDetailsRepository matchingAlgorithmReportDataDetailsRepository;

    @Value("${" + PropertyKeyConstants.IS_INDEX_GROUPING_MATCHES + "}")
    Boolean isIndexGrouping;

    @Autowired
    private BibItemIndexExecutorService bibItemIndexExecutorService;

    /**
     * Gets logger.
     *
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Gets matching bib details repository.
     *
     * @return the matching bib details repository
     */
    public MatchingBibDetailsRepository getMatchingBibDetailsRepository() {
        return matchingBibDetailsRepository;
    }

    /**
     * Gets matching match points details repository.
     *
     * @return the matching match points details repository
     */
    public MatchingMatchPointsDetailsRepository getMatchingMatchPointsDetailsRepository() {
        return matchingMatchPointsDetailsRepository;
    }

    /**
     * Gets matching algorithm util.
     *
     * @return the matching algorithm util
     */
    public MatchingAlgorithmUtil getMatchingAlgorithmUtil() {
        return matchingAlgorithmUtil;
    }

    /**
     * Gets solr query builder.
     *
     * @return the solr query builder
     */
    public SolrQueryBuilder getSolrQueryBuilder() {
        return solrQueryBuilder;
    }

    /**
     * Gets solr template.
     *
     * @return the solr template
     */
    public SolrTemplate getSolrTemplate() {
        return solrTemplate;
    }

    /**
     * Gets producer template.
     *
     * @return the producer template
     */
    public ProducerTemplate getProducerTemplate() {
        return producerTemplate;
    }

    public ActiveMqQueuesInfo getActiveMqQueuesInfo() {
        return activeMqQueuesInfo;
    }

    /**
     * This method finds the matching records based on the match point field(OCLC,ISBN,ISSN,LCCN).
     *
     * @return the long
     * @throws Exception the exception
     */
    public long findMatchingAndPopulateMatchPointsEntities()  {
        List<String> matchingMatchPoints = ScsbConstants.MATCHING_MATCH_POINTS;
        long count = matchingMatchPoints
                .stream()
                .mapToLong(this::loadAndSaveMatchingMatchPointEntities)
                .sum();
        logger.info("Total count in MatchPoints : {} " , count);
        drainAllQueueMsgs("saveMatchingMatchPointsQ");

        return count;
    }

    private long loadAndSaveMatchingMatchPointEntities(String matchPointFieldOclc) {
        List<MatchingMatchPointsEntity> matchingMatchPointsEntities = null;
        try {
            matchingMatchPointsEntities = getMatchingAlgorithmUtil().getMatchingMatchPointsEntity(matchPointFieldOclc);
            getMatchingAlgorithmUtil().saveMatchingMatchPointEntities(matchingMatchPointsEntities);
        } catch (Exception exception) {
            logger.info("Exception in finding MatchPoints : {}",exception.getMessage());
        }
        return matchingMatchPointsEntities.size();
    }

    /**
     * This method is used to populate matching bib records in the database.
     *
     * @return the long
     * @throws IOException         the io exception
     * @throws SolrServerException the solr server exception
     */
    public long populateMatchingBibEntities() {
        long count = ScsbConstants.MATCHING_MATCH_POINTS.stream().mapToLong(this::fetchAndSaveMatchingBibs).sum();
        drainAllQueueMsgs("saveMatchingBibsQ");
        return count;
    }

    private void drainAllQueueMsgs(String queueName) {
        Integer saveMatchingBibsQ = getActiveMqQueuesInfo().getActivemqQueuesInfo(queueName);
        if(saveMatchingBibsQ != null) {
            while (saveMatchingBibsQ != 0) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                   logger.error(ScsbConstants.ERROR,e);
                }
                saveMatchingBibsQ = getActiveMqQueuesInfo().getActivemqQueuesInfo(queueName);
            }
        }
    }

    public Map<String, Integer> populateReportsForMatchPoints(Integer batchSize, String matchPoint1, String matchPoint2, Map<String, Integer> institutionCounterMap, Integer matchScore) {

        List<Integer> multiMatchBibIdsForMatchPoint1AndMatchPoint2 = null;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_OCLC) && matchPoint2.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN)) {
            multiMatchBibIdsForMatchPoint1AndMatchPoint2 = getMatchingBibDetailsRepository().getMultiMatchBibIdsForOclcAndIsbn();
        } else if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_OCLC) && matchPoint2.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISSN)) {
            multiMatchBibIdsForMatchPoint1AndMatchPoint2 = getMatchingBibDetailsRepository().getMultiMatchBibIdsForOclcAndIssn();
        } else if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_OCLC) && matchPoint2.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_LCCN)) {
            multiMatchBibIdsForMatchPoint1AndMatchPoint2 = getMatchingBibDetailsRepository().getMultiMatchBibIdsForOclcAndLccn();
        } else if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN) && matchPoint2.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISSN)) {
            multiMatchBibIdsForMatchPoint1AndMatchPoint2 = getMatchingBibDetailsRepository().getMultiMatchBibIdsForIsbnAndIssn();
        } else if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN) && matchPoint2.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_LCCN)) {
            multiMatchBibIdsForMatchPoint1AndMatchPoint2 = getMatchingBibDetailsRepository().getMultiMatchBibIdsForIsbnAndLccn();
        } else if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISSN) && matchPoint2.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_LCCN)) {
            multiMatchBibIdsForMatchPoint1AndMatchPoint2 = getMatchingBibDetailsRepository().getMultiMatchBibIdsForIssnAndLccn();
        }
        List<List<Integer>> multipleMatchBibIds = Lists.partition(multiMatchBibIdsForMatchPoint1AndMatchPoint2, batchSize);
        Map<String, Set<Integer>> matchPoint1AndBibIdMap = new HashMap<>();
        Map<Integer, MatchingBibEntity> bibEntityMap = new HashMap<>();
        buildBibIdAndBibEntityMap(multipleMatchBibIds, matchPoint1AndBibIdMap, bibEntityMap, getLogger(), matchPoint1, matchPoint2);
        Set<String> matchPoint1Set = new HashSet<>();
        for (Iterator<String> iterator = matchPoint1AndBibIdMap.keySet().iterator(); iterator.hasNext(); ) {
            String matchPoint = iterator.next();
            if (!matchPoint1Set.contains(matchPoint)) {
                StringBuilder matchPoints1 = new StringBuilder();
                StringBuilder matchPoints2 = new StringBuilder();
                matchPoint1Set.add(matchPoint);
                Set<Integer> bibIds = matchPoint1AndBibIdMap.get(matchPoint);
                Set<Integer> tempBibIds = new HashSet<>(bibIds);
                for (Integer bibId : bibIds) {
                    MatchingBibEntity matchingBibEntity = bibEntityMap.get(bibId);
                    if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_OCLC) && matchPoint2.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN)) {
                        matchPoints1.append(StringUtils.isNotBlank(matchPoints1.toString()) ? "," : "").append(matchingBibEntity.getOclc());
                        matchPoints2.append(StringUtils.isNotBlank(matchPoints2.toString()) ? "," : "").append(matchingBibEntity.getIsbn());
                    } else if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_OCLC) && matchPoint2.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISSN)) {
                        matchPoints1.append(StringUtils.isNotBlank(matchPoints1.toString()) ? "," : "").append(matchingBibEntity.getOclc());
                        matchPoints2.append(StringUtils.isNotBlank(matchPoints2.toString()) ? "," : "").append(matchingBibEntity.getIssn());
                    } else if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_OCLC) && matchPoint2.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_LCCN)) {
                        matchPoints1.append(StringUtils.isNotBlank(matchPoints1.toString()) ? "," : "").append(matchingBibEntity.getOclc());
                        matchPoints2.append(StringUtils.isNotBlank(matchPoints2.toString()) ? "," : "").append(matchingBibEntity.getLccn());
                    } else if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN) && matchPoint2.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISSN)) {
                        matchPoints1.append(StringUtils.isNotBlank(matchPoints1.toString()) ? "," : "").append(matchingBibEntity.getIsbn());
                        matchPoints2.append(StringUtils.isNotBlank(matchPoints2.toString()) ? "," : "").append(matchingBibEntity.getIssn());
                    } else if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN) && matchPoint2.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_LCCN)) {
                        matchPoints1.append(StringUtils.isNotBlank(matchPoints2.toString()) ? "," : "").append(matchingBibEntity.getIsbn());
                        matchPoints2.append(StringUtils.isNotBlank(matchPoints2.toString()) ? "," : "").append(matchingBibEntity.getLccn());
                    } else if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISSN) && matchPoint2.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_LCCN)) {
                        matchPoints1.append(StringUtils.isNotBlank(matchPoints2.toString()) ? "," : "").append(matchingBibEntity.getIssn());
                        matchPoints2.append(StringUtils.isNotBlank(matchPoints2.toString()) ? "," : "").append(matchingBibEntity.getLccn());
                    }
                    String[] matchPoint1List = matchPoints1.toString().split(",");
                    tempBibIds.addAll(getMatchingAlgorithmUtil().getBibIdsForCriteriaValue(matchPoint1AndBibIdMap, matchPoint1Set, matchPoint, matchPoint1, matchPoint1List, bibEntityMap, matchPoints1));
                }
                if (matchPoint1.equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_OCLC)) {
                    getMatchingAlgorithmUtil().populateAndSaveReportEntity(tempBibIds, bibEntityMap, ScsbCommonConstants.OCLC_CRITERIA, matchPoint2, matchPoints1.toString(), matchPoints2.toString(), institutionCounterMap,matchScore);
                } else {
                    getMatchingAlgorithmUtil().populateAndSaveReportEntity(tempBibIds, bibEntityMap, matchPoint1, matchPoint2, matchPoints1.toString(), matchPoints2.toString(), institutionCounterMap,matchScore);
                }
            }
        }
        stopWatch.stop();
        getLogger().info("Time taken to save - {} and {} Combination Reports : {}" , matchPoint1, matchPoint2, stopWatch.getTotalTimeSeconds());
        return institutionCounterMap;
    }

    /**
     * This method is used to populate reports for single match.
     *
     * @param batchSize the batch size
     * @param institutionCounterMap
     * @return the map
     */
    public Map<String,Integer> populateReportsForSingleMatch(Integer batchSize, Map<String, Integer> institutionCounterMap) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        getMatchingAlgorithmUtil().getSingleMatchBibsAndSaveReport(batchSize, ScsbCommonConstants.MATCH_POINT_FIELD_OCLC,institutionCounterMap, MatchScoreUtil.OCLC_SCORE);
        getMatchingAlgorithmUtil().getSingleMatchBibsAndSaveReport(batchSize, ScsbCommonConstants.MATCH_POINT_FIELD_ISBN, institutionCounterMap,MatchScoreUtil.ISBN_SCORE);
        getMatchingAlgorithmUtil().getSingleMatchBibsAndSaveReport(batchSize, ScsbCommonConstants.MATCH_POINT_FIELD_ISSN, institutionCounterMap,MatchScoreUtil.ISSN_SCORE);
        getMatchingAlgorithmUtil().getSingleMatchBibsAndSaveReport(batchSize, ScsbCommonConstants.MATCH_POINT_FIELD_LCCN, institutionCounterMap,MatchScoreUtil.LCCN_SCORE);
        Integer saveMatchingBibsQ = getActiveMqQueuesInfo().getActivemqQueuesInfo("updateMatchingBibEntityQ");
        if(saveMatchingBibsQ != null) {
            while (saveMatchingBibsQ != 0) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    logger.error(ScsbConstants.ERROR,e);
                }
                saveMatchingBibsQ = getActiveMqQueuesInfo().getActivemqQueuesInfo("updateMatchingBibEntityQ");
            }
        }
        populateReportsForPendingMatches(batchSize,institutionCounterMap);
        stopWatch.stop();
        getLogger().info("Time taken to save Single Matching Reports : {}" , stopWatch.getTotalTimeSeconds());
        return institutionCounterMap;
    }

    /**
     * Populate reports for pending matches map.
     *
     * @param batchSize the batch size
     * @param institutionCounterMap
     * @return the map
     */
    public Map<String,Integer> populateReportsForPendingMatches(Integer batchSize, Map<String, Integer> institutionCounterMap) {

        Page<MatchingBibEntity> matchingBibEntities = getMatchingBibDetailsRepository().findByStatus(PageRequest.of(0, batchSize), ScsbConstants.PENDING);
        int totalPages = matchingBibEntities.getTotalPages();
        List<MatchingBibEntity> matchingBibEntityList = matchingBibEntities.getContent();
        Set<Integer> matchingBibIds = new HashSet<>();
        getMatchingAlgorithmUtil().processPendingMatchingBibs(matchingBibEntityList, matchingBibIds,institutionCounterMap);
        for(int pageNum=1; pageNum < totalPages; pageNum++) {
            matchingBibEntities = getMatchingBibDetailsRepository().findByStatus(PageRequest.of(pageNum, batchSize), ScsbConstants.PENDING);
            matchingBibEntityList = matchingBibEntities.getContent();
            getMatchingAlgorithmUtil().processPendingMatchingBibs(matchingBibEntityList, matchingBibIds, institutionCounterMap);
        }

        getMatchingBibDetailsRepository().updateStatus(ScsbCommonConstants.COMPLETE_STATUS, ScsbConstants.PENDING);
        return institutionCounterMap;
    }

    /**
     * This method is used to save matching summary count.
     *
     * @param institutionCounterMap  institutionsMatchingCount
     */
    public void saveMatchingSummaryCount(Map<String, Integer> institutionCounterMap) {
        MatchingAlgorithmReportEntity reportEntity = new MatchingAlgorithmReportEntity();
        reportEntity.setType("MatchingCount");
        reportEntity.setCreatedDate(new Date());
        reportEntity.setFileName("MatchingSummaryCount");
        reportEntity.setInstitutionName("ALL");
        List<MatchingAlgorithmReportDataEntity> reportDataEntities = new ArrayList<>();


        List<String> allInstitutionCodesExceptSupportInstitution = commonUtil.findAllInstitutionCodesExceptSupportInstitution();
        for (String institution : allInstitutionCodesExceptSupportInstitution) {
            MatchingAlgorithmReportDataEntity reportDataEntity = new MatchingAlgorithmReportDataEntity();
            reportDataEntity.setHeaderName(institution.toLowerCase()+"MatchingCount");
            reportDataEntity.setHeaderValue(String.valueOf(institutionCounterMap.get(institution)));
            reportDataEntities.add(reportDataEntity);
        }
        reportEntity.addAll(reportDataEntities);
        getProducerTemplate().sendBody("scsbactivemq:queue:saveMatchingReportsQ", Collections.singletonList(reportEntity));
    }

    /**
     * This method is used to fetch and save matching bibs.
     *
     * @param matchCriteria the match criteria
     * @return the integer
     */
    public Integer fetchAndSaveMatchingBibs(String matchCriteria) {
        long batchSize = 300;
        Integer size = 0;
        try {
            long countBasedOnCriteria = getMatchingMatchPointsDetailsRepository().countBasedOnCriteria(matchCriteria);
            SaveMatchingBibsCallable saveMatchingBibsCallable = new SaveMatchingBibsCallable();
            saveMatchingBibsCallable.setBibIdList(new HashSet<>());
            int totalPagesCount = (int) (countBasedOnCriteria / batchSize);
            ExecutorService executor = getExecutorService(50);
            List<Callable<Integer>> callables = new ArrayList<>();
            for (int pageNum = 0; pageNum < totalPagesCount + 1; pageNum++) {
                Callable callable = new SaveMatchingBibsCallable(getMatchingMatchPointsDetailsRepository(), matchCriteria, getSolrTemplate(),
                        getProducerTemplate(), getSolrQueryBuilder(), batchSize, pageNum, getMatchingAlgorithmUtil());
                callables.add(callable);
            }
            size = executeCallables(size, executor, callables);
        }
        catch (Exception exception){
            logger.info("Exception caught in saving Matching Bibs : {}",exception.getMessage());
        }
        return size;
    }

    public List<Integer> getBibIdListFromString(MatchingAlgorithmReportDataEntity reportDataEntity) {
        List<Integer> bibIdList = new ArrayList<>();
        if(reportDataEntity.getHeaderName().equals(ScsbConstants.BIB_ID)) {
            String bibId = reportDataEntity.getHeaderValue();
            String[] bibIds = bibId.split(",");
            for (int i = 0; i < bibIds.length; i++) {
                bibIdList.add(Integer.valueOf(bibIds[i]));
            }
        }
        return bibIdList;
    }

    private Integer executeCallables(Integer size, ExecutorService executorService, List<Callable<Integer>> callables) {
        List<Future<Integer>> futures = null;
        try {
            futures = getFutures(executorService, callables);
        } catch (Exception e) {
            logger.error(ScsbCommonConstants.LOG_ERROR,e);
        }

        if(futures != null) {
            for (Iterator<Future<Integer>> iterator = futures.iterator(); iterator.hasNext(); ) {
                Future future = iterator.next();
                try {
                    size += (Integer) future.get();
                } catch (InterruptedException e) {
                    logger.error(ScsbCommonConstants.LOG_ERROR,e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    logger.error(ScsbCommonConstants.LOG_ERROR,e);
                }
            }
        }
        return size;
    }

    private List<Future<Integer>> getFutures(ExecutorService executorService, List<Callable<Integer>> callables) throws InterruptedException {
        List<Future<Integer>> futures = executorService.invokeAll(callables);
        List<Future<Integer>> collectedFutures = futures.stream().map(future -> {
            try {
                future.get();
                return future;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }).collect(toList());
        logger.info("No of Futures Collected : {}", collectedFutures.size());
        return collectedFutures;
    }

    private ExecutorService getExecutorService(Integer numThreads) {
        if (null == executorService || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(numThreads);
        }
        return executorService;
    }

    private void populateBibIds( Map<String, Set<Integer>> isbnAndBibIdMap ,  Map<Integer, MatchingBibEntity> bibEntityMap, List<List<Integer>> multipleMatchBibIds, String matchPoint) {
        buildBibIdAndBibEntityMap(multipleMatchBibIds, isbnAndBibIdMap, bibEntityMap, logger, ScsbCommonConstants.MATCH_POINT_FIELD_ISBN, matchPoint);
    }

    private void buildBibIdAndBibEntityMap(List<List<Integer>> multipleMatchBibIds, Map<String, Set<Integer>> matchPoint1AndBibIdMap, Map<Integer, MatchingBibEntity> bibEntityMap, Logger logger, String matchPoint1, String matchPoint2) {
        logger.info(ScsbConstants.TOTAL_BIB_ID_PARTITION_LIST, multipleMatchBibIds.size());
        for (List<Integer> bibIds : multipleMatchBibIds) {
            List<MatchingBibEntity> bibEntitiesBasedOnBibIds = getMatchingBibDetailsRepository().getMultiMatchBibEntitiesBasedOnBibIds(bibIds, matchPoint1, matchPoint2);
            if (CollectionUtils.isNotEmpty(bibEntitiesBasedOnBibIds)) {
                getMatchingAlgorithmUtil().populateBibIdWithMatchingCriteriaValue(matchPoint1AndBibIdMap, bibEntitiesBasedOnBibIds, matchPoint1, bibEntityMap);
            }
        }
    }

    public void groupBibsForMonograph(Integer batchSize, Boolean isPendingMatch) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int totalPagesCount = getTotalPagesCountForMonographs(batchSize, isPendingMatch);
        logger.info("Starting to group Monograph bibs for {} where total page count is {}",isPendingMatch,totalPagesCount);
        for (int pageNum = 0; pageNum < totalPagesCount + 1; pageNum++) {
            long from = pageNum * Long.valueOf(batchSize);
            logger.info("Quering report data query for Monograph where Total Page count : {} and current page number is : {} from is : {} and to is : {}",totalPagesCount, pageNum, from, batchSize);
            StopWatch stopWatchForFetchingReport = new StopWatch();
            stopWatchForFetchingReport.start();
            Optional<List<MatchingAlgorithmReportDataEntity>> reportDataEntities = getMonographDataEntitiesFromDB(batchSize, isPendingMatch, from);
            stopWatchForFetchingReport.stop();
            logger.info("Total time taken for fetching reports {} for size {}",stopWatchForFetchingReport.getTotalTimeSeconds(),reportDataEntities.get().size());
            reportDataEntities.ifPresent(this::groupBibsAndAssignMatchScore);
        }
        stopWatch.stop();
        logger.info("Completed to group Monograph bibs for {} ",isPendingMatch);
        logger.info(ScsbConstants.TOTAL_TIME_TAKEN + " to group Monograph bibs : " + stopWatch.getTotalTimeSeconds());
    }

    public void groupBibsForMVMs(Integer batchSize) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int totalPagesCount = getTotalPagesCountForMVMs(batchSize);
        logger.info("Starting to group MVM bibs where total page count is {}",totalPagesCount);
        for (int pageNum = 0; pageNum < totalPagesCount + 1; pageNum++) {
            long from = pageNum * Long.valueOf(batchSize);
            logger.info("Quering report data query for MVMs where Total Page count : {} and current page number is : {} from is : {} and to is : {}",totalPagesCount, pageNum, from, batchSize);
            Optional<List<MatchingAlgorithmReportDataEntity>> reportDataEntities = getMVMReportDataEntitiesFromDB(batchSize, from);
            reportDataEntities.ifPresent(this::groupBibsAndAssignMatchScore);
        }
        stopWatch.stop();
        logger.info("Completed to group MVM bibs");
        logger.info(ScsbConstants.TOTAL_TIME_TAKEN + " to group MVM bibs : " + stopWatch.getTotalTimeSeconds());
    }

    public void groupForSerialBibs(Integer batchSize) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int totalPagesCount = getTotalPagesCountForSerialBibs(batchSize);
        logger.info("Starting to group Serial bibs where total page count is {}",totalPagesCount);
        for (int pageNum = 0; pageNum < totalPagesCount + 1; pageNum++) {
            long from = pageNum * Long.valueOf(batchSize);
            logger.info("Quering report data query for Serials where Total Page count : {} and current page number is : {} from is : {} and to is : {}",totalPagesCount, pageNum, from, batchSize);
            Optional<List<MatchingAlgorithmReportDataEntity>> reportDataEntities = getSerialReportDataEntitiesFromDB(batchSize, from);
            reportDataEntities.ifPresent(this::groupBibsAndAssignMatchScore);
        }
        stopWatch.stop();
        logger.info("Completed to group Serial bibs");
        logger.info(ScsbConstants.TOTAL_TIME_TAKEN + " to group Serial bibs : " + stopWatch.getTotalTimeSeconds());
    }

    private int getTotalPagesCountForMVMs(Integer batchSize) {
        long countOfRecordNum = matchingAlgorithmReportDataDetailsRepository.getCountOfRecordNumForMatchingMVMs(ScsbCommonConstants.BIB_ID);
        logger.info(ScsbConstants.TOTAL_RECORDS + "{}", countOfRecordNum);
        int totalPagesCount = (int) ((countOfRecordNum*2) / batchSize);
        logger.info(ScsbConstants.TOTAL_PAGES + "{}" , totalPagesCount/2);
        return totalPagesCount;
    }

    private int getTotalPagesCountForSerialBibs(Integer batchSize) {
        long countOfRecordNum = matchingAlgorithmReportDataDetailsRepository.getCountOfRecordNumForMatchingSerials(ScsbCommonConstants.BIB_ID);
        logger.info(ScsbConstants.TOTAL_RECORDS + "{}", countOfRecordNum);
        int totalPagesCount = (int) ((countOfRecordNum*2) / batchSize);
        logger.info(ScsbConstants.TOTAL_PAGES + "{}" , totalPagesCount/2);
        return totalPagesCount;
    }

    private Optional<List<MatchingAlgorithmReportDataEntity>> getMVMReportDataEntitiesFromDB(Integer batchSize, long from) {
        List<MatchingAlgorithmReportDataEntity> reportDataEntityForMatchingMVMs = matchingAlgorithmReportDataDetailsRepository.getReportDataEntityForMatchingMVMs(Arrays.asList(ScsbCommonConstants.BIB_ID, ScsbConstants.MATCH_SCORE), from, batchSize);
        return Optional.ofNullable(CollectionUtils.isNotEmpty(reportDataEntityForMatchingMVMs)?reportDataEntityForMatchingMVMs:null);
    }

    private Optional<List<MatchingAlgorithmReportDataEntity>> getSerialReportDataEntitiesFromDB(Integer batchSize, long from) {
        List<MatchingAlgorithmReportDataEntity> reportDataEntityForMatchingSerials = matchingAlgorithmReportDataDetailsRepository.getReportDataEntityForMatchingSerials(Arrays.asList(ScsbCommonConstants.BIB_ID, ScsbConstants.MATCH_SCORE), from, batchSize);
        return Optional.ofNullable(CollectionUtils.isNotEmpty(reportDataEntityForMatchingSerials)?reportDataEntityForMatchingSerials:null);
    }

    private void clearAllCollection(List<MatchingAlgorithmReportDataEntity> dataEntities, Map<Integer, BibliographicEntity> bibIdAndBibEntityMap, Set<Integer> bibIdsToIndex) {
        bibIdsToIndex.clear();
        bibIdAndBibEntityMap.clear();
        dataEntities.clear();
    }

    private void saveAndIndexGroupedBibs(Map<Integer, BibliographicEntity> bibIdAndBibEntityMap, Set<Integer> bibIdsToIndex) {
        logger.info("Total BibIds grouped to index : {}", bibIdsToIndex.size());
        matchingAlgorithmUtil.saveGroupedBibsToDb(bibIdAndBibEntityMap.values());
        if(isIndexGrouping){
            producerTemplate.sendBody(MATCHING_ALGORITHM_GROUPING_INDEX, bibIdsToIndex);
        }
    }

    private Map<Integer, BibliographicEntity> getBibIdAndBibliographicEntityMap(List<MatchScoreReport> matchScoreReportList) {
        Set<Integer> bibIdsList = matchingAlgorithmUtil.extractBibIdsFromMatchScoreReports(matchScoreReportList);
        Map<Integer, BibliographicEntity> bibIdAndBibEntityMap = matchingAlgorithmUtil.getbibIdAndBibMap(bibIdsList);
        return bibIdAndBibEntityMap;
    }

    private List<MatchScoreReport> prepareMatchScoreReportList(Map<String, List<MatchingAlgorithmReportDataEntity>> reportDatasGroupedByRecordNum) {
        List<MatchScoreReport> matchScoreReports=new ArrayList<>();
        reportDatasGroupedByRecordNum.values().forEach(value -> {
            MatchScoreReport matchScoreReport = new MatchScoreReport();
            value.forEach(reportDataEntity -> {
                if (reportDataEntity.getHeaderName().equals(ScsbConstants.BIB_ID)) {
                    matchScoreReport.setBibIds(getBibIdListFromString(reportDataEntity));
                } else if (reportDataEntity.getHeaderName().equals(ScsbConstants.MATCH_SCORE)) {
                    matchScoreReport.setMatchScore(Integer.valueOf(reportDataEntity.getHeaderValue()));
                }
            });
            matchScoreReports.add(matchScoreReport);
        });
        return matchScoreReports;
    }

    private Optional<List<MatchingAlgorithmReportDataEntity>> getMonographDataEntitiesFromDB(Integer batchSize, Boolean isPendingMatch, long from) {
        List<MatchingAlgorithmReportDataEntity> reportDataEntities;
        if (isPendingMatch) {
            reportDataEntities = matchingAlgorithmReportDataDetailsRepository.getReportDataEntityForPendingMatchingMonographs(Arrays.asList(ScsbCommonConstants.BIB_ID, ScsbConstants.MATCH_SCORE), from, batchSize);
        } else {
            reportDataEntities = matchingAlgorithmReportDataDetailsRepository.getReportDataEntityForMatchingMonographs(Arrays.asList(ScsbCommonConstants.BIB_ID, ScsbConstants.MATCH_SCORE), from, batchSize);
        }
        return Optional.ofNullable(!CollectionUtils.isEmpty(reportDataEntities) ? reportDataEntities : null);
    }

    private int getTotalPagesCountForMonographs(Integer batchSize, Boolean isPendingMatch) {
        logger.info("Starting grouping process for Monographs");
        long countOfRecordNum = getCountsOfRecordNumForMonograph(isPendingMatch);
        logger.info(ScsbConstants.TOTAL_RECORDS + "{}", countOfRecordNum);
        int totalPagesCount = (int) ((countOfRecordNum*2) / batchSize);
        logger.info(ScsbConstants.TOTAL_PAGES + "{}", totalPagesCount/2);
        return totalPagesCount;
    }

    private long getCountsOfRecordNumForMonograph(Boolean isPendingMatch) {
        long countOfRecordNum = 0;
        if (isPendingMatch) {
            countOfRecordNum = matchingAlgorithmReportDataDetailsRepository.getCountOfRecordNumForMatchingPendingMonograph(ScsbCommonConstants.BIB_ID);
            logger.info("Starting grouping process for Monographs which is in Pending Status which has a total count of :{}", countOfRecordNum);
        } else {
            countOfRecordNum = matchingAlgorithmReportDataDetailsRepository.getCountOfRecordNumForMatchingMonograph(ScsbCommonConstants.BIB_ID);
            logger.info("Starting grouping process for Monographs which has pendingMatch as false which has a total count of : {}", countOfRecordNum);
        }
        return countOfRecordNum;
    }

    private void groupBibsAndAssignMatchScore(List<MatchingAlgorithmReportDataEntity> reportDataEntityList) {
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        Map<String, List<MatchingAlgorithmReportDataEntity>> reportDatasGroupedByRecordNum = reportDataEntityList.stream().collect(Collectors.groupingBy(MatchingAlgorithmReportDataEntity::getRecordNum));
        List<MatchScoreReport> matchScoreReportList = prepareMatchScoreReportList(reportDatasGroupedByRecordNum); // 10k
        Map<Integer, BibliographicEntity> bibIdAndBibEntityMap = getBibIdAndBibliographicEntityMap(matchScoreReportList);//10k-40k
        Set<Integer> bibIdsToIndex = new HashSet<>();
        matchScoreReportList.forEach(matchScoreReport -> {
            List<BibliographicEntity> bibToupdate = matchScoreReport.getBibIds().stream().map(bibIdAndBibEntityMap::get).collect(toList());
            Optional<Map<Integer, BibliographicEntity>> bibliographicEntityMap = matchingAlgorithmUtil.updateBibsForMatchingIdentifier(bibToupdate, matchScoreReport.getMatchScore());
            bibliographicEntityMap.ifPresentOrElse(entry -> bibIdsToIndex.addAll(entry.keySet()), () -> logger.info("No bib ids found to group for indexing"));
        });
        saveAndIndexGroupedBibs(bibIdAndBibEntityMap, bibIdsToIndex);
        clearAllCollection(reportDataEntityList, bibIdAndBibEntityMap, bibIdsToIndex);
        stopWatch.stop();
        logger.info("Total time taken for grouping reports of size {} and bibs size {} is : {}",reportDataEntityList.size(),bibIdAndBibEntityMap.size(),stopWatch.getTotalTimeSeconds());
    }

    public int removeMatchingIdsInDB() {
        return matchingAlgorithmUtil.removeMatchingIdsInDB();
    }

    public int removeMatchingIdsInSolr() throws IOException, SolrServerException {
        int totalBibsIndexed = 0;
        Set<Integer> bibIdsToIndex;
        do {
            bibIdsToIndex = matchingAlgorithmUtil.getBibIdsToRemoveMatchingIdsInSolr();
            if (!bibIdsToIndex.isEmpty()) {
                SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
                solrIndexRequest.setNumberOfThreads(1);
                solrIndexRequest.setNumberOfDocs(10000);
                solrIndexRequest.setCommitInterval(10000);
                solrIndexRequest.setPartialIndexType("BibIdList");
                String collectedBibIds = bibIdsToIndex.stream().map(String::valueOf).collect(Collectors.joining(","));
                solrIndexRequest.setBibIds(collectedBibIds);
                Integer bibsIndexed = bibItemIndexExecutorService.partialIndex(solrIndexRequest);
                logger.info("Completed indexing {} to remove Matching Identifiers for Bib Ids", bibsIndexed);
                totalBibsIndexed = totalBibsIndexed + bibsIndexed;
            }
        } while (!bibIdsToIndex.isEmpty());
        return totalBibsIndexed;
    }
}
