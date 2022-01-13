package org.recap.matchingalgorithm.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.jpa.MatchingAlgorithmReportDataEntity;
import org.recap.model.jpa.MatchingBibInfoDetail;
import org.recap.repository.jpa.MatchingAlgorithmReportDataDetailsRepository;
import org.recap.repository.jpa.MatchingAlgorithmReportDetailRepository;
import org.recap.repository.jpa.MatchingBibInfoDetailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by premkb on 28/1/17.
 */
@Slf4j
@Service
public class MatchingBibInfoDetailService {


    @Autowired
    private MatchingAlgorithmReportDetailRepository matchingAlgorithmReportDetailRepository;

    @Autowired
    private MatchingAlgorithmReportDataDetailsRepository matchingAlgorithmReportDataDetailsRepository ;

    @Autowired
    private MatchingBibInfoDetailRepository matchingBibInfoDetailRepository;

    @Value("${" + PropertyKeyConstants.MATCHING_ALGORITHM_BIBINFO_BATCHSIZE + "}")
    private Integer batchSize;

    public MatchingAlgorithmReportDetailRepository getMatchingAlgorithmReportDetailRepository() {
        return matchingAlgorithmReportDetailRepository;
    }

    public MatchingBibInfoDetailRepository getMatchingBibInfoDetailRepository() {
        return matchingBibInfoDetailRepository;
    }

    public MatchingAlgorithmReportDataDetailsRepository getMatchingAlgorithmReportDataDetailsRepository() {
        return matchingAlgorithmReportDataDetailsRepository;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    /**
     * This method is used to populate matching bib info and save it to MATCHING_BIB_INFO_DETAIL_T for the given from and to date.
     *
     * @param fromDate the from date
     * @param toDate   the to date
     * @return the string
     */
    public String populateMatchingBibInfo(Date fromDate, Date toDate) {
        List<String> typeList = new ArrayList<>();
        typeList.add(ScsbConstants.SINGLE_MATCH);
        typeList.add(ScsbConstants.MULTI_MATCH);
        List<String> headerNameList = new ArrayList<>();
        headerNameList.add(ScsbCommonConstants.BIB_ID);
        headerNameList.add(ScsbCommonConstants.OWNING_INSTITUTION);
        headerNameList.add(ScsbCommonConstants.OWNING_INSTITUTION_BIB_ID);
        Integer matchingCount = getMatchingAlgorithmReportDetailRepository().getCountByTypeAndFileNameAndDateRange(typeList, ScsbCommonConstants.ONGOING_MATCHING_ALGORITHM, fromDate, toDate);
        log.info("matchingReports Count ------> {} ",matchingCount);
        Integer pageCount = getPageCount(matchingCount,getBatchSize());
        log.info("Total pages ---> {}",pageCount);
        StopWatch stopWatchFull = new StopWatch();
        stopWatchFull.start();
        for(int pageNum=0; pageNum<pageCount; pageNum++) {
            log.info("Current page ---> {}", pageNum);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            Page<Integer> recordNumbers = getMatchingAlgorithmReportDetailRepository().getRecordNumByTypeAndFileNameAndDateRange(PageRequest.of(pageNum, getBatchSize()), typeList,
                    ScsbCommonConstants.ONGOING_MATCHING_ALGORITHM, fromDate, toDate);
            List<Integer> recordNumberList = recordNumbers.getContent();
            log.info("recordNumberList size -----> {}", recordNumberList.size());
            List<String> stringList = getStringList(recordNumberList);
            List<MatchingAlgorithmReportDataEntity> reportDataEntityList = getMatchingAlgorithmReportDataDetailsRepository().getRecordsForMatchingBibInfo(stringList,headerNameList);
            Map<String,List<MatchingAlgorithmReportDataEntity>> reportDataEntityMap = getRecordNumReportDataEntityMap(reportDataEntityList);
            List<MatchingBibInfoDetail> matchingBibInfoDetailList = findAndPopulateMatchingBibInfoDetail(reportDataEntityMap);
            getMatchingBibInfoDetailRepository().saveAll(matchingBibInfoDetailList);
            getMatchingBibInfoDetailRepository().flush();
            stopWatch.stop();
            log.info("Time taken to save ---> {}", stopWatch.getTotalTimeSeconds());
            log.info("Page {} saved to db ", pageCount);
        }
        stopWatchFull.stop();
        log.info("Loaded matching bib info in {} seconds", stopWatchFull.getTotalTimeSeconds());
        return "Success";
    }

    /**
     * This method is used to populate matching bib info and save it to MATCHING_BIB_INFO_DETAIL_T
     *
     * @return the string
     */
    public String populateMatchingBibInfo(){
        List<String> typeList = new ArrayList<>();
        typeList.add(ScsbConstants.SINGLE_MATCH);
        typeList.add(ScsbConstants.MULTI_MATCH);
        List<String> headerNameList = new ArrayList<>();
        headerNameList.add(ScsbCommonConstants.BIB_ID);
        headerNameList.add(ScsbCommonConstants.OWNING_INSTITUTION);
        headerNameList.add(ScsbCommonConstants.OWNING_INSTITUTION_BIB_ID);
        Integer matchingCount = getMatchingAlgorithmReportDetailRepository().getCountByType(typeList);
        log.info("matchingCount------> {}", matchingCount);
        Integer pageCount = getPageCount(matchingCount,getBatchSize());
        log.info("pageCount---> {} ", pageCount);
        StopWatch stopWatchFull = new StopWatch();
        stopWatchFull.start();
        for(int count=0;count<pageCount;count++){
            log.info("Current page---> {}", count);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            Page<Integer> recordNumbers = getMatchingAlgorithmReportDetailRepository().getRecordNumByType(PageRequest.of(count, getBatchSize()),typeList);
            List<Integer> recordNumberList = recordNumbers.getContent();
            log.info("recordNumberList size-----> {}", recordNumberList.size());
            List<MatchingAlgorithmReportDataEntity> reportDataEntityList = getMatchingAlgorithmReportDataDetailsRepository().getRecordsForMatchingBibInfo(getStringList(recordNumberList),headerNameList);
            Map<String, List<MatchingAlgorithmReportDataEntity>> reportDataEntityMap = getRecordNumReportDataEntityMap(reportDataEntityList);
            List<MatchingBibInfoDetail> matchingBibInfoDetailList = populateMatchingBibInfoDetail(reportDataEntityMap);
            getMatchingBibInfoDetailRepository().saveAll(matchingBibInfoDetailList);
            getMatchingBibInfoDetailRepository().flush();
            stopWatch.stop();
            log.info("Time taken to save--> {}", stopWatch.getTotalTimeSeconds());
            log.info("Page {} saved to db", count);
        }
        stopWatchFull.stop();
        log.info("Loaded matching bib info in {} seconds", stopWatchFull.getTotalTimeSeconds());
        return "Success";
    }

    /**
     * This method gets page count.
     *
     * @param totalRecordCount the total record count
     * @param batchSize        the batch size
     * @return the int
     */
    public int getPageCount(int totalRecordCount,int batchSize){
        int quotient = totalRecordCount / batchSize;
        int remainder = Integer.valueOf(Long.toString(totalRecordCount)) % (batchSize);
        return remainder == 0 ? quotient : quotient + 1;
    }

    private List<String> getStringList(List<Integer> integerList){
        List<String> stringList = new ArrayList<>();
        for(Integer integer : integerList){
            stringList.add(String.valueOf(integer));
        }
        return stringList;
    }

    private  Map<String,List<MatchingAlgorithmReportDataEntity>> getRecordNumReportDataEntityMap(List<MatchingAlgorithmReportDataEntity> reportDataEntityList){
        Map<String,List<MatchingAlgorithmReportDataEntity>> reportDataEntityMap = new HashMap<>();
        for(MatchingAlgorithmReportDataEntity reportDataEntity:reportDataEntityList){
            if(reportDataEntityMap.containsKey(reportDataEntity.getRecordNum())){
                reportDataEntityMap.get(reportDataEntity.getRecordNum()).add(reportDataEntity);
            }else{
                List<MatchingAlgorithmReportDataEntity> reportDataEntityListForRowNum = new ArrayList<>();
                reportDataEntityListForRowNum.add(reportDataEntity);
                reportDataEntityMap.put(reportDataEntity.getRecordNum(),reportDataEntityListForRowNum);
            }
        }
        return reportDataEntityMap;
    }

    private List<MatchingBibInfoDetail> populateMatchingBibInfoDetail(Map<String, List<MatchingAlgorithmReportDataEntity>> reportDataEntityMap){
        List<MatchingBibInfoDetail> matchingBibInfoDetailList = new ArrayList<>();
        for(Map.Entry<String,List<MatchingAlgorithmReportDataEntity>> entry:reportDataEntityMap.entrySet()){
            Map<String, String[]> dataArrayMap = populateDataArrays(entry.getValue());
            String[] bibIdArray = dataArrayMap.get(ScsbCommonConstants.BIB_ID);
            String[] institutionArray = dataArrayMap.get(ScsbCommonConstants.OWNING_INSTITUTION);
            String[] owningInstitutionBibIdArray = dataArrayMap.get(ScsbCommonConstants.OWNING_INSTITUTION_BIB_ID);
            for(int count=0;count<bibIdArray.length;count++){
                MatchingBibInfoDetail matchingBibInfoDetail = new MatchingBibInfoDetail();
                matchingBibInfoDetail.setBibId(bibIdArray[count]);
                matchingBibInfoDetail.setOwningInstitution(institutionArray[count]);
                matchingBibInfoDetail.setOwningInstitutionBibId(owningInstitutionBibIdArray[count]);
                matchingBibInfoDetail.setRecordNum(Integer.valueOf(entry.getKey()));
                matchingBibInfoDetailList.add(matchingBibInfoDetail);
            }
        }
        return matchingBibInfoDetailList;
    }

    /**
     * This method is used to find and populate the matching bib information which is to be saved in database.
     * @param reportDataEntityMap
     * @return
     */
    private List<MatchingBibInfoDetail> findAndPopulateMatchingBibInfoDetail(Map<String, List<MatchingAlgorithmReportDataEntity>> reportDataEntityMap){
        List<MatchingBibInfoDetail> matchingBibInfoDetailList = new ArrayList<>();
        for(Map.Entry<String, List<MatchingAlgorithmReportDataEntity>> entry:reportDataEntityMap.entrySet()){
            Integer recordNum = Integer.valueOf(entry.getKey());
            Map<String, String[]> dataArrayMap = populateDataArrays(entry.getValue());
            String[] bibIdArray = dataArrayMap.get(ScsbCommonConstants.BIB_ID);
            String[] institutionArray = dataArrayMap.get(ScsbCommonConstants.OWNING_INSTITUTION);
            String[] owningInstitutionBibIdArray = dataArrayMap.get(ScsbCommonConstants.OWNING_INSTITUTION_BIB_ID);

            matchingBibInfoDetailList.addAll(getMatchingBibInfoDetailsToBeSaved(recordNum, bibIdArray, institutionArray, owningInstitutionBibIdArray));
        }
        return matchingBibInfoDetailList;
    }

    private List<MatchingBibInfoDetail> getMatchingBibInfoDetailsToBeSaved(Integer recordNum, String[] bibIdArray, String[] institutionArray, String[] owningInstitutionBibIdArray) {
        List<MatchingBibInfoDetail> matchingBibInfoDetailList = new ArrayList<>();
        List<String> addedBibIds = new ArrayList<>();
        List<Integer> recordNums = getMatchingBibInfoDetailRepository().findRecordNumByBibIds(Arrays.asList(bibIdArray));
        List<MatchingBibInfoDetail> matchingBibInfoDetails = getMatchingBibInfoDetailRepository().findByRecordNumIn(recordNums);
        if(CollectionUtils.isNotEmpty(matchingBibInfoDetails)) {
            for(MatchingBibInfoDetail matchingBibInfoDetail : matchingBibInfoDetails) {
                addedBibIds.add(matchingBibInfoDetail.getBibId());
                matchingBibInfoDetail.setRecordNum(recordNum);
                matchingBibInfoDetailList.add(matchingBibInfoDetail);
            }
        }
        for(int count=0;count<bibIdArray.length;count++){
            String bibId = bibIdArray[count];
            if(!addedBibIds.contains(bibId)) {
                MatchingBibInfoDetail matchingBibInfoDetail = new MatchingBibInfoDetail();
                matchingBibInfoDetail.setBibId(bibId);
                matchingBibInfoDetail.setOwningInstitution(institutionArray[count]);
                matchingBibInfoDetail.setOwningInstitutionBibId(owningInstitutionBibIdArray[count]);
                matchingBibInfoDetail.setRecordNum(recordNum);
                matchingBibInfoDetailList.add(matchingBibInfoDetail);
            }
        }
        return matchingBibInfoDetailList;
    }

    private Map<String, String[]> populateDataArrays(List<MatchingAlgorithmReportDataEntity> reportDataEntities) {
        Map<String, String[]> dataArrayMap = new HashMap<>();
        for(MatchingAlgorithmReportDataEntity reportDataEntity : reportDataEntities) {
            if (ScsbCommonConstants.BIB_ID.equals(reportDataEntity.getHeaderName())) {
                dataArrayMap.put(ScsbCommonConstants.BIB_ID, reportDataEntity.getHeaderValue().split(","));
            } else if (ScsbCommonConstants.OWNING_INSTITUTION.equals(reportDataEntity.getHeaderName())) {
                dataArrayMap.put(ScsbCommonConstants.OWNING_INSTITUTION, reportDataEntity.getHeaderValue().split(","));
            } else if (ScsbCommonConstants.OWNING_INSTITUTION_BIB_ID.equals(reportDataEntity.getHeaderName())) {
                dataArrayMap.put(ScsbCommonConstants.OWNING_INSTITUTION_BIB_ID, reportDataEntity.getHeaderValue().split(","));
            }
        }
        return dataArrayMap;
    }

}
