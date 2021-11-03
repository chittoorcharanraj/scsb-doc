package org.recap.util;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.matchingalgorithm.MatchScoreUtil;
import org.recap.matchingalgorithm.MatchingAlgorithmCGDProcessor;
import org.recap.matchingalgorithm.service.OngoingMatchingReportsService;
import org.recap.model.jpa.*;
import org.recap.model.matchingreports.MatchingSummaryReport;
import org.recap.model.solr.BibItem;
import org.recap.model.solr.SolrIndexRequest;
import org.recap.repository.jpa.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;


/**
 * Created by angelind on 2/2/17.
 */
@Component
public class OngoingMatchingAlgorithmUtil {

    private static final Logger logger = LoggerFactory.getLogger(OngoingMatchingAlgorithmUtil.class);

    @Autowired
    private SolrQueryBuilder solrQueryBuilder;

    @Resource(name = "recapSolrTemplate")
    private SolrTemplate solrTemplate;

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private BibliographicDetailsRepositoryForMatching bibliographicDetailsRepositoryForMatching;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private MatchingAlgorithmUtil matchingAlgorithmUtil;

    @Autowired
    private UpdateCgdUtil updateCgdUtil;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private OngoingMatchingReportsService ongoingMatchingReportsService;

    @Autowired
    private MatchingAlgorithmReportDataDetailsRepository reportDataDetailsRepository;

    @Autowired
    private OngoingMatchingAlgorithmQueryUtil ongoingMatchingAlgorithmQueryUtil;

    @Autowired
    private DateUtil dateUtil;

    @Value("${" + PropertyKeyConstants.NONHOLDINGID_INSTITUTION + "}")
    private List<String> nonHoldingInstitutionList;

    private Map collectionGroupMap;
    private Map institutionMap;

    @Autowired
    private MatchingAlgorithmReportDetailRepository matchingAlgorithmReportDetailRepository;

    /**
     * Fetch updated records and Start Cgd Update Process Based On Criteria.
     *
     * @param solrIndexRequest the Solr Index Request
     * @param rows             the rows
     * @return the string
     * @throws IOException         the io exception
     * @throws SolrServerException the solr server exception
     */
    public String fetchUpdatedRecordsAndStartCgdUpdateProcessBasedOnCriteria(SolrIndexRequest solrIndexRequest, Integer rows) throws Exception {
        StopWatch ongoingMatchingStopWatch = new StopWatch();
        ongoingMatchingStopWatch.start();
        String status;
        matchingAlgorithmUtil.populateMatchingCounter();
        List<MatchingSummaryReport> matchingSummaryReports = ongoingMatchingReportsService.populateSummaryReportBeforeMatching();
        List<Integer> serialMvmBibIds = new ArrayList<>();
        List<Integer> bibIdListToIndex = new ArrayList<>();
        Integer start = 0;
        int totalProcessed = 0;
        String query = ongoingMatchingAlgorithmQueryUtil.prepareQueryForOngoingMatchingCgdUpdateProcessBasedOnCriteria(solrIndexRequest);
        QueryResponse queryResponse = ongoingMatchingAlgorithmQueryUtil.fetchDataByQuery(query, rows, start);
        Integer totalNumFound = Math.toIntExact(queryResponse.getResults().getNumFound());
        int quotient = totalNumFound / (rows);
        int remainder = totalNumFound % (rows);
        Integer totalPages = remainder == 0 ? quotient : quotient + 1;
        logger.info("Total Number of Records Found : {}", totalNumFound);
        logger.info("Batch Size : {} ", rows);
        logger.info("Total Pages : {} ", totalPages);
        logger.info("{} : {}/{} ", ScsbConstants.CURRENT_PAGE, 1, totalPages);
        SolrDocumentList solrDocumentList = queryResponse.getResults();
        totalProcessed = totalProcessed + solrDocumentList.size();
        status = processOngoingMatchingAlgorithm(solrDocumentList, serialMvmBibIds, true, bibIdListToIndex);

        for (int pageNum = 1; pageNum < totalPages; pageNum++) {
            logger.info("{} : {}/{} ", ScsbConstants.CURRENT_PAGE, pageNum + 1, totalPages);
            start = pageNum * rows;
            queryResponse = ongoingMatchingAlgorithmQueryUtil.fetchDataByQuery(query, rows, start);
            solrDocumentList = queryResponse.getResults();
            totalProcessed = totalProcessed + solrDocumentList.size();
            status = processOngoingMatchingAlgorithm(solrDocumentList, serialMvmBibIds, true, bibIdListToIndex);
        }
        logger.info("Total Number of Records Processed for Ongoing Matching Algorithm: {} ", totalProcessed);
        if (CollectionUtils.isNotEmpty(serialMvmBibIds)) {
            ongoingMatchingReportsService.generateSerialAndMVMsReport(serialMvmBibIds);
        }
        ongoingMatchingReportsService.generateTitleExceptionReport(getFromDateFromRequest(solrIndexRequest), rows);
        ongoingMatchingReportsService.generateSummaryReport(matchingSummaryReports);
        if (CollectionUtils.isNotEmpty(bibIdListToIndex) && solrIndexRequest.isIndexBibsForOngoingMa()) {
            matchingAlgorithmUtil.indexBibs(bibIdListToIndex);
        }
        ongoingMatchingStopWatch.stop();
        logger.info("{} for OngoingMatching CGD process {} for a total bibs of : {}", ScsbConstants.TOTAL_TIME_TAKEN, ongoingMatchingStopWatch.getTotalTimeSeconds(), bibIdListToIndex.size());
        return status;
    }

    /**
     * Fetch updated records and Start Grouping Process Based On Criteria.
     *
     * @param solrIndexRequest the Solr Index Request
     * @param rows             the rows
     * @return the string
     * @throws IOException         the io exception
     * @throws SolrServerException the solr server exception
     */
    public String fetchUpdatedRecordsAndStartGroupingProcessBasedOnCriteria(SolrIndexRequest solrIndexRequest, Integer rows) throws Exception {
        StopWatch ongoingMatchingStopWatch = new StopWatch();
        ongoingMatchingStopWatch.start();
        String status;
        List<Integer> matchedBibIds = new ArrayList<>();
        List<Integer> bibIdListToIndex = new ArrayList<>();
        Integer start = 0;
        int totalProcessed = 0;
        String query = ongoingMatchingAlgorithmQueryUtil.prepareQueryForOngoingMatchingGroupingProcessBasedOnCriteria(solrIndexRequest);
        QueryResponse queryResponse = ongoingMatchingAlgorithmQueryUtil.fetchDataByQuery(query, rows, start);
        Integer totalNumFound = Math.toIntExact(queryResponse.getResults().getNumFound());
        int quotient = totalNumFound / (rows);
        int remainder = totalNumFound % (rows);
        Integer totalPages = remainder == 0 ? quotient : quotient + 1;
        logger.info("Total Number of Records Found : {}", totalNumFound);
        logger.info("Batch Size : {} ", rows);
        logger.info("Total Pages : {} ", totalPages);
        logger.info("{} : {}/{} ", ScsbConstants.CURRENT_PAGE, 1, totalPages);
        SolrDocumentList solrDocumentList = queryResponse.getResults();
        totalProcessed = totalProcessed + solrDocumentList.size();
        status = processOngoingMatchingAlgorithm(solrDocumentList, matchedBibIds, false, bibIdListToIndex);

        for (int pageNum = 1; pageNum < totalPages; pageNum++) {
            logger.info("{} : {}/{} ", ScsbConstants.CURRENT_PAGE, pageNum + 1, totalPages);
            start = pageNum * rows;
            queryResponse = ongoingMatchingAlgorithmQueryUtil.fetchDataByQuery(query, rows, start);
            solrDocumentList = queryResponse.getResults();
            totalProcessed = totalProcessed + solrDocumentList.size();
            status = processOngoingMatchingAlgorithm(solrDocumentList, matchedBibIds, false, bibIdListToIndex);
        }
        if (CollectionUtils.isNotEmpty(matchedBibIds)) {
            bibIdListToIndex.addAll(matchedBibIds);
        }
        if (CollectionUtils.isNotEmpty(bibIdListToIndex)) {
            List<Integer> bibIdListToIndexUnique = bibIdListToIndex.stream().distinct().collect(Collectors.toList());
            matchingAlgorithmUtil.updateAnamolyFlagForBibs(bibIdListToIndexUnique);
            if (solrIndexRequest.isIndexBibsForOngoingMa()) {
                matchingAlgorithmUtil.indexBibs(bibIdListToIndexUnique);
            }
        }
        ongoingMatchingStopWatch.stop();
        logger.info("{} for OngoingMatching Grouping process {} for a total bibs of : {}", ScsbConstants.TOTAL_TIME_TAKEN, ongoingMatchingStopWatch.getTotalTimeSeconds(), bibIdListToIndex.size());
        return status;
    }

    /**
     * This method is used to process ongoing matching algorithm based on the given bibs in solrDocumentList and updates the CGD and generates report in solr and database.
     *
     * @param solrDocumentList the solr document list
     * @param serialMvmBibIds  the serial mvm bib ids
     * @return the string
     */
    public String processOngoingMatchingAlgorithm(SolrDocumentList solrDocumentList, List<Integer> serialMvmBibIds, Boolean isCGDProcess, List<Integer> bibIdListToIndex) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String status = ScsbCommonConstants.SUCCESS;
        List<Integer> bibIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(solrDocumentList)) {
            for (Iterator<SolrDocument> iterator = solrDocumentList.iterator(); iterator.hasNext(); ) {
                SolrDocument solrDocument = iterator.next();
                int bibId = (Integer) solrDocument.getFieldValue(ScsbConstants.BIB_ID);
                bibIdListToIndex.add(bibId);
                bibIdList.add(bibId);
                status = processMatchingForBib(solrDocument, serialMvmBibIds, isCGDProcess);
            }
            matchingAlgorithmUtil.resetMAQualifier(bibIdList, isCGDProcess);
        }
        stopWatch.stop();
        logger.info("Total Time taken to executing matching algorithm only for {} records : {}" , solrDocumentList.size(), stopWatch.getTotalTimeSeconds());
        return status;
    }

    /**
     * This method processes matching for multiple match scenario and single match scenario.
     *
     * @param solrDocument    the solr document
     * @param serialMvmBibIds the serial mvm bib ids
     * @return the string
     */
    public String processMatchingForBib(SolrDocument solrDocument, List<Integer> serialMvmBibIds, Boolean isCGDProcess) {
        String status = ScsbCommonConstants.SUCCESS;
        List<Integer> itemIds = new ArrayList<>();
        Map<String, HashMap<Integer, BibItem>> bibItemMap = new HashMap<>();
        int bibId = (Integer) solrDocument.getFieldValue(ScsbConstants.BIB_ID);
        bibItemMap = getMatchingBibsAndMatchPoints(solrDocument, bibItemMap);
        Map<String, HashMap<Integer, BibItem>> multiMatchBibItemMap = new HashMap<>();
        Map<String, HashMap<Integer, BibItem>> singleMatchBibItemMap = new HashMap<>();
        String matchpoint = null;
        if (bibItemMap.size() > 0) {
            for (Iterator<String> bibItemIterator = bibItemMap.keySet().iterator(); bibItemIterator.hasNext(); ) {
                matchpoint = bibItemIterator.next();
                if (matchpoint.contains("-")) {
                    multiMatchBibItemMap.put(matchpoint, bibItemMap.get(matchpoint));
                } else {
                    if (!ScsbConstants.TITLE.equalsIgnoreCase(matchpoint)) {
                        singleMatchBibItemMap.put(matchpoint, bibItemMap.get(matchpoint));
                    }
                }
            }
            if (multiMatchBibItemMap.size() > 0) {
                // Multi Match
                logger.info("Multi Match Found for Bib Id: {}", bibId);
                try {
                    itemIds = saveReportAndUpdateCGDForMultiMatch(multiMatchBibItemMap, serialMvmBibIds, isCGDProcess);
                } catch (IOException | SolrServerException e) {
                    logger.error(ScsbCommonConstants.LOG_ERROR, e);
                    status = ScsbCommonConstants.FAILURE;
                }
            }
            if (singleMatchBibItemMap.size() > 0 && isCGDProcess) {
                // Single Match
                logger.info("Single Match Found for Bib Id: {}", bibId);
                try {
                    if (checkIfReportForSingleMatchExists(solrDocument, singleMatchBibItemMap)) {
                        itemIds = saveReportAndUpdateCGDForSingleMatch(singleMatchBibItemMap, serialMvmBibIds, isCGDProcess);
                    }
                } catch (Exception e) {
                    logger.error(ScsbCommonConstants.LOG_ERROR, e);
                    status = ScsbCommonConstants.FAILURE;
                }
            }
            if (CollectionUtils.isNotEmpty(itemIds) && isCGDProcess) {
                updateCGDForItemInSolr(itemIds);
            }
        }
        return status;
    }


    protected boolean checkIfReportForSingleMatchExists(SolrDocument solrDocument, Map<String, HashMap<Integer, BibItem>> singleMatchBibItemMap) {
        String matchPointString = null;
        HashMap<Integer, BibItem> bibItemMap = new HashMap<>();
        for (Iterator<String> bibItemIterator = singleMatchBibItemMap.keySet().iterator(); bibItemIterator.hasNext(); ) {
            matchPointString = bibItemIterator.next();
            bibItemMap = singleMatchBibItemMap.get(matchPointString);
        }
        String fieldValue = String.valueOf(solrDocument.getFieldValue(matchPointString)).replaceAll("(^\\[|\\]$)", "");
        String bibIds = bibItemMap.keySet().stream().map(Objects::toString).collect(joining(","));
        logger.info("Field Value : {} bibIds {}", fieldValue, bibItemMap.size());
        List<MatchingAlgorithmReportDataEntity> reportDataEntityList = reportDataDetailsRepository.getReportDataEntityForSingleMatch(LocalDate.now().toString(), matchPointString, fieldValue, bibIds);
        return reportDataEntityList.isEmpty();
    }

    /**
     * This method is used to find the matching points.
     * @param solrDocument
     * @param bibItemMap
     * @return
     */
    private Map<String, HashMap<Integer, BibItem>> getMatchingBibsAndMatchPoints(SolrDocument solrDocument, Map<String, HashMap<Integer, BibItem>> bibItemMap) {
        HashMap<Integer, BibItem> oclcBibItemMap = new HashMap<>();
        HashMap<Integer, BibItem> isbnBibItemMap = new HashMap<>();
        HashMap<Integer, BibItem> issnBibItemMap = new HashMap<>();
        HashMap<Integer, BibItem> lccnBibItemMap = new HashMap<>();
        HashMap<Integer, BibItem> titleBibItemMap = new HashMap<>();
        List<Integer> oclcBibIds = new ArrayList<>();
        List<Integer> isbnBibIds = new ArrayList<>();
        List<Integer> issnBibIds = new ArrayList<>();
        List<Integer> lccnBibIds = new ArrayList<>();
        List<Integer> titleBibIds = new ArrayList<>();

        addToBibItemMap(solrDocument, oclcBibItemMap, ScsbCommonConstants.OCLC_NUMBER, MatchScoreUtil.OCLC_SCORE);
        addToBibItemMap(solrDocument, isbnBibItemMap, ScsbCommonConstants.ISBN_CRITERIA, MatchScoreUtil.ISBN_SCORE);
        addToBibItemMap(solrDocument, issnBibItemMap, ScsbCommonConstants.ISSN_CRITERIA, MatchScoreUtil.ISSN_SCORE);
        addToBibItemMap(solrDocument, lccnBibItemMap, ScsbCommonConstants.LCCN_CRITERIA, MatchScoreUtil.LCCN_SCORE);
        addToBibItemMap(solrDocument, titleBibItemMap, ScsbConstants.TITLE_MATCH_SOLR, MatchScoreUtil.TITLE_SCORE);

        if (oclcBibItemMap != null && oclcBibItemMap.size() > 1) {
            for (Map.Entry<Integer, BibItem> entry : oclcBibItemMap.entrySet()) {
                oclcBibIds.add(entry.getKey());
            }
        }
        if (isbnBibItemMap != null && isbnBibItemMap.size() > 1) {
            for (Map.Entry<Integer, BibItem> entry : isbnBibItemMap.entrySet()) {
                isbnBibIds.add(entry.getKey());
            }
        }
        if (issnBibItemMap != null && issnBibItemMap.size() > 1) {
            for (Map.Entry<Integer, BibItem> entry : issnBibItemMap.entrySet()) {
                issnBibIds.add(entry.getKey());
            }
        }
        if (lccnBibItemMap != null && lccnBibItemMap.size() > 1) {
            for (Map.Entry<Integer, BibItem> entry : lccnBibItemMap.entrySet()) {
                lccnBibIds.add(entry.getKey());
            }
        }
        if (titleBibItemMap != null && titleBibItemMap.size() > 1) {
            for (Map.Entry<Integer, BibItem> entry : titleBibItemMap.entrySet()) {
                titleBibIds.add(entry.getKey());
            }
        }
            Collection oclcisbn = CollectionUtils.intersection(oclcBibIds, isbnBibIds);
            Collection oclcissn = CollectionUtils.intersection(oclcBibIds, issnBibIds);
            Collection oclclccn = CollectionUtils.intersection(oclcBibIds, lccnBibIds);
            Collection isbnissn = CollectionUtils.intersection(isbnBibIds, issnBibIds);
            Collection isbnlccn = CollectionUtils.intersection(isbnBibIds, lccnBibIds);
            Collection issnlccn = CollectionUtils.intersection(issnBibIds, lccnBibIds);
            Collection oclcTitle = CollectionUtils.intersection(oclcBibIds, titleBibIds);
            Collection isbnTitle = CollectionUtils.intersection(isbnBibIds, titleBibIds);
            Collection issnTitle = CollectionUtils.intersection(issnBibIds, titleBibIds);
            Collection lccnTitle = CollectionUtils.intersection(lccnBibIds, titleBibIds);

            HashMap<Integer, BibItem> oclcisbnBibItemMap = new HashMap<>();
            HashMap<Integer, BibItem> oclcissnBibItemMap = new HashMap<>();
            HashMap<Integer, BibItem> oclclccnBibItemMap = new HashMap<>();
            HashMap<Integer, BibItem> isbnissnBibItemMap = new HashMap<>();
            HashMap<Integer, BibItem> isbnlccnBibItemMap = new HashMap<>();
            HashMap<Integer, BibItem> issnlccnBibItemMap = new HashMap<>();
            HashMap<Integer, BibItem> oclctitleBibItemMap = new HashMap<>();
            HashMap<Integer, BibItem> isbntitleBibItemMap = new HashMap<>();
            HashMap<Integer, BibItem> issntitleBibItemMap = new HashMap<>();
            HashMap<Integer, BibItem> lccntitleBibItemMap = new HashMap<>();


        if(oclcisbn.size() > 1) {
            for (Iterator<Integer> iterator = oclcisbn.iterator(); iterator.hasNext(); ) {
                Integer bibId = iterator.next();
                BibItem oclcisbnbibItem = oclcBibItemMap.get(bibId);
                oclcisbnBibItemMap.put(bibId, oclcisbnbibItem);
                bibItemMap.put(ScsbConstants.OCLCISBN, oclcisbnBibItemMap);
            }
        }
            if (oclcissn.size() > 1) {
                for (Iterator<Integer> iterator = oclcissn.iterator(); iterator.hasNext(); ) {
                    Integer bibId = iterator.next();
                    BibItem oclcissnbibItem = oclcBibItemMap.get(bibId);
                    oclcissnBibItemMap.put(bibId, oclcissnbibItem);
                    bibItemMap.put(ScsbConstants.OCLCISSN, oclcissnBibItemMap);

                }
            }
            if (oclclccn.size() > 1) {
                for (Iterator<Integer> iterator = oclclccn.iterator(); iterator.hasNext(); ) {
                    Integer bibId = iterator.next();
                    BibItem oclclccnbibItem = oclcBibItemMap.get(bibId);
                    oclclccnBibItemMap.put(bibId, oclclccnbibItem);
                    bibItemMap.put(ScsbConstants.OCLCLCCN, oclclccnBibItemMap);
                }
           }
            if (isbnissn.size() > 1) {
                for (Iterator<Integer> iterator = isbnissn.iterator(); iterator.hasNext(); ) {
                    Integer bibId = iterator.next();
                    BibItem isbnissnbibItem = isbnBibItemMap.get(bibId);
                    isbnissnBibItemMap.put(bibId, isbnissnbibItem);
                    bibItemMap.put(ScsbConstants.ISBNISSN, isbnissnBibItemMap);
                }
            }
            if (isbnlccn.size() > 1) {
                for (Iterator<Integer> iterator = isbnlccn.iterator(); iterator.hasNext(); ) {
                    Integer bibId = iterator.next();
                    BibItem isbnlccnbibItem = isbnBibItemMap.get(bibId);
                    isbnlccnBibItemMap.put(bibId, isbnlccnbibItem);
                    bibItemMap.put(ScsbConstants.ISBNLCCN, isbnlccnBibItemMap);
                    }
            }
            if (issnlccn.size() > 1) {
                for (Iterator<Integer> iterator = issnlccn.iterator(); iterator.hasNext(); ) {
                    Integer bibId = iterator.next();
                    BibItem issnlccnbibItem = issnBibItemMap.get(bibId);
                    issnlccnBibItemMap.put(bibId, issnlccnbibItem);
                    bibItemMap.put(ScsbConstants.ISSNLCCN, issnlccnBibItemMap);
                    }
          }
        if (oclcTitle.size() > 1) {
            for (Iterator<Integer> iterator = oclcTitle.iterator(); iterator.hasNext(); ) {
                Integer bibId = iterator.next();
                BibItem oclctitlebibItem = oclcBibItemMap.get(bibId);
                oclctitleBibItemMap.put(bibId, oclctitlebibItem);
                bibItemMap.put(ScsbConstants.OCLCTITLE, oclctitleBibItemMap);
            }
        }
        if (isbnTitle.size() > 1) {
            for (Iterator<Integer> iterator = isbnTitle.iterator(); iterator.hasNext(); ) {
                Integer bibId = iterator.next();
                BibItem isbntitlebibItem = isbnBibItemMap.get(bibId);
                isbntitleBibItemMap.put(bibId, isbntitlebibItem);
                bibItemMap.put(ScsbConstants.ISBNTITLE, isbntitleBibItemMap);
            }
        }
        if (issnTitle.size() > 1) {
            for (Iterator<Integer> iterator = issnTitle.iterator(); iterator.hasNext(); ) {
                Integer bibId = iterator.next();
                BibItem issntitlebibItem = issnBibItemMap.get(bibId);
                issntitleBibItemMap.put(bibId, issntitlebibItem);
                bibItemMap.put(ScsbConstants.ISSNTITLE, issntitleBibItemMap);
            }
        }
        if (lccnTitle.size() > 1) {
            for (Iterator<Integer> iterator = lccnTitle.iterator(); iterator.hasNext(); ) {
                Integer bibId = iterator.next();
                BibItem lccntitlebibItem = lccnBibItemMap.get(bibId);
                lccntitleBibItemMap.put(bibId, lccntitlebibItem);
                bibItemMap.put(ScsbConstants.LCCNTITLE, lccntitleBibItemMap);
            }
        }

            oclcBibIds.removeAll(oclcisbn);
            oclcBibIds.removeAll(oclcissn);
            oclcBibIds.removeAll(oclclccn);
            oclcBibIds.removeAll(oclcTitle);
            isbnBibIds.removeAll(isbnissn);
            isbnBibIds.removeAll(isbnlccn);
            isbnBibIds.removeAll(oclcisbn);
            isbnBibIds.removeAll(isbnTitle);
            issnBibIds.removeAll(oclcissn);
            issnBibIds.removeAll(isbnissn);
            issnBibIds.removeAll(issnlccn);
            issnBibIds.removeAll(issnTitle);
            lccnBibIds.removeAll(oclclccn);
            lccnBibIds.removeAll(isbnlccn);
            lccnBibIds.removeAll(issnlccn);
            lccnBibIds.removeAll(lccnTitle);
//            titleBibIds.removeAll(oclcTitle);
//            titleBibIds.removeAll(isbnTitle);
//            titleBibIds.removeAll(issnTitle);
//            titleBibIds.removeAll(lccnTitle);

        if (!oclcBibIds.isEmpty()  && oclcBibItemMap.size() > 1) {
            bibItemMap.put(ScsbCommonConstants.OCLC_NUMBER, oclcBibItemMap);
        }
        if (!isbnBibIds.isEmpty() && isbnBibItemMap.size() > 1) {
            bibItemMap.put(ScsbCommonConstants.ISBN_CRITERIA, isbnBibItemMap);
        }
        if (!issnBibIds.isEmpty() && issnBibItemMap.size() > 1) {
            bibItemMap.put(ScsbCommonConstants.ISSN_CRITERIA, issnBibItemMap);
        }
        if (!lccnBibIds.isEmpty() && lccnBibItemMap.size() > 1) {
            bibItemMap.put(ScsbCommonConstants.LCCN_CRITERIA, lccnBibItemMap);
        }
        if (!titleBibIds.isEmpty() && titleBibItemMap.size() > 1) {
            bibItemMap.put(ScsbConstants.TITLE, titleBibItemMap);
        }
       return bibItemMap;
        }

    private void addToBibItemMap(SolrDocument solrDocument, Map<Integer, BibItem> bibItemMap, String matchPointField, Integer matchScore) {
        Map<Integer, BibItem> tempMap;
        tempMap = findMatchingBibs(solrDocument, matchPointField,matchScore,bibItemMap);
        if (tempMap != null && tempMap.size() > 0)  {
            bibItemMap.putAll(tempMap);
        }
    }

    /**
     * This method updates cgd for item in solr.
     *
     * @param itemIds the item ids
     */
    public void updateCGDForItemInSolr(List<Integer> itemIds) {
        if (CollectionUtils.isNotEmpty(itemIds)) {
            List<ItemEntity> itemEntities = itemDetailsRepository.findByIdIn(itemIds);
            updateCgdUtil.updateCGDForItemInSolr(itemEntities);
        }
    }

    private List<Integer> saveReportAndUpdateCGDForSingleMatch( Map<String, HashMap<Integer, BibItem>> singleMatchedBibItemMap, List<Integer> serialMvmBibIds,Boolean isCGDProcess) {
        List<MatchingAlgorithmReportDataEntity> reportDataEntities = new ArrayList<>();
        Set<String> owningInstSet = new HashSet<>();
        Set<String> materialTypeSet = new HashSet<>();
        List<Integer> bibIds = new ArrayList<>();
        List<String> owningInstList = new ArrayList<>();
        List<String> materialTypeList = new ArrayList<>();
        Map<String,String> titleMap = new HashMap<>();
        List<String> owningInstBibIds = new ArrayList<>();
        List<Integer> itemIds = new ArrayList<>();
        List<String> criteriaValues = new ArrayList<>();
        List<MatchingAlgorithmReportEntity> reportEntitiesToSave = new ArrayList<>();
        Integer matchScore=0;

        int index=0;
        String matchPointString = null;
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        for (Iterator<String> bibMatchIterator = singleMatchedBibItemMap.keySet().iterator(); bibMatchIterator.hasNext(); ) {
            matchPointString = bibMatchIterator.next();
            bibItemMap = singleMatchedBibItemMap.get(matchPointString);
            for (Iterator<Integer> iterator = bibItemMap.keySet().iterator(); iterator.hasNext(); ) {
                Integer bibId = iterator.next();
                BibItem bibItem = bibItemMap.get(bibId);
                bibItem.setMatchScore(MatchScoreUtil.getMatchScoreForMatchPoint(matchPointString));
                owningInstSet.add(bibItem.getOwningInstitution());
                owningInstList.add(bibItem.getOwningInstitution());
                owningInstBibIds.add(bibItem.getOwningInstitutionBibId());
                bibIds.add(bibId);
                materialTypeList.add(bibItem.getLeaderMaterialType());
                materialTypeSet.add(bibItem.getLeaderMaterialType());
                if (matchPointString.equalsIgnoreCase(ScsbCommonConstants.OCLC_NUMBER)) {
                    criteriaValues.addAll(bibItem.getOclcNumber());
                } else if (matchPointString.equalsIgnoreCase(ScsbCommonConstants.ISBN_CRITERIA)) {
                    criteriaValues.addAll(bibItem.getIsbn());
                } else if (matchPointString.equalsIgnoreCase(ScsbCommonConstants.ISSN_CRITERIA)) {
                    criteriaValues.addAll(bibItem.getIssn());
                } else if (matchPointString.equalsIgnoreCase(ScsbCommonConstants.LCCN_CRITERIA)) {
                    criteriaValues.add(bibItem.getLccn());
                } else if (matchPointString.equalsIgnoreCase(ScsbConstants.TITLE)) {
                    criteriaValues.add(bibItem.getTitleMatch());
                }
                index = index + 1;
                if (StringUtils.isNotBlank(bibItem.getTitleSubFieldA())) {
                    String titleHeader = ScsbCommonConstants.TITLE + index;
                    matchingAlgorithmUtil.getReportDataEntity(titleHeader, bibItem.getTitleSubFieldA(), reportDataEntities);
                    titleMap.put(titleHeader, bibItem.getTitleSubFieldA());
                }
            }
        }

        if(owningInstSet.size() > 1) {
            MatchingAlgorithmReportEntity reportEntity = new MatchingAlgorithmReportEntity();
            String fileName = ScsbCommonConstants.ONGOING_MATCHING_ALGORITHM;
            reportEntity.setFileName(fileName);
            reportEntity.setInstitutionName(ScsbCommonConstants.ALL_INST);
            reportEntity.setCreatedDate(new Date());
            String criteriaValueString = StringUtils.join(criteriaValues, ",");
            if(materialTypeSet.size() == 1) {
                Map parameterMap = new HashMap();
                parameterMap.put(ScsbCommonConstants.BIB_ID, bibIds);
                parameterMap.put(ScsbConstants.MATERIAL_TYPE, materialTypeList);
                parameterMap.put(ScsbCommonConstants.OWNING_INST_BIB_ID, owningInstBibIds);
                parameterMap.put(ScsbConstants.OWNING_INST, owningInstList);
                parameterMap.put(ScsbConstants.CRITERIA_VALUES, criteriaValueString);
                parameterMap.put(ScsbConstants.MATCH_POINT, matchPointString);
                try {
                    matchScore=MatchScoreUtil.getMatchScoreForMatchPoint(matchPointString);
                    if (isCGDProcess) {
                        itemIds = updateCGDBasedOnMaterialTypes(reportEntity, materialTypeSet, serialMvmBibIds, ScsbConstants.SINGLE_MATCH, parameterMap, reportEntitiesToSave, titleMap, matchScore, bibItemMap);
                    }
                    materialTypeList = (List<String>) parameterMap.get(ScsbConstants.MATERIAL_TYPE);
                    reportEntity.setType(ScsbConstants.SINGLE_MATCH.concat("-").concat(matchPointString));
                } catch (Exception e) {
                    logger.error(ScsbCommonConstants.LOG_ERROR,e);
                }
            } else {
                reportEntity.setType(ScsbConstants.MATERIAL_TYPE_EXCEPTION);
            }
            if (isCGDProcess) {
                matchingAlgorithmUtil.getReportDataEntityList(reportDataEntities, owningInstList, bibIds, materialTypeList, owningInstBibIds, matchScore);
                matchingAlgorithmUtil.getReportDataEntity(matchPointString.equalsIgnoreCase(ScsbCommonConstants.OCLC_NUMBER) ? ScsbCommonConstants.OCLC_CRITERIA : matchPointString, criteriaValueString, reportDataEntities);
                reportEntity.addAll(reportDataEntities);
                reportEntitiesToSave.add(reportEntity);
                matchingAlgorithmReportDetailRepository.saveAll(reportEntitiesToSave);
                matchingAlgorithmReportDetailRepository.flush();
            }
        }
        return itemIds;
    }

    /**
     * This method processes cgd and reports for un matching titles report entity.
     *
     * @param fileName                 the file name
     * @param titleMap                 the title map
     * @param bibIds                   the bib ids
     * @param materialTypes            the material types
     * @param owningInstitutions       the owning institutions
     * @param owningInstBibIds         the owning inst bib ids
     * @param matchPointValue          the match point value
     * @param unMatchingTitleHeaderSet the un matching title header set
     * @param matchPointString         the match point string
     * @return the report entity
     */
    public MatchingAlgorithmReportEntity processCGDAndReportsForUnMatchingTitles(String fileName, Map<String, String> titleMap, List<Integer> bibIds, List<String> materialTypes, List<String> owningInstitutions,
                                                                                 List<String> owningInstBibIds, String matchPointValue, Set<String> unMatchingTitleHeaderSet, String matchPointString) {
        MatchingAlgorithmReportEntity unMatchReportEntity = matchingAlgorithmUtil.buildReportEntity(fileName);
        List<MatchingAlgorithmReportDataEntity> reportDataEntityList = new ArrayList<>();
        List<String> bibIdList = new ArrayList<>();
        List<String> materialTypeList = new ArrayList<>();
        List<String> owningInstitutionList = new ArrayList<>();
        List<String> owningInstBibIdList = new ArrayList<>();

        matchingAlgorithmUtil.prepareReportForUnMatchingTitles(titleMap, bibIds, materialTypes, owningInstitutions, owningInstBibIds, unMatchingTitleHeaderSet, reportDataEntityList, bibIdList, materialTypeList, owningInstitutionList, owningInstBibIdList);

        List<Integer> bibliographicIds = new ArrayList<>();
        for (Iterator<String> iterator = bibIdList.iterator(); iterator.hasNext(); ) {
            String bibId = iterator.next();
            bibliographicIds.add(Integer.valueOf(bibId));
        }
        matchingAlgorithmUtil.getReportDataEntityList(reportDataEntityList, owningInstitutionList, bibIdList, materialTypeList, owningInstBibIdList,0);
        matchingAlgorithmUtil.getReportDataEntity(matchPointString.equalsIgnoreCase(ScsbCommonConstants.OCLC_NUMBER) ? ScsbCommonConstants.OCLC_CRITERIA : matchPointString, matchPointValue, reportDataEntityList);
        unMatchReportEntity.addAll(reportDataEntityList);
        return unMatchReportEntity;
    }

    /**
     * This method is used to generate reports and update CGD for multiple match scenario
     * @param multiMatchedBibItemMap
     * @param
     * @return
     * @throws IOException
     * @throws SolrServerException
     */
    private List<Integer> saveReportAndUpdateCGDForMultiMatch(Map<String, HashMap<Integer, BibItem>>  multiMatchedBibItemMap, List<Integer> serialMvmBibIds, Boolean isCGDProcess) throws IOException, SolrServerException {
        calculateAndSetMatchScores(multiMatchedBibItemMap);
        MatchingAlgorithmReportEntity reportEntity = new MatchingAlgorithmReportEntity();
        reportEntity.setFileName(ScsbCommonConstants.ONGOING_MATCHING_ALGORITHM);
        reportEntity.setCreatedDate(new Date());
        reportEntity.setInstitutionName(ScsbCommonConstants.ALL_INST);
        List<MatchingAlgorithmReportDataEntity> reportDataEntities = new ArrayList<>();
        Set<String> owningInstSet = new HashSet<>();
        List<String> owningInstList = new ArrayList<>();
        List<Integer> bibIdList = new ArrayList<>();
        List<String> materialTypeList = new ArrayList<>();
        Set<String> materialTypes = new HashSet<>();
        List<String> owningInstBibIds = new ArrayList<>();
        Set<String> oclcNumbers = new HashSet<>();
        Set<String> isbns = new HashSet<>();
        Set<String> issns = new HashSet<>();
        Set<String> lccns = new HashSet<>();
        Set<String> titleMatches = new HashSet<>();

        List<Integer> ids = new ArrayList<>();
        Integer matchScore = 0;
        Set<String> matchPointStrings = new HashSet<>();
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        for (Iterator<String> bibIterator = multiMatchedBibItemMap.keySet().iterator(); bibIterator.hasNext(); ) {
            Integer matchScoreCal=0;
            Set<String> matchPointString = new HashSet<>();
            String bibMatchPoint = bibIterator.next();
            String[] matchPointTokens = bibMatchPoint.split("-");
            for (String matchPoint : matchPointTokens) {
                if(!matchPointString.contains(matchPoint)) {
                    matchPointString.add(matchPoint);
                    matchPointStrings.add(matchPoint);
                    matchScoreCal = MatchScoreUtil.calculateMatchScore(matchScoreCal, MatchScoreUtil.getMatchScoreForMatchPoint(matchPoint));
                }
            }
            matchScore = matchScoreCal;

            bibItemMap = multiMatchedBibItemMap.get(bibMatchPoint);
            for (Map.Entry<Integer, BibItem> integerBibItemEntry : bibItemMap.entrySet()) {
               if(integerBibItemEntry.getValue().getMatchScore() != null && integerBibItemEntry.getValue().getMatchScore() > 0) {
                   Integer matchScoreNew = MatchScoreUtil.calculateMatchScore(matchScoreCal, integerBibItemEntry.getValue().getMatchScore());
                   //integerBibItemEntry.getValue().setMatchScore(matchScoreNew);
               }
               else {
                  //integerBibItemEntry.getValue().setMatchScore(matchScoreCal);
              }
            }

            for (Iterator<Integer> iterator = bibItemMap.keySet().iterator(); iterator.hasNext(); ) {
                Integer bibId = iterator.next();
                BibItem bibItem = bibItemMap.get(bibId);
                owningInstSet.add(bibItem.getOwningInstitution());
                owningInstList.add(bibItem.getOwningInstitution());
                bibIdList.add(bibItem.getBibId());
                materialTypes.add(bibItem.getLeaderMaterialType());
                materialTypeList.add(bibItem.getLeaderMaterialType());
                owningInstBibIds.add(bibItem.getOwningInstitutionBibId());
                if (CollectionUtils.isNotEmpty(bibItem.getOclcNumber()))
                    oclcNumbers.addAll(bibItem.getOclcNumber());
                if (CollectionUtils.isNotEmpty(bibItem.getIsbn()))
                    isbns.addAll(bibItem.getIsbn());
                if (CollectionUtils.isNotEmpty(bibItem.getIssn()))
                    issns.addAll(bibItem.getIssn());
                if (StringUtils.isNotBlank(bibItem.getLccn()))
                    lccns.add(bibItem.getLccn());
                if (StringUtils.isNotBlank(bibItem.getTitleMatch()))
                    titleMatches.add(bibItem.getTitleMatch());
            }
        }
        if(owningInstSet.size() > 1) {
            Map parameterMap = new HashMap();
            parameterMap.put(ScsbCommonConstants.BIB_ID, bibIdList);
            parameterMap.put(ScsbConstants.MATERIAL_TYPE, materialTypeList);
            if (isCGDProcess) {
                ids = updateCGDBasedOnMaterialTypes(reportEntity, materialTypes, serialMvmBibIds, ScsbConstants.MULTI_MATCH, parameterMap, new ArrayList<>(), null,matchScore,bibItemMap);
                materialTypeList = (List<String>) parameterMap.get(ScsbConstants.MATERIAL_TYPE);
                matchingAlgorithmUtil.getReportDataEntityList(reportDataEntities, owningInstList, bibIdList, materialTypeList, owningInstBibIds, matchScore);
                checkAndAddReportEntities(reportDataEntities, oclcNumbers, ScsbCommonConstants.OCLC_CRITERIA);
                checkAndAddReportEntities(reportDataEntities, isbns, ScsbCommonConstants.ISBN_CRITERIA);
                checkAndAddReportEntities(reportDataEntities, issns, ScsbCommonConstants.ISSN_CRITERIA);
                checkAndAddReportEntities(reportDataEntities, lccns, ScsbCommonConstants.LCCN_CRITERIA);
                checkAndAddReportEntities(reportDataEntities, titleMatches, ScsbConstants.TITLE);
                checkAndAddReportEntities(reportDataEntities, matchPointStrings, "MatchPoints");
                reportEntity.addAll(reportDataEntities);
                producerTemplate.sendBody("scsbactivemq:queue:saveMatchingReportsQ", Arrays.asList(reportEntity));
            } else {
                groupBibsAndUpdateInDB(bibIdList, bibItemMap);
                ids = bibIdList;
            }
        }
        return ids;
    }

    private void calculateAndSetMatchScores(Map<String, HashMap<Integer, BibItem>> multiMatchedBibItemMap) {
        if (!multiMatchedBibItemMap.isEmpty()) {
            Map<Integer, BibItem> distinctBibItemMap = new HashMap<>();
            for (Map.Entry<String, HashMap<Integer, BibItem>> entryMultiMatchedBibItemMap : multiMatchedBibItemMap.entrySet()) {
                HashMap<Integer, BibItem> multiMatchCombinationBibItemMap = entryMultiMatchedBibItemMap.getValue();
                distinctBibItemMap.putAll(multiMatchCombinationBibItemMap);

            }
            for (Map.Entry<String, HashMap<Integer, BibItem>> entryMultiMatchedBibItemMap : multiMatchedBibItemMap.entrySet()) {
                String multiMatchCombination = entryMultiMatchedBibItemMap.getKey();
                HashMap<Integer, BibItem> multiMatchCombinationBibItemMap = entryMultiMatchedBibItemMap.getValue();
                String[] matchPointTokens = multiMatchCombination.split("-");
                Integer matchScore = 0;
                for (String matchPoint : matchPointTokens) {
                    matchScore = MatchScoreUtil.calculateMatchScore(matchScore, MatchScoreUtil.getMatchScoreForMatchPoint(matchPoint));
                }
                for (Map.Entry<Integer, BibItem> entryMultiMatchCombinationBibItemMap : multiMatchCombinationBibItemMap.entrySet()) {
                    Integer bibId = entryMultiMatchCombinationBibItemMap.getKey();
                    BibItem bibItem = distinctBibItemMap.get(bibId);
                    if (bibItem.getMatchScore() != null && bibItem.getMatchScore() > 0) {
                        Integer matchScoreNew = MatchScoreUtil.calculateMatchScore(matchScore, bibItem.getMatchScore());
                        bibItem.setMatchScore(matchScoreNew);
                    } else {
                        bibItem.setMatchScore(matchScore);
                    }
                }
            }

            for (Map.Entry<String, HashMap<Integer, BibItem>> entryMultiMatchedBibItemMap : multiMatchedBibItemMap.entrySet()) {
                HashMap<Integer, BibItem> multiMatchCombinationBibItemMap = entryMultiMatchedBibItemMap.getValue();
                for (Map.Entry<Integer, BibItem> entryMultiMatchCombinationBibItemMap : multiMatchCombinationBibItemMap.entrySet()) {
                    Integer bibId = entryMultiMatchCombinationBibItemMap.getKey();
                    BibItem bibItem = entryMultiMatchCombinationBibItemMap.getValue();
                    BibItem distinctBibItem = distinctBibItemMap.get(bibId);
                    bibItem.setMatchScore(distinctBibItem.getMatchScore());
                }
            }
        }
    }

    private void checkAndAddReportEntities(List<MatchingAlgorithmReportDataEntity> reportDataEntities, Set<String> oclcNumbers, String oclcCriteria) {
        if (CollectionUtils.isNotEmpty(oclcNumbers)) {
            MatchingAlgorithmReportDataEntity oclcNumberReportDataEntity = matchingAlgorithmUtil.getReportDataEntityForCollectionValues(oclcNumbers, oclcCriteria);
            reportDataEntities.add(oclcNumberReportDataEntity);
        }
    }

    /**
     * This method checks for monograph and updates the CGD
     *
     * @param reportEntity     the report entity
     * @param materialTypes    the material types
     * @param serialMvmBibIds  the serial mvm bib ids
     * @param matchType        the match type
     * @param parameterMap     the parameter map
     * @param reportEntityList the report entity list
     * @param titleMap         the title map
     * @return the list
     * @throws IOException         the io exception
     * @throws SolrServerException the solr server exception
     */
    private List<Integer> updateCGDBasedOnMaterialTypes(MatchingAlgorithmReportEntity reportEntity, Set<String> materialTypes, List<Integer> serialMvmBibIds, String matchType, Map parameterMap,
                                                        List<MatchingAlgorithmReportEntity> reportEntityList, Map<String,String> titleMap,Integer matchScore,Map<Integer, BibItem> bibItemMap) throws IOException, SolrServerException {
        List<Integer> itemIds = new ArrayList<>();
        List<String> materialTypeList = (List<String>) parameterMap.get(ScsbConstants.MATERIAL_TYPE);
        List<Integer> bibIdList = (List<Integer>) parameterMap.get(ScsbCommonConstants.BIB_ID);
        MatchingAlgorithmCGDProcessor matchingAlgorithmCGDProcessor = new MatchingAlgorithmCGDProcessor(bibliographicDetailsRepository, producerTemplate,
                getCollectionGroupMap(), getInstitutionEntityMap(), itemChangeLogDetailsRepository, ScsbConstants.ONGOING_MATCHING_OPERATION_TYPE, collectionGroupDetailsRepository, itemDetailsRepository, institutionDetailsRepository,nonHoldingInstitutionList);
        if(materialTypes.size() == 1) {
            reportEntity.setType(matchType);
            Map<Integer, ItemEntity> itemEntityMap = new HashMap<>();
            if(materialTypes.contains(ScsbCommonConstants.MONOGRAPH)) {
                Set<String> materialTypeSet = new HashSet<>();
                boolean isMonograph = matchingAlgorithmCGDProcessor.checkForMonographAndPopulateValues(materialTypeSet, itemEntityMap, bibIdList,ScsbConstants.ONGOING_MATCHING_OPERATION_TYPE);
                if(isMonograph) {
                    if(matchType.equalsIgnoreCase(ScsbConstants.SINGLE_MATCH)) {
                        MatchingAlgorithmReportEntity reportEntityForTitleException = titleVerificationForSingleMatch(reportEntity.getFileName(), titleMap, bibIdList, materialTypeList, parameterMap);
                        if(reportEntityForTitleException != null) {
                            logger.info(ScsbConstants.MATCHING_ALGORITHM_UPDATE_CGD_MESSAGE);
                            reportEntityList.add(reportEntityForTitleException);
                        }
                        else {
                            if (!itemEntityMap.isEmpty()) {
                                matchingAlgorithmCGDProcessor.updateCGDProcess(itemEntityMap);
                            }
                        }
                    }
                    else if(matchType.equalsIgnoreCase(ScsbConstants.MULTI_MATCH) && !itemEntityMap.isEmpty()){
                        matchingAlgorithmCGDProcessor.updateCGDProcess(itemEntityMap);
                    }
                } else {
                    if(materialTypeSet.size() > 1) {
                        reportEntity.setType(ScsbConstants.MATERIAL_TYPE_EXCEPTION);
                    } else if(materialTypeSet.size() == 1){
                        reportEntity.setType(matchType);
                        if(matchType.equalsIgnoreCase(ScsbConstants.SINGLE_MATCH)) {
                            MatchingAlgorithmReportEntity reportEntityForTitleException = titleVerificationForSingleMatch(reportEntity.getFileName(), titleMap, bibIdList, materialTypeList, parameterMap);
                            if(reportEntityForTitleException != null) {
                                logger.info(ScsbConstants.MATCHING_ALGORITHM_UPDATE_CGD_MESSAGE);
                                reportEntityList.add(reportEntityForTitleException);
                            }
                        }
                        if(materialTypeSet.contains(ScsbConstants.MONOGRAPHIC_SET)) {
                            int size = materialTypeList.size();
                            materialTypeList = new ArrayList<>();
                            for(int i = 0; i < size; i++) {
                                materialTypeList.add(ScsbConstants.MONOGRAPHIC_SET);
                            }
                            matchingAlgorithmCGDProcessor.updateItemsCGD(itemEntityMap);
                            serialMvmBibIds.addAll(bibIdList);
                        }
                    }
                }
            } else if(materialTypes.contains(ScsbCommonConstants.SERIAL)) {
                if(matchType.equalsIgnoreCase(ScsbConstants.SINGLE_MATCH)) {
                    MatchingAlgorithmReportEntity reportEntityForTitleException = titleVerificationForSingleMatch(reportEntity.getFileName(), titleMap, bibIdList, materialTypeList, parameterMap);
                    if(reportEntityForTitleException != null) {
                        logger.info(ScsbConstants.MATCHING_ALGORITHM_UPDATE_CGD_MESSAGE);
                        reportEntityList.add(reportEntityForTitleException);
                    }
                    else {
                        matchingAlgorithmCGDProcessor.populateItemEntityMap(itemEntityMap, bibIdList);
                        matchingAlgorithmCGDProcessor.updateItemsCGD(itemEntityMap);
                        serialMvmBibIds.addAll(bibIdList);
                    }
                }
                else if(matchType.equalsIgnoreCase(ScsbConstants.MULTI_MATCH)){
                    matchingAlgorithmCGDProcessor.populateItemEntityMap(itemEntityMap, bibIdList);
                    matchingAlgorithmCGDProcessor.updateItemsCGD(itemEntityMap);
                    serialMvmBibIds.addAll(bibIdList);
                }
            }
        } else {
            reportEntity.setType(ScsbConstants.MATERIAL_TYPE_EXCEPTION);
        }
        parameterMap.put(ScsbConstants.MATERIAL_TYPE, materialTypeList);
        return itemIds;
    }

    private void groupBibsAndUpdateInDB(List<Integer> bibIdList, Map<Integer, BibItem> bibItemMap) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<BibliographicEntityForMatching> bibliographicEntityList = bibliographicDetailsRepositoryForMatching.findAllById(bibIdList);
        Optional<Map<Integer,BibliographicEntityForMatching>> bibliographicEntityOptional = matchingAlgorithmUtil.updateBibsForMatchingIdentifier(bibliographicEntityList, bibItemMap);
        if (bibliographicEntityOptional.isPresent()) {
            Map<Integer, BibliographicEntityForMatching> bibliographicEntityListToBeSaved = bibliographicEntityOptional.get();
            matchingAlgorithmUtil.saveGroupedBibsToDbForOngoing(bibliographicEntityListToBeSaved.values());
        }
        stopWatch.stop();
        logger.info("Total Time taken to save grouped Bibs : {}" , stopWatch.getTotalTimeSeconds());
    }

    /**
     * This method is used to find the matching bibs
     * @param solrDocument
     * @param fieldName
     * @param matchScore
     * @param existingBibItemMap
     * @return
     */
    private Map<Integer, BibItem> findMatchingBibs(SolrDocument solrDocument, String fieldName, Integer matchScore, Map<Integer, BibItem> existingBibItemMap) {
        Map<Integer, BibItem> bibItemMap = null;
        Object value = solrDocument.getFieldValue(fieldName);
        if(value != null) {
            if(value instanceof String) {
                String fieldValue = (String) value;
                if(StringUtils.isNotBlank(fieldValue)) {
                    String query = solrQueryBuilder.solrQueryForOngoingMatching(fieldName, fieldValue);
                    bibItemMap = getBibsFromSolr(fieldName, query, matchScore, existingBibItemMap);
                }
            } else if(value instanceof List) {
                List<String> fieldValues = (List<String>) value;
                if(CollectionUtils.isNotEmpty(fieldValues)) {
                    String query = solrQueryBuilder.solrQueryForOngoingMatching(fieldName, fieldValues);
                    bibItemMap = getBibsFromSolr(fieldName, query,matchScore,existingBibItemMap);
                }
            }
        }
        return bibItemMap;
    }

    /**
     * This method is used to get bibs from the solr
     *
     * @param fieldName        the field name
     * @param query            the query
     * @param matchScore
     * @param existingBibItemMap
     * @return bibs from solr
     */
    public Map<Integer, BibItem> getBibsFromSolr(String fieldName, String query, Integer matchScore, Map<Integer, BibItem> existingBibItemMap) {
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        SolrQuery solrQuery = new SolrQuery(query);
        try {
            QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
            SolrDocumentList solrDocumentList = queryResponse.getResults();
            long numFound = solrDocumentList.getNumFound();
            if(numFound > 1) {
                if(numFound > solrDocumentList.size()) {
                    solrQuery.setRows((int) numFound);
                    queryResponse = solrTemplate.getSolrClient().query(solrQuery);
                    solrDocumentList = queryResponse.getResults();
                }
                for (Iterator<SolrDocument> iterator = solrDocumentList.iterator(); iterator.hasNext(); ) {
                    SolrDocument solrDocument = iterator.next();
                    BibItem bibItem = populateBibItem(solrDocument);
                    bibItemMap.put(bibItem.getBibId(), bibItem);
                }
            }
        } catch (IOException|SolrServerException e) {
            logger.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return bibItemMap;
    }

    /**
     * Title verification for single match report entity.
     *
     * @param fileName         the file name
     * @param titleMap         the title map
     * @param bibIds           the bib ids
     * @param materialTypeList the material type list
     * @param parameterMap     the parameter map
     * @return the report entity
     */
    private MatchingAlgorithmReportEntity titleVerificationForSingleMatch(String fileName, Map<String,String> titleMap, List<Integer> bibIds, List<String> materialTypeList, Map parameterMap) {
        MatchingAlgorithmReportEntity reportEntity = null;
        List<String> owningInstBibIds = (List<String>) parameterMap.get(ScsbCommonConstants.OWNING_INST_BIB_ID);
        List<String> owningInstList = (List<String>) parameterMap.get(ScsbConstants.OWNING_INST);
        String criteriaValues = (String) parameterMap.get(ScsbConstants.CRITERIA_VALUES);
        String matchPointString = (String) parameterMap.get(ScsbConstants.MATCH_POINT);
        Set<String> unMatchingTitleHeaderSet = matchingAlgorithmUtil.getMatchingAndUnMatchingBibsOnTitleVerification(titleMap);
        if(CollectionUtils.isNotEmpty(unMatchingTitleHeaderSet)) {
            reportEntity = processCGDAndReportsForUnMatchingTitles(fileName, titleMap, bibIds,
                    materialTypeList, owningInstList, owningInstBibIds,
                    criteriaValues, unMatchingTitleHeaderSet, matchPointString);
            logger.info("titleVerificationForSingleMatch, Single Match, found unmatching titles");
        }
        return reportEntity;
    }

    /**
     * This method populates bib item.
     *
     * @param solrDocument the solr document
     * @return the bib item
     */
    public BibItem populateBibItem(SolrDocument solrDocument) {
        Collection<String> fieldNames = solrDocument.getFieldNames();
        BibItem bibItem = new BibItem();
        bibItem=commonUtil.getBibItemFromSolrFieldNames(solrDocument, fieldNames, bibItem);
        return bibItem;
    }

    private Date getFromDateFromRequest(SolrIndexRequest solrIndexRequest) throws ParseException {
        if (StringUtils.isNotBlank(solrIndexRequest.getFromDate())) {
            return getFormattedDate(solrIndexRequest.getFromDate());
        } else {
            return new Date();
        }
    }

    private Date getFormattedDate(String fromDate) throws ParseException {
        Date date = new SimpleDateFormat(ScsbConstants.ONGOING_MATCHING_DATE_FORMAT).parse(fromDate);
        return dateUtil.getFromDate(date);
    }

    /**
     * This method gets formatted date.
     *
     * @param inputDate the input date
     * @return the formatted date string
     */
    public String getFormattedDateString(Date inputDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ScsbCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        String utcStr = null;
        try {
            String inputDateString = simpleDateFormat.format(inputDate);
            Date date = simpleDateFormat.parse(inputDateString);
            DateFormat format = new SimpleDateFormat(ScsbCommonConstants.UTC_DATE_FORMAT);
            format.setTimeZone(TimeZone.getTimeZone(ScsbCommonConstants.UTC));
            utcStr = format.format(date);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return utcStr + ScsbCommonConstants.SOLR_DATE_RANGE_TO_NOW;
    }

    /**
     * This method gets collection group map.
     *
     * @return the collection group map
     */
    public Map getCollectionGroupMap() {
        if (null == collectionGroupMap) {
            collectionGroupMap = new HashMap<>();
            Iterable<CollectionGroupEntity> collectionGroupEntities = collectionGroupDetailsRepository.findAll();
            for (Iterator<CollectionGroupEntity> iterator = collectionGroupEntities.iterator(); iterator.hasNext(); ) {
                CollectionGroupEntity collectionGroupEntity = iterator.next();
                collectionGroupMap.put(collectionGroupEntity.getCollectionGroupCode(), collectionGroupEntity.getId());
            }
        }
        return collectionGroupMap;
    }

    /**
     * This method gets institution entity map.
     *
     * @return the institution entity map
     */
    public Map getInstitutionEntityMap() {
        if (null == institutionMap) {
            institutionMap = new HashMap<>();
            Iterable<InstitutionEntity> institutionEntities = institutionDetailsRepository.findAll();
            for (Iterator<InstitutionEntity> iterator = institutionEntities.iterator(); iterator.hasNext(); ) {
                InstitutionEntity institutionEntity = iterator.next();
                institutionMap.put(institutionEntity.getInstitutionCode(), institutionEntity.getId());
            }
        }
        return institutionMap;
    }
}
