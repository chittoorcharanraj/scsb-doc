package org.recap.controller;

import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.matchingalgorithm.service.MatchingBibInfoDetailService;
import org.recap.model.solr.SolrIndexRequest;
import org.recap.util.DateUtil;
import org.recap.util.OngoingMatchingAlgorithmUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by angelind on 16/3/17.
 */
@Controller
public class OngoingMatchingAlgorithmJobController {

    private static final Logger logger = LoggerFactory.getLogger(OngoingMatchingAlgorithmJobController.class);

    @Autowired
    private OngoingMatchingAlgorithmUtil ongoingMatchingAlgorithmUtil;

    @Autowired
    private MatchingBibInfoDetailService matchingBibInfoDetailService;

    @Autowired
    private DateUtil dateUtil;

    @Value("${" + PropertyKeyConstants.MATCHING_ALGORITHM_BIBINFO_BATCHSIZE + "}")
    private String batchSize;

    public Logger getLogger() {
        return logger;
    }

    public OngoingMatchingAlgorithmUtil getOngoingMatchingAlgorithmUtil() {
        return ongoingMatchingAlgorithmUtil;
    }

    public MatchingBibInfoDetailService getMatchingBibInfoDetailService() {
        return matchingBibInfoDetailService;
    }

    public DateUtil getDateUtil() {
        return dateUtil;
    }

    public String getBatchSize() {
        return batchSize;
    }

    @RequestMapping(value = "/ongoingMatchingJob")
    public String matchingJob(Model model) {
        model.addAttribute("matchingJobFromDate", new Date());
        return "ongoingMatchingJob";
    }

    @ResponseBody
    @PostMapping(value = "/ongoingMatchingJob")
    public String startMatchingAlgorithmJob(@Valid @ModelAttribute("solrIndexRequest") SolrIndexRequest solrIndexRequest) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String jobType = solrIndexRequest.getProcessType();
        String status = "";
        try {
            logger.info("Process Type : {}", jobType);
            if (jobType.equalsIgnoreCase(ScsbConstants.POPULATE_DATA_FOR_DATA_DUMP_JOB)) {
                Date date = new SimpleDateFormat(ScsbConstants.ONGOING_MATCHING_DATE_FORMAT).parse(solrIndexRequest.getFromDate());
                status = getMatchingBibInfoDetailService().populateMatchingBibInfo(getDateUtil().getFromDate(date), getDateUtil().getToDate(date));
            } else if (jobType.equalsIgnoreCase(ScsbCommonConstants.ONGOING_MATCHING_ALGORITHM_JOB)) {
                String maProcessType = solrIndexRequest.getMaProcessType();
                String matchBy = solrIndexRequest.getMatchBy();
                boolean includeMaQualifier = solrIndexRequest.isIncludeMaQualifier();
                boolean indexBibs = solrIndexRequest.isIndexBibsForOngoingMa();
                Integer rows = Integer.valueOf(getBatchSize());
                logger.info("MA Process Type : {}, Match By : {}, Batch Size : {}, Include MA Qualifier : {}, Index Bibs : {}", maProcessType, matchBy, rows, includeMaQualifier, indexBibs);
                if (maProcessType.equalsIgnoreCase(ScsbConstants.ONGOING_MA_BOTH_GROUPING_CGD_PROCESS)) {
                    status = getOngoingMatchingAlgorithmUtil().fetchUpdatedRecordsAndStartCgdUpdateProcessBasedOnCriteria(solrIndexRequest, rows);
                    status = getOngoingMatchingAlgorithmUtil().fetchUpdatedRecordsAndStartGroupingProcessBasedOnCriteria(solrIndexRequest, rows);
                } else if (maProcessType.equalsIgnoreCase(ScsbConstants.ONGOING_MA_UPDATE_CGD_PROCESS)) {
                    status = getOngoingMatchingAlgorithmUtil().fetchUpdatedRecordsAndStartCgdUpdateProcessBasedOnCriteria(solrIndexRequest, rows);
                } else if (maProcessType.equalsIgnoreCase(ScsbConstants.ONGOING_MA_ONLY_GROUPING)) {
                    status = getOngoingMatchingAlgorithmUtil().fetchUpdatedRecordsAndStartGroupingProcessBasedOnCriteria(solrIndexRequest, rows);
                }
            }
        } catch (Exception e) {
            logger.error("Exception : {0}", e);
        }
        stopWatch.stop();
        getLogger().info("Total Time taken to complete Ongoing Matching Algorithm : {}", stopWatch.getTotalTimeSeconds());
        return status;
    }
}
