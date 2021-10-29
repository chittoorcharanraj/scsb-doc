package org.recap.controller;

import lombok.extern.slf4j.Slf4j;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.matchingalgorithm.service.MatchingBibInfoDetailService;
import org.recap.model.solr.SolrIndexRequest;
import org.recap.service.OngoingMatchingAlgorithmService;
import org.recap.util.DateUtil;
import org.recap.util.OngoingMatchingAlgorithmUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rajeshbabuk on 20/4/17.
 */
@Slf4j
@RefreshScope
@RestController
@RequestMapping("/ongoingMatchingAlgorithmService")
public class OngoingMatchingAlgorithmJobRestController {

    @Autowired
    private MatchingBibInfoDetailService matchingBibInfoDetailService;

    @Autowired
    private OngoingMatchingAlgorithmService ongoingMatchingAlgorithmService;

    @Autowired
    private OngoingMatchingAlgorithmUtil ongoingMatchingAlgorithmUtil;

    @Autowired
    DateUtil dateUtil;

    @Value("${" + PropertyKeyConstants.MATCHING_ALGORITHM_BIBINFO_BATCHSIZE + "}")
    private String batchSize;

    @Value("${" + PropertyKeyConstants.RUN_ONGOING_MA_GROUPING_PROCESS + "}")
    private boolean runOngoingMaGroupingProcess;

    @PostMapping(value = "/ongoingMatchingAlgorithmJob", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String startMatchingAlgorithmJob(@RequestBody SolrIndexRequest solrIndexRequest) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Date date = solrIndexRequest.getCreatedDate();
        solrIndexRequest.setFromDate(new SimpleDateFormat(ScsbConstants.ONGOING_MATCHING_DATE_FORMAT).format(date));
        solrIndexRequest.setMaProcessType(ScsbConstants.ONGOING_MA_BOTH_GROUPING_CGD_PROCESS);
        solrIndexRequest.setIncludeMaQualifier(true);
        solrIndexRequest.setIndexBibsForOngoingMa(true);
        String status = "";
        Integer rows = Integer.valueOf(batchSize);
        try {
            status = processCgdUpdatesForOngoingMatchingAlgorithm(solrIndexRequest, rows);
            if (runOngoingMaGroupingProcess) {
                status = processGroupingForOngoingMatchingAlgorithm(solrIndexRequest, rows);
            }
            if (ScsbCommonConstants.SUCCESS.equalsIgnoreCase(status)) {
                status = matchingBibInfoDetailService.populateMatchingBibInfo(dateUtil.getFromDate(date), dateUtil.getToDate(date));
            }
        } catch (Exception e) {
            log.error("Exception : {0}", e);
        }
        stopWatch.stop();
        log.info("Total Time taken to complete Ongoing Matching Algorithm : {}", stopWatch.getTotalTimeSeconds());
        return status;
    }

    private String processGroupingForOngoingMatchingAlgorithm(SolrIndexRequest solrIndexRequest, Integer rows) throws Exception {
        StopWatch stopWatchForGrouping = new StopWatch();
        stopWatchForGrouping.start();
        String status = ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartGroupingProcessBasedOnCriteria(solrIndexRequest, rows);
        stopWatchForGrouping.stop();
        log.info("{}{}{}", ScsbConstants.TOTAL_TIME_TAKEN, "for Grouping Bibs: ", stopWatchForGrouping.getTotalTimeSeconds());
        return status;
    }

    private String processCgdUpdatesForOngoingMatchingAlgorithm(SolrIndexRequest solrIndexRequest, Integer rows) throws Exception {
        StopWatch stopWatchForCgdProcess = new StopWatch();
        stopWatchForCgdProcess.start();
        String status = ongoingMatchingAlgorithmUtil.fetchUpdatedRecordsAndStartCgdUpdateProcessBasedOnCriteria(solrIndexRequest, rows);
        stopWatchForCgdProcess.stop();
        log.info("{}{}{}", ScsbConstants.TOTAL_TIME_TAKEN, "for Updating CGD : ", stopWatchForCgdProcess.getTotalTimeSeconds());
        return status;
    }

    @GetMapping("/generateCGDRoundTripReport")
    public String generateCGDRoundTripReport() {
        return ongoingMatchingAlgorithmService.generateCGDRoundTripReport();
    }
}
