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
import java.util.Set;
import java.util.TimeZone;

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
    MatchingAlgorithmReportDataDetailsRepository reportDataDetailsRepository;

    @Value("${" + PropertyKeyConstants.NONHOLDINGID_INSTITUTION + "}")
    private List<String> nonHoldingInstitutionList;

    private Map collectionGroupMap;
    private Map institutionMap;

    @Autowired
    private MatchingAlgorithmReportDetailRepository matchingAlgorithmReportDetailRepository;

    /**
     * Fetch updated records and start process string.
     *
     * @param date the date
     * @param rows the rows
     * @param isCGDProcess
     * @param isBasedOnMAQualifier
     * @return the string
     * @throws IOException         the io exception
     * @throws SolrServerException the solr server exception
     */
    public String fetchUpdatedRecordsAndStartProcess(Date date, Integer rows, boolean isCGDProcess, boolean isBasedOnMAQualifier) throws IOException, SolrServerException {
        StopWatch ongoingMatchingStopWatch=new StopWatch();
        ongoingMatchingStopWatch.start();
        String status;
        List<MatchingSummaryReport> matchingSummaryReports=new ArrayList<>();
        if(isCGDProcess) {
            matchingAlgorithmUtil.populateMatchingCounter();
            matchingSummaryReports = ongoingMatchingReportsService.populateSummaryReportBeforeMatching();
        }
        List<Integer> serialMvmBibIds = new ArrayList<>();
        List<Integer> bibIdListToIndex = new ArrayList<>();
        String formattedDate = getFormattedDateString(date);
        Integer start = 0;
        int totalProcessed = 0;
        QueryResponse queryResponse = fetchDataForOngoingMatchingBasedOnDate(formattedDate, rows, start,isCGDProcess,isBasedOnMAQualifier);
        Integer totalNumFound = Math.toIntExact(queryResponse.getResults().getNumFound());
        int quotient = totalNumFound / (rows);
        int remainder = totalNumFound % (rows);
        Integer totalPages = remainder == 0 ? quotient : quotient + 1;
        logger.info("Batch Size : {} ",rows);
        logger.info("Total Number of Records Found from date [{}]: {} ", date, totalNumFound);
        logger.info("Total Pages : {} ",totalPages);
        logger.info("{} : {}/{} ", ScsbConstants.CURRENT_PAGE, 1, totalPages);
        SolrDocumentList solrDocumentList = queryResponse.getResults();
        totalProcessed = totalProcessed + solrDocumentList.size();
        status = processOngoingMatchingAlgorithm(solrDocumentList, serialMvmBibIds,isCGDProcess,bibIdListToIndex);

        for(int pageNum=1; pageNum<totalPages; pageNum++) {
            logger.info("{} : {}/{} ", ScsbConstants.CURRENT_PAGE, pageNum+1, totalPages);
            start = pageNum * rows;
            queryResponse = fetchDataForOngoingMatchingBasedOnDate(formattedDate, rows, start,isCGDProcess,isBasedOnMAQualifier);
            solrDocumentList = queryResponse.getResults();
            totalProcessed = totalProcessed + solrDocumentList.size();
            status = processOngoingMatchingAlgorithm(solrDocumentList, serialMvmBibIds,isCGDProcess,bibIdListToIndex);
        }
        if (isCGDProcess) {
            logger.info("Total Number of Records Processed for Ongoing Matching Algorithm: {} ", totalProcessed);
            if (CollectionUtils.isNotEmpty(serialMvmBibIds)) {
                ongoingMatchingReportsService.generateSerialAndMVMsReport(serialMvmBibIds);
            }
            ongoingMatchingReportsService.generateTitleExceptionReport(date, rows);

            ongoingMatchingReportsService.generateSummaryReport(matchingSummaryReports);
        }
        String indexStatus = matchingAlgorithmUtil.indexBibs(bibIdListToIndex);
        ongoingMatchingStopWatch.stop();
        if(isCGDProcess){
            logger.info("{} for OngoingMatching CGD process {} for a total bibs of : {}",ScsbConstants.TOTAL_TIME_TAKEN,ongoingMatchingStopWatch.getTotalTimeSeconds(),bibIdListToIndex.size());
        }
        return status;
    }

    /**
     * Fetch updated records and start process string.
     *
     * @param fromDate the from date
     * @param toDate the to date
     * @param rows the rows
     * @return the string
     * @throws IOException         the io exception
     * @throws SolrServerException the solr server exception
     */
    public String fetchUpdatedRecordsByDateRangeAndStartProcess(Date fromDate, Date toDate, Integer rows,Boolean isCGDProcess) throws IOException, SolrServerException {
        String status;
        matchingAlgorithmUtil.populateMatchingCounter();
        List<MatchingSummaryReport> matchingSummaryReports = ongoingMatchingReportsService.populateSummaryReportBeforeMatching();
        List<Integer> serialMvmBibIds = new ArrayList<>();
        String formattedFromDate = getFormattedDateString(fromDate);
        String formattedToDate = getFormattedDateString(toDate);
        Integer start = 0;
        int totalProcessed = 0;
        QueryResponse queryResponse = fetchDataForOngoingMatchingBasedOnDateRange(formattedFromDate, formattedToDate, rows, start);
        Integer totalNumFound = Math.toIntExact(queryResponse.getResults().getNumFound());
        int quotient = totalNumFound / (rows);
        int remainder = totalNumFound % (rows);
        Integer totalPages = remainder == 0 ? quotient : quotient + 1;
        logger.info("Batch Size : {} ",rows);
        logger.info("Total Number of Records Found : {} ",totalNumFound);
        logger.info("Total Number of Records Found from date [{} TO {}]: {} ", fromDate, toDate, totalNumFound);
        logger.info("Total Pages : {} ",totalPages);
        logger.info("{} : {}/{} ", ScsbConstants.CURRENT_PAGE, 1, totalPages);
        SolrDocumentList solrDocumentList = queryResponse.getResults();
        totalProcessed = totalProcessed + solrDocumentList.size();
        status = processOngoingMatchingAlgorithm(solrDocumentList, serialMvmBibIds,isCGDProcess,new ArrayList<>());

        for(int pageNum=1; pageNum<totalPages; pageNum++) {
            logger.info("{} : {}/{} ", ScsbConstants.CURRENT_PAGE, pageNum+1, totalPages);
            start = pageNum * rows;
            queryResponse = fetchDataForOngoingMatchingBasedOnDateRange(formattedFromDate, formattedToDate, rows, start);
            solrDocumentList = queryResponse.getResults();
            totalProcessed = totalProcessed + solrDocumentList.size();
            status = processOngoingMatchingAlgorithm(solrDocumentList, serialMvmBibIds,isCGDProcess,new ArrayList<>());
        }
        logger.info("Total Number of Records Processed for Ongoing Matching Algorithm: {} ",totalProcessed);
        if(CollectionUtils.isNotEmpty(serialMvmBibIds)) {
            ongoingMatchingReportsService.generateSerialAndMVMsReport(serialMvmBibIds);
        }
        ongoingMatchingReportsService.generateTitleExceptionReport(fromDate, rows);

        ongoingMatchingReportsService.generateSummaryReport(matchingSummaryReports);
        return status;
    }

    /**
     * Fetch updated records by Bib Id range and start process string.
     *
     * @param fromBibId fromBibId
     * @param toBibId toBibId
     * @param rows the rows
     * @return the string
     * @throws IOException         the io exception
     * @throws SolrServerException the solr server exception
     */
    public String fetchUpdatedRecordsByBibIdRangeAndStartProcess(String fromBibId, String toBibId, Integer rows,Boolean isCGDProcess) throws IOException, SolrServerException {
        String status;
        matchingAlgorithmUtil.populateMatchingCounter();
        List<MatchingSummaryReport> matchingSummaryReports = ongoingMatchingReportsService.populateSummaryReportBeforeMatching();
        List<Integer> serialMvmBibIds = new ArrayList<>();
        Integer start = 0;
        int totalProcessed = 0;
        QueryResponse queryResponse = fetchDataForOngoingMatchingBasedOnBibIdRange(fromBibId, toBibId, rows, start);
        Integer totalNumFound = Math.toIntExact(queryResponse.getResults().getNumFound());
        int quotient = totalNumFound / (rows);
        int remainder = totalNumFound % (rows);
        Integer totalPages = remainder == 0 ? quotient : quotient + 1;
        logger.info("Batch Size : {} ",rows);
        logger.info("Total Number of Records Found from Bib Id Range [{} to {}]: {} ", fromBibId, toBibId, totalNumFound);
        logger.info("Total Pages : {} ",totalPages);
        logger.info("{} : {}/{} ", ScsbConstants.CURRENT_PAGE, 1, totalPages);
        SolrDocumentList solrDocumentList = queryResponse.getResults();
        totalProcessed = totalProcessed + solrDocumentList.size();
        status = processOngoingMatchingAlgorithm(solrDocumentList, serialMvmBibIds,isCGDProcess,new ArrayList<>());

        for(int pageNum=1; pageNum<totalPages; pageNum++) {
            logger.info("{} : {}/{} ", ScsbConstants.CURRENT_PAGE, pageNum+1, totalPages);
            start = pageNum * rows;
            queryResponse = fetchDataForOngoingMatchingBasedOnBibIdRange(fromBibId, toBibId, rows, start);
            solrDocumentList = queryResponse.getResults();
            totalProcessed = totalProcessed + solrDocumentList.size();
            status = processOngoingMatchingAlgorithm(solrDocumentList, serialMvmBibIds,isCGDProcess,new ArrayList<>());
        }
        logger.info("Total Number of Records Processed for Ongoing Matching Algorithm: {} ",totalProcessed);
        if(CollectionUtils.isNotEmpty(serialMvmBibIds)) {
            ongoingMatchingReportsService.generateSerialAndMVMsReport(serialMvmBibIds);
        }
        ongoingMatchingReportsService.generateTitleExceptionReport(new Date(), rows);

        ongoingMatchingReportsService.generateSummaryReport(matchingSummaryReports);
        return status;
    }

    /**
     * Fetch updated records by Bib Id range and start process string.
     *
     * @param bibIds bib Ids
     * @param rows the rows
     * @return the string
     * @throws IOException         the io exception
     * @throws SolrServerException the solr server exception
     */
    public String fetchUpdatedRecordsByBibIdsAndStartProcess(String bibIds, Integer rows,Boolean isCGDProcess) throws IOException, SolrServerException {
        String status;
        matchingAlgorithmUtil.populateMatchingCounter();
        List<MatchingSummaryReport> matchingSummaryReports = ongoingMatchingReportsService.populateSummaryReportBeforeMatching();
        List<Integer> serialMvmBibIds = new ArrayList<>();
        Integer start = 0;
        int totalProcessed = 0;
        QueryResponse queryResponse = fetchDataForOngoingMatchingBasedOnBibIds(bibIds, rows, start);
        Integer totalNumFound = Math.toIntExact(queryResponse.getResults().getNumFound());
        int quotient = totalNumFound / (rows);
        int remainder = totalNumFound % (rows);
        Integer totalPages = remainder == 0 ? quotient : quotient + 1;
        logger.info("Batch Size : {} ",rows);
        logger.info("Total Number of Records Found from Bib Ids: {} ", totalNumFound);
        logger.info("Total Pages : {} ",totalPages);
        logger.info("{} : {}/{} ", ScsbConstants.CURRENT_PAGE, 1, totalPages);
        SolrDocumentList solrDocumentList = queryResponse.getResults();
        totalProcessed = totalProcessed + solrDocumentList.size();
        status = processOngoingMatchingAlgorithm(solrDocumentList, serialMvmBibIds,isCGDProcess,new ArrayList<>());

        for(int pageNum=1; pageNum<totalPages; pageNum++) {
            logger.info("{} : {}/{} ", ScsbConstants.CURRENT_PAGE, pageNum+1, totalPages);
            start = pageNum * rows;
            queryResponse = fetchDataForOngoingMatchingBasedOnBibIds(bibIds, rows, start);
            solrDocumentList = queryResponse.getResults();
            totalProcessed = totalProcessed + solrDocumentList.size();
            status = processOngoingMatchingAlgorithm(solrDocumentList, serialMvmBibIds,isCGDProcess,new ArrayList<>());
        }
        logger.info("Total Number of Records Processed for Ongoing Matching Algorithm: {} ",totalProcessed);
        if(CollectionUtils.isNotEmpty(serialMvmBibIds)) {
            ongoingMatchingReportsService.generateSerialAndMVMsReport(serialMvmBibIds);
        }
        ongoingMatchingReportsService.generateTitleExceptionReport(new Date(), rows);

        ongoingMatchingReportsService.generateSummaryReport(matchingSummaryReports);
        return status;
    }

    /**
     * This method fetches data for ongoing matching based on date.
     *
     * @param date      the date
     * @param batchSize the batch size
     * @param start     the start
     * @return the solr document list
     */
    public QueryResponse fetchDataForOngoingMatchingBasedOnDate(String date, Integer batchSize, Integer start,Boolean isCGDProcess,boolean isBasedOnMAQualifier) {
        try {
            String query = isCGDProcess?solrQueryBuilder.fetchBibsForCGDProcess(date,isBasedOnMAQualifier):solrQueryBuilder.fetchBibsForGroupingProcess(date,isBasedOnMAQualifier);
            SolrQuery solrQuery = new SolrQuery(query);
            solrQuery.setStart(start);
            solrQuery.setRows(batchSize);
            return solrTemplate.getSolrClient().query(solrQuery);

        } catch (SolrServerException | IOException e) {
            logger.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return null;
    }

    /**
     * This method fetches data for ongoing matching based on date range.
     *
     * @param fromDate      the from date
     * @param toDate      the to date
     * @param batchSize the batch size
     * @param start     the start
     * @return the solr document list
     */
    public QueryResponse fetchDataForOngoingMatchingBasedOnDateRange(String fromDate, String toDate, Integer batchSize, Integer start) {
        try {
            String query = solrQueryBuilder.fetchCreatedOrUpdatedBibsByDateRange(fromDate, toDate);
            SolrQuery solrQuery = new SolrQuery(query);
            solrQuery.setStart(start);
            solrQuery.setRows(batchSize);
            return solrTemplate.getSolrClient().query(solrQuery);

        } catch (SolrServerException | IOException e) {
            logger.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return null;
    }

    /**
     * This method fetches data for ongoing matching based on Bib Id Range.
     *
     * @param fromBibId the from Bib Id
     * @param toBibId   the to Bib Id
     * @param batchSize the batch size
     * @param start     the start
     * @return the solr document list
     */
    public QueryResponse fetchDataForOngoingMatchingBasedOnBibIdRange(String fromBibId, String toBibId, Integer batchSize, Integer start) {
        try {
            String query = solrQueryBuilder.fetchBibsByBibIdRange(fromBibId, toBibId);
            SolrQuery solrQuery = new SolrQuery(query);
            solrQuery.setStart(start);
            solrQuery.setRows(batchSize);
            return solrTemplate.getSolrClient().query(solrQuery);

        } catch (SolrServerException | IOException e) {
            logger.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return null;
    }

    /**
     * This method fetches data for ongoing matching based on Bib Ids.
     *
     * @param bibIds the bib Ids
     * @param batchSize the batch size
     * @param start     the start
     * @return the solr document list
     */
    public QueryResponse fetchDataForOngoingMatchingBasedOnBibIds(String bibIds, Integer batchSize, Integer start) {
        try {
            String query = solrQueryBuilder.fetchBibsByBibIds(bibIds);
            SolrQuery solrQuery = new SolrQuery(query);
            solrQuery.setStart(start);
            solrQuery.setRows(batchSize);
            return solrTemplate.getSolrClient().query(solrQuery);

        } catch (SolrServerException | IOException e) {
            logger.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return null;
    }

    /**
     * This method is used to process ongoing matching algorithm based on the given bibs in solrDocumentList and updates the CGD and generates report in solr and database.
     *
     * @param solrDocumentList the solr document list
     * @param serialMvmBibIds  the serial mvm bib ids
     * @return the string
     */
    public String processOngoingMatchingAlgorithm(SolrDocumentList solrDocumentList, List<Integer> serialMvmBibIds,Boolean isCGDProcess,List<Integer> bibIdList) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String status = ScsbCommonConstants.SUCCESS;
        if(CollectionUtils.isNotEmpty(solrDocumentList)) {
            for (Iterator<SolrDocument> iterator = solrDocumentList.iterator(); iterator.hasNext(); ) {
                SolrDocument solrDocument = iterator.next();
                int bibId = (Integer) solrDocument.getFieldValue(ScsbConstants.BIB_ID);
                bibIdList.add(bibId);
                status = processMatchingForBib(solrDocument, serialMvmBibIds,isCGDProcess);
            }
            StopWatch stopWatchForMAQualifierUpdate=new StopWatch();
            stopWatchForMAQualifierUpdate.start();
            matchingAlgorithmUtil.resetMAQualifier(bibIdList, isCGDProcess);
            stopWatchForMAQualifierUpdate.stop();
            logger.info("Total time taken for updating and indexing MAQualifier {} for size {}",stopWatchForMAQualifierUpdate.getTotalTimeSeconds(),bibIdList.size());
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
    public String processMatchingForBib(SolrDocument solrDocument, List<Integer> serialMvmBibIds,Boolean isCGDProcess) {
        String status = ScsbCommonConstants.SUCCESS;
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        List<Integer> itemIds = new ArrayList<>();
        int bibId = (Integer) solrDocument.getFieldValue(ScsbConstants.BIB_ID);
        Set<String> matchPointString = getMatchingBibsAndMatchPoints(solrDocument, bibItemMap);
        if(bibItemMap.size() > 0) {
            if(matchPointString.size() > 1) {
                // Multi Match
                logger.info("Multi Match Found for Bib Id: {}", bibId);
                try {
                    itemIds = saveReportAndUpdateCGDForMultiMatch(bibItemMap, serialMvmBibIds,matchPointString,isCGDProcess);
                } catch (IOException | SolrServerException e) {
                    logger.error(ScsbCommonConstants.LOG_ERROR, e);
                    status = ScsbCommonConstants.FAILURE;
                }
            } else if(matchPointString.size() == 1) {
                // Single Match
                logger.info("Single Match Found for Bib Id: {}", bibId);
                try {
                    if(checkIfReportForSingleMatchExists(solrDocument, bibItemMap, matchPointString)){
                        itemIds = saveReportAndUpdateCGDForSingleMatch(bibItemMap, matchPointString.iterator().next(), serialMvmBibIds,isCGDProcess);
                    }
                } catch (Exception e) {
                    logger.error(ScsbCommonConstants.LOG_ERROR,e);
                    status = ScsbCommonConstants.FAILURE;
                }
            }

            if(CollectionUtils.isNotEmpty(itemIds)) {
                updateCGDForItemInSolr(itemIds);
            }
        }
        return status;
    }

    protected boolean checkIfReportForSingleMatchExists(SolrDocument solrDocument, Map<Integer, BibItem> bibItemMap, Set<String> matchPointString) {
        String fieldValue = String.valueOf(solrDocument.getFieldValue(matchPointString.iterator().next())).replaceAll("(^\\[|\\]$)","");
        String bibIds = bibItemMap.keySet().stream().map(Objects::toString).collect(joining(","));
        logger.info("Field Value : {} bibIds {}",fieldValue,bibIds);
        List<MatchingAlgorithmReportDataEntity> reportDataEntityList = reportDataDetailsRepository.getReportDataEntityForSingleMatch(LocalDate.now().toString(), matchPointString.stream().findFirst().get(),fieldValue,bibIds);
        return reportDataEntityList.isEmpty();
    }

    /**
     * This method is used to find the matching points.
     * @param solrDocument
     * @param bibItemMap
     * @return
     */
    private Set<String> getMatchingBibsAndMatchPoints(SolrDocument solrDocument, Map<Integer, BibItem> bibItemMap) {
        Set<String> matchPointString = new HashSet<>();
        addToBibItemMap(solrDocument, bibItemMap, matchPointString, ScsbCommonConstants.OCLC_NUMBER,MatchScoreUtil.OCLC_SCORE);
        addToBibItemMap(solrDocument, bibItemMap, matchPointString, ScsbCommonConstants.ISBN_CRITERIA,MatchScoreUtil.ISBN_SCORE);
        addToBibItemMap(solrDocument, bibItemMap, matchPointString, ScsbCommonConstants.ISSN_CRITERIA,MatchScoreUtil.ISSN_SCORE);
        addToBibItemMap(solrDocument, bibItemMap, matchPointString, ScsbCommonConstants.LCCN_CRITERIA,MatchScoreUtil.LCCN_SCORE);
        return matchPointString;
    }

    private void addToBibItemMap(SolrDocument solrDocument, Map<Integer, BibItem> bibItemMap, Set<String> matchPointString, String matchPointField, Integer matchScore) {
        Map<Integer, BibItem> tempMap;
        tempMap = findMatchingBibs(solrDocument, matchPointString, matchPointField,matchScore,bibItemMap);
        if (tempMap != null && tempMap.size() > 0)
            bibItemMap.putAll(tempMap);
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

    private List<Integer> saveReportAndUpdateCGDForSingleMatch(Map<Integer, BibItem> bibItemMap, String matchPointString, List<Integer> serialMvmBibIds,Boolean isCGDProcess) {
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
        for (Iterator<Integer> iterator = bibItemMap.keySet().iterator(); iterator.hasNext(); ) {
            Integer bibId = iterator.next();
            BibItem bibItem = bibItemMap.get(bibId);
            owningInstSet.add(bibItem.getOwningInstitution());
            owningInstList.add(bibItem.getOwningInstitution());
            owningInstBibIds.add(bibItem.getOwningInstitutionBibId());
            bibIds.add(bibId);
            materialTypeList.add(bibItem.getLeaderMaterialType());
            materialTypeSet.add(bibItem.getLeaderMaterialType());
            if(matchPointString.equalsIgnoreCase(ScsbCommonConstants.OCLC_NUMBER)) {
                criteriaValues.addAll(bibItem.getOclcNumber());
            } else if(matchPointString.equalsIgnoreCase(ScsbCommonConstants.ISBN_CRITERIA)) {
                criteriaValues.addAll(bibItem.getIsbn());
            } else if(matchPointString.equalsIgnoreCase(ScsbCommonConstants.ISSN_CRITERIA)) {
                criteriaValues.addAll(bibItem.getIssn());
            } else if(matchPointString.equalsIgnoreCase(ScsbCommonConstants.LCCN_CRITERIA)) {
                criteriaValues.add(bibItem.getLccn());
            }
            index = index + 1;
            if(StringUtils.isNotBlank(bibItem.getTitleSubFieldA())) {
                String titleHeader = ScsbCommonConstants.TITLE + index;
                matchingAlgorithmUtil.getReportDataEntity(titleHeader, bibItem.getTitleSubFieldA(), reportDataEntities);
                titleMap.put(titleHeader, bibItem.getTitleSubFieldA());
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
                    itemIds = updateCGDBasedOnMaterialTypes(reportEntity, materialTypeSet, serialMvmBibIds, ScsbConstants.SINGLE_MATCH, parameterMap, reportEntitiesToSave, titleMap,matchScore,bibItemMap,isCGDProcess);
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
     * @param bibItemMap
     * @param matchPointString
     * @return
     * @throws IOException
     * @throws SolrServerException
     */
    private List<Integer> saveReportAndUpdateCGDForMultiMatch(Map<Integer, BibItem> bibItemMap, List<Integer> serialMvmBibIds, Set<String> matchPointString,Boolean isCGDProcess) throws IOException, SolrServerException {
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
        List<Integer> itemIds = new ArrayList<>();
        Integer matchScore=0;

        for (String matchPoint : matchPointString) {
            matchScore=MatchScoreUtil.calculateMatchScore(matchScore, MatchScoreUtil.getMatchScoreForMatchPoint(matchPoint));
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
            if(CollectionUtils.isNotEmpty(bibItem.getOclcNumber()))
                oclcNumbers.addAll(bibItem.getOclcNumber());
            if(CollectionUtils.isNotEmpty(bibItem.getIsbn()))
                isbns.addAll(bibItem.getIsbn());
            if(CollectionUtils.isNotEmpty(bibItem.getIssn()))
                issns.addAll(bibItem.getIssn());
            if(StringUtils.isNotBlank(bibItem.getLccn()))
                lccns.add(bibItem.getLccn());
        }

        if(owningInstSet.size() > 1) {
            Map parameterMap = new HashMap();
            parameterMap.put(ScsbCommonConstants.BIB_ID, bibIdList);
            parameterMap.put(ScsbConstants.MATERIAL_TYPE, materialTypeList);
            itemIds = updateCGDBasedOnMaterialTypes(reportEntity, materialTypes, serialMvmBibIds, ScsbConstants.MULTI_MATCH, parameterMap, new ArrayList<>(), null,matchScore,bibItemMap,isCGDProcess);
            materialTypeList = (List<String>) parameterMap.get(ScsbConstants.MATERIAL_TYPE);
            if (isCGDProcess) {
                matchingAlgorithmUtil.getReportDataEntityList(reportDataEntities, owningInstList, bibIdList, materialTypeList, owningInstBibIds, matchScore);
                checkAndAddReportEntities(reportDataEntities, oclcNumbers, ScsbCommonConstants.OCLC_CRITERIA);
                checkAndAddReportEntities(reportDataEntities, isbns, ScsbCommonConstants.ISBN_CRITERIA);
                checkAndAddReportEntities(reportDataEntities, issns, ScsbCommonConstants.ISSN_CRITERIA);
                checkAndAddReportEntities(reportDataEntities, lccns, ScsbCommonConstants.LCCN_CRITERIA);
                checkAndAddReportEntities(reportDataEntities, matchPointString, "MatchPoints");
                reportEntity.addAll(reportDataEntities);
                producerTemplate.sendBody("scsbactivemq:queue:saveMatchingReportsQ", Arrays.asList(reportEntity));
            }
        }
        return itemIds;
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
                                                        List<MatchingAlgorithmReportEntity> reportEntityList, Map<String,String> titleMap,Integer matchScore,Map<Integer, BibItem> bibItemMap,Boolean isCGDProcess) throws IOException, SolrServerException {
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
                boolean isMonograph = matchingAlgorithmCGDProcessor.checkForMonographAndPopulateValues(materialTypeSet, itemEntityMap, bibIdList,ScsbConstants.ONGOING_MATCHING_OPERATION_TYPE,isCGDProcess);
                if(isMonograph) {
                    if(matchType.equalsIgnoreCase(ScsbConstants.SINGLE_MATCH)) {
                        MatchingAlgorithmReportEntity reportEntityForTitleException = titleVerificationForSingleMatch(reportEntity.getFileName(), titleMap, bibIdList, materialTypeList, parameterMap);
                        if(reportEntityForTitleException != null) {
                            logger.info(ScsbConstants.MATCHING_ALGORITHM_UPDATE_CGD_MESSAGE);
                            reportEntityList.add(reportEntityForTitleException);
                        }
                        else {
                            if (!itemEntityMap.isEmpty() && isCGDProcess) {
                                matchingAlgorithmCGDProcessor.updateCGDProcess(itemEntityMap);
                            }
                            else if(!isCGDProcess){
                                bibIdList.forEach(bibId->{
                                    BibItem bibItem = bibItemMap.get(bibId);
                                    Integer calculatedSingleMatchScore = MatchScoreUtil.getMatchScoreForSingleMatchAndTitle(bibItem.getMatchScore());
                                    bibItem.setMatchScore(calculatedSingleMatchScore);
                                });
                                groupBibsAndUpdateInDB(bibIdList,bibItemMap);
                            }
                        }
                    }
                    else if(matchType.equalsIgnoreCase(ScsbConstants.MULTI_MATCH)){
                        if (!itemEntityMap.isEmpty() && isCGDProcess) {
                            matchingAlgorithmCGDProcessor.updateCGDProcess(itemEntityMap);
                        }
                        else if(!isCGDProcess){
                            groupBibsAndUpdateInDB(bibIdList, bibItemMap);
                        }
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
                            }else{
                                matchScore = MatchScoreUtil.getMatchScoreForSingleMatchAndTitle(matchScore);
                            }
                        }
                        if(materialTypeSet.contains(ScsbConstants.MONOGRAPHIC_SET)) {
                            int size = materialTypeList.size();
                            materialTypeList = new ArrayList<>();
                            for(int i = 0; i < size; i++) {
                                materialTypeList.add(ScsbConstants.MONOGRAPHIC_SET);
                            }
                            if(isCGDProcess){
                                matchingAlgorithmCGDProcessor.updateItemsCGD(itemEntityMap);
                            }
                            serialMvmBibIds.addAll(bibIdList);
                        }
                    }
                    if(!isCGDProcess){
                        groupBibsAndUpdateInDB(bibIdList, bibItemMap);
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
                        matchScore = MatchScoreUtil.getMatchScoreForSingleMatchAndTitle(matchScore);
                        matchingAlgorithmCGDProcessor.populateItemEntityMap(itemEntityMap, bibIdList);
                        if(isCGDProcess){
                            matchingAlgorithmCGDProcessor.updateItemsCGD(itemEntityMap);
                        }
                        serialMvmBibIds.addAll(bibIdList);
                    }
                }
                else if(matchType.equalsIgnoreCase(ScsbConstants.MULTI_MATCH)){
                    matchingAlgorithmCGDProcessor.populateItemEntityMap(itemEntityMap, bibIdList);
                    if(isCGDProcess){
                        matchingAlgorithmCGDProcessor.updateItemsCGD(itemEntityMap);
                    }
                    serialMvmBibIds.addAll(bibIdList);
                }
                if(!isCGDProcess) {
                    groupBibsAndUpdateInDB(bibIdList, bibItemMap);
                }
            }
        } else {
            reportEntity.setType(ScsbConstants.MATERIAL_TYPE_EXCEPTION);
        }
        parameterMap.put(ScsbConstants.MATERIAL_TYPE, materialTypeList);
        return itemIds;
    }

    private void groupBibsAndUpdateInDB(List<Integer> bibIdList, Map<Integer, BibItem> bibItemMap) {
        List<BibliographicEntity> bibliographicEntityList = bibliographicDetailsRepository.findByIdIn(bibIdList);
        matchingAlgorithmUtil.updateBibsForMatchingIdentifier(bibliographicEntityList, bibItemMap);
        matchingAlgorithmUtil.saveGroupedBibsToDb(bibliographicEntityList);
    }

    /**
     * This method is used to find the matching bibs
     * @param solrDocument
     * @param matchPointString
     * @param fieldName
     * @param matchScore
     * @param existingBibItemMap
     * @return
     */
    private Map<Integer, BibItem> findMatchingBibs(SolrDocument solrDocument, Set<String> matchPointString, String fieldName, Integer matchScore, Map<Integer, BibItem> existingBibItemMap) {
        Map<Integer, BibItem> bibItemMap = null;
        Object value = solrDocument.getFieldValue(fieldName);
        if(value != null) {
            if(value instanceof String) {
                String fieldValue = (String) value;
                if(StringUtils.isNotBlank(fieldValue)) {
                    String query = solrQueryBuilder.solrQueryForOngoingMatching(fieldName, fieldValue);
                    bibItemMap = getBibsFromSolr(matchPointString, fieldName, query, matchScore, existingBibItemMap);
                }
            } else if(value instanceof List) {
                List<String> fieldValues = (List<String>) value;
                if(CollectionUtils.isNotEmpty(fieldValues)) {
                    String query = solrQueryBuilder.solrQueryForOngoingMatching(fieldName, fieldValues);
                    bibItemMap = getBibsFromSolr(matchPointString, fieldName, query,matchScore,existingBibItemMap);
                }
            }
        }
        return bibItemMap;
    }

    /**
     * This method is used to get bibs from the solr
     *
     * @param matchPointString the match point string
     * @param fieldName        the field name
     * @param query            the query
     * @param matchScore
     * @param existingBibItemMap
     * @return bibs from solr
     */
    public Map<Integer, BibItem> getBibsFromSolr(Set<String> matchPointString, String fieldName, String query, Integer matchScore, Map<Integer, BibItem> existingBibItemMap) {
        Map<Integer, BibItem> bibItemMap = new HashMap<>();
        SolrQuery solrQuery = new SolrQuery(query);
        try {
            QueryResponse queryResponse = solrTemplate.getSolrClient().query(solrQuery);
            SolrDocumentList solrDocumentList = queryResponse.getResults();
            long numFound = solrDocumentList.getNumFound();
            if(numFound > 1) {
                matchPointString.add(fieldName);
                if(numFound > solrDocumentList.size()) {
                    solrQuery.setRows((int) numFound);
                    queryResponse = solrTemplate.getSolrClient().query(solrQuery);
                    solrDocumentList = queryResponse.getResults();
                }
                for (Iterator<SolrDocument> iterator = solrDocumentList.iterator(); iterator.hasNext(); ) {
                    SolrDocument solrDocument = iterator.next();
                    BibItem bibItem = populateBibItem(solrDocument);
                    Integer incomingBibsMatchScore=0;
                    Integer existingLoopBibMatchScore=0;
                    if(!(bibItem.getMatchScore()==null) && bibItem.getMatchScore()!=0){
                        incomingBibsMatchScore=bibItem.getMatchScore();
                    }
                    if(existingBibItemMap.get(bibItem.getBibId())!=null){
                        existingLoopBibMatchScore = existingBibItemMap.get(bibItem.getBibId()).getMatchScore();
                    }
                    Integer calulatedMatchScore = MatchScoreUtil.calculateMatchScore(incomingBibsMatchScore, existingLoopBibMatchScore);
                    Integer finalCalcutedMatchScore = MatchScoreUtil.calculateMatchScore(calulatedMatchScore, matchScore);
                    bibItem.setMatchScore(finalCalcutedMatchScore);
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
            collectionGroupMap = new HashMap();
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
            institutionMap = new HashMap();
            Iterable<InstitutionEntity> institutionEntities = institutionDetailsRepository.findAll();
            for (Iterator<InstitutionEntity> iterator = institutionEntities.iterator(); iterator.hasNext(); ) {
                InstitutionEntity institutionEntity = iterator.next();
                institutionMap.put(institutionEntity.getInstitutionCode(), institutionEntity.getId());
            }
        }
        return institutionMap;
    }
}
