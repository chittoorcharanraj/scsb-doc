package org.recap.matchingalgorithm.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.jpa.MatchingAlgorithmReportDataEntity;
import org.recap.model.jpa.MatchingBibInfoDetail;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.repository.jpa.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by premkb on 29/1/17.
 */

public class MatchingBibInfoDetailServiceUT extends BaseTestCaseUT {

    @Mock
    private MatchingBibInfoDetailService matchingBibInfoDetailService;

    @Mock
    private MatchingAlgorithmReportDetailRepository reportDetailRepository;

    @Mock
    private MatchingBibInfoDetailRepository matchingBibInfoDetailRepository;

    @Mock
    private MatchingAlgorithmReportDataDetailsRepository reportDataDetailsRepository;

    private Integer batchSize=1000;


    @Test
    public void populateMatchingBibInfo(){
        List<String> typeList = new ArrayList<>();
        typeList.add(ScsbConstants.SINGLE_MATCH);
        typeList.add(ScsbConstants.MULTI_MATCH);
        List<String> headerNameList = new ArrayList<>();
        headerNameList.add(ScsbCommonConstants.BIB_ID);
        headerNameList.add(ScsbCommonConstants.OWNING_INSTITUTION);
        headerNameList.add(ScsbCommonConstants.OWNING_INSTITUTION_BIB_ID);
        List<MatchingAlgorithmReportDataEntity> reportDataEntityList = new ArrayList<>();
        reportDataEntityList.add(0,getReportDataEntity("1","BibId","1"));
        reportDataEntityList.add(1,getReportDataEntity("1","OwningInstitution","1"));
        reportDataEntityList.add(2,getReportDataEntity("1","OwningInstitutionBibId","Ad4654564"));
        MatchingBibInfoDetail matchingBibInfoDetail = new MatchingBibInfoDetail();
        matchingBibInfoDetail.setBibId("1234");
        Date fromDate = new Date();
        Date toDate = new Date();
        int pageNum = 0;
        int count = 0;
        int matchingCount = batchSize;
        Mockito.when(matchingBibInfoDetailService.getMatchingAlgorithmReportDetailRepository()).thenReturn(reportDetailRepository);
        Mockito.when(matchingBibInfoDetailService.getMatchingAlgorithmReportDataDetailsRepository()).thenReturn(reportDataDetailsRepository);
        Mockito.when(matchingBibInfoDetailService.getMatchingBibInfoDetailRepository()).thenReturn(matchingBibInfoDetailRepository);
        Mockito.when(matchingBibInfoDetailService.getBatchSize()).thenReturn(batchSize);
        Mockito.when(matchingBibInfoDetailService.getPageCount(matchingCount,batchSize)).thenCallRealMethod();
        Mockito.when(matchingBibInfoDetailService.getMatchingAlgorithmReportDetailRepository().getCountByType(typeList)).thenReturn(batchSize);
        Mockito.when(matchingBibInfoDetailService.getMatchingAlgorithmReportDetailRepository().getCountByTypeAndFileNameAndDateRange(typeList, ScsbCommonConstants.ONGOING_MATCHING_ALGORITHM, fromDate, toDate)).thenReturn(batchSize);
        Mockito.when(matchingBibInfoDetailService.getMatchingAlgorithmReportDetailRepository().getRecordNumByTypeAndFileNameAndDateRange(PageRequest.of(pageNum, batchSize), typeList, ScsbCommonConstants.ONGOING_MATCHING_ALGORITHM, fromDate, toDate)).thenReturn(getRecordNumber());
        Mockito.when(matchingBibInfoDetailService.getMatchingAlgorithmReportDetailRepository().getRecordNumByType(PageRequest.of(count, batchSize),typeList)).thenReturn(getRecordNumber());
        Mockito.when(matchingBibInfoDetailService.getMatchingAlgorithmReportDataDetailsRepository().getRecordsForMatchingBibInfo(Mockito.any(),Mockito.any())).thenReturn(reportDataEntityList);
        Mockito.when(matchingBibInfoDetailService.getMatchingBibInfoDetailRepository().findRecordNumByBibIds(Mockito.any())).thenReturn(new ArrayList<Integer>(1));
        Mockito.when(matchingBibInfoDetailService.getMatchingBibInfoDetailRepository().findByRecordNumIn(Mockito.any())).thenReturn(Arrays.asList(matchingBibInfoDetail));
        Mockito.when(matchingBibInfoDetailService.populateMatchingBibInfo(fromDate,toDate)).thenCallRealMethod();
        String respone  = matchingBibInfoDetailService.populateMatchingBibInfo(fromDate,toDate);
        assertNotNull(respone);
        assertEquals(ScsbCommonConstants.SUCCESS,respone);
        Mockito.when(matchingBibInfoDetailService.populateMatchingBibInfo()).thenCallRealMethod();
        String response = matchingBibInfoDetailService.populateMatchingBibInfo();
        assertNotNull(response);
    }

    private MatchingAlgorithmReportDataEntity getReportDataEntity(String num, String name, String value) {
        MatchingAlgorithmReportDataEntity reportDataEntity = new MatchingAlgorithmReportDataEntity();
        reportDataEntity.setRecordNum(num);
        reportDataEntity.setHeaderName(name);
        reportDataEntity.setHeaderValue(value);
        return reportDataEntity;
    }

    @Test
    public void checkGetterServices(){
        Mockito.when(matchingBibInfoDetailService.getBatchSize()).thenCallRealMethod();
        Mockito.when(matchingBibInfoDetailService.getMatchingBibInfoDetailRepository()).thenCallRealMethod();
        Mockito.when(matchingBibInfoDetailService.getMatchingAlgorithmReportDataDetailsRepository()).thenCallRealMethod();
        Mockito.when(matchingBibInfoDetailService.getMatchingAlgorithmReportDetailRepository()).thenCallRealMethod();
        assertNotEquals(batchSize,matchingBibInfoDetailService.getBatchSize());
        assertNotEquals(matchingBibInfoDetailRepository,matchingBibInfoDetailService.getMatchingBibInfoDetailRepository());
        assertNotEquals(reportDataDetailsRepository,matchingBibInfoDetailService.getMatchingAlgorithmReportDataDetailsRepository());
        assertNotEquals(reportDetailRepository,matchingBibInfoDetailService.getMatchingAlgorithmReportDetailRepository());
    }

    public Page<Integer> getRecordNumber(){
        Page<Integer> recordNumber = new Page<Integer>() {
            @Override
            public int getTotalPages() {
                return 0;
            }

            @Override
            public long getTotalElements() {
                return 0;
            }

            @Override
            public <U> Page<U> map(Function<? super Integer, ? extends U> converter) {
                return null;
            }

            @Override
            public int getNumber() {
                return 0;
            }

            @Override
            public int getSize() {
                return 0;
            }

            @Override
            public int getNumberOfElements() {
                return 0;
            }

            @Override
            public List<Integer> getContent() {
                return (Arrays.asList(1));
            }

            @Override
            public boolean hasContent() {
                return false;
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public Pageable nextPageable() {
                return null;
            }

            @Override
            public Pageable previousPageable() {
                return null;
            }

            @Override
            public Iterator<Integer> iterator() {
                return null;
            }
        };
        return recordNumber;
    }
}
