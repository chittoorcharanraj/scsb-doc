package org.recap.report;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.csv.AccessionSummaryRecord;
import org.recap.model.csv.SolrExceptionReportCSVRecord;
import org.recap.model.csv.SubmitCollectionReportRecord;
import org.recap.model.jpa.ReportEntity;
import org.recap.util.AccessionSummaryRecordGenerator;
import org.recap.util.SolrExceptionCSVRecordGenerator;
import org.recap.util.SubmitCollectionReportGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Slf4j
public class CommonReportGenerator {



    @Autowired
    private ProducerTemplate producerTemplate;

    public String generateSubmitCollectionReportFile(String fileName, List<ReportEntity> reportEntityList, String reportQueue) {
        String generatedFileName;
        String[] fileNameSplit = null;
        List<SubmitCollectionReportRecord> submitCollectionReportRecordList = new ArrayList<>();
        SubmitCollectionReportGenerator submitCollectionReportGenerator = new SubmitCollectionReportGenerator();
        for (ReportEntity reportEntity : reportEntityList) {
            List<SubmitCollectionReportRecord> submitCollectionReportRecords = submitCollectionReportGenerator.prepareSubmitCollectionRejectionRecord(reportEntity);
            submitCollectionReportRecordList.addAll(submitCollectionReportRecords);
        }
        DateFormat df = new SimpleDateFormat(ScsbConstants.DATE_FORMAT_FOR_FILE_NAME);

        Predicate<String> checkForProtectionOrNotProtectionKeyword = p-> p.contains(ScsbConstants.PROTECTED) || p.contains(ScsbConstants.NOT_PROTECTED);
        if (checkForProtectionOrNotProtectionKeyword.test(fileName)) {
            fileNameSplit = fileName.split("/", 5);
            generatedFileName = ScsbConstants.SUBMIT_COLLECTION_REPORTS_BASE_PATH + fileNameSplit[2] + ScsbCommonConstants.PATH_SEPARATOR +fileNameSplit[3] + ScsbCommonConstants.PATH_SEPARATOR+ ScsbCommonConstants.SUBMIT_COLLECTION_REPORT + "-" + fileNameSplit[4] + "-" + df.format(new Date()) + ".csv";
        } else {
            generatedFileName = fileName + "-" + df.format(new Date()) + ".csv";
        }
        if (StringUtils.containsIgnoreCase(reportQueue, ScsbConstants.SUBMIT_COLLECTION_SUMMARY_Q_SUFFIX)) {
            fileName = generatedFileName;
        }
        if (checkForProtectionOrNotProtectionKeyword.test(fileName)  && Objects.requireNonNull(fileNameSplit)[2] != null && Objects.requireNonNull(fileNameSplit)[3] != null && Objects.requireNonNull(fileNameSplit)[4] != null) {
            producerTemplate.sendBodyAndHeader(reportQueue, submitCollectionReportRecordList, ScsbConstants.FILE_NAME, fileNameSplit[2] + ScsbCommonConstants.PATH_SEPARATOR + fileNameSplit[3] + ScsbCommonConstants.PATH_SEPARATOR + ScsbCommonConstants.SUBMIT_COLLECTION_REPORT + "-" + fileNameSplit[4] + "-" + df.format(new Date()) + ".csv");
        } else {
            producerTemplate.sendBodyAndHeader(reportQueue, submitCollectionReportRecordList, ScsbConstants.FILE_NAME, fileName);
        }

        return generatedFileName;
    }

    public String generateAccessionReportFile(String fileName, List<ReportEntity> reportEntityList, String reportQueue) {
        String generatedFileName;
        List<AccessionSummaryRecord> accessionSummaryRecordList;
        AccessionSummaryRecordGenerator accessionSummaryRecordGenerator = new AccessionSummaryRecordGenerator();
        accessionSummaryRecordList = accessionSummaryRecordGenerator.prepareAccessionSummaryReportRecord(reportEntityList);
        producerTemplate.sendBodyAndHeader(reportQueue, accessionSummaryRecordList, ScsbConstants.FILE_NAME, fileName);
        DateFormat df = new SimpleDateFormat(ScsbCommonConstants.DATE_FORMAT_FOR_FILE_NAME);
        generatedFileName = fileName + "-" + df.format(new Date()) + ".csv";
        return generatedFileName;
    }

    public String generateReportForSolrExceptionCsvRecords(String fileName, String queueName, List<ReportEntity> reportEntityList) {
        List<SolrExceptionReportCSVRecord> solrExceptionReportCSVRecords = getSolrExceptionReportReCAPCSVRecords(reportEntityList);
        log.info("Total Num of CSVRecords Prepared : {}  ", solrExceptionReportCSVRecords.size());

        if (!CollectionUtils.isEmpty(solrExceptionReportCSVRecords)) {
            producerTemplate.sendBodyAndHeader(queueName, solrExceptionReportCSVRecords, ScsbCommonConstants.REPORT_FILE_NAME, fileName);
            DateFormat df = new SimpleDateFormat(ScsbConstants.DATE_FORMAT_FOR_FILE_NAME);
            return fileName + "-" + df.format(new Date()) + ".csv";
        }
        return null;
    }

    public List<SolrExceptionReportCSVRecord> getSolrExceptionReportReCAPCSVRecords(List<ReportEntity> reportEntityList) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<SolrExceptionReportCSVRecord> solrExceptionReportCSVRecords = new ArrayList<>();

        SolrExceptionCSVRecordGenerator solrExceptionCSVRecordGenerator = new SolrExceptionCSVRecordGenerator();
        for (ReportEntity reportEntity : reportEntityList) {
            SolrExceptionReportCSVRecord solrExceptionReportCSVRecord = solrExceptionCSVRecordGenerator.prepareSolrExceptionReportCSVRecord(reportEntity, new SolrExceptionReportCSVRecord());
            solrExceptionReportCSVRecords.add(solrExceptionReportCSVRecord);
        }

        stopWatch.stop();
        log.info("Total time taken to prepare CSVRecords : {} ", stopWatch.getTotalTimeSeconds());
        return solrExceptionReportCSVRecords;
    }
}
