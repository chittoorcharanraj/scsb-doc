package org.recap.report;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.csv.OngoingAccessionReportRecord;
import org.recap.model.jpa.ReportEntity;
import org.recap.util.OngoingAccessionReportGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by premkb on 07/02/17.
 */
@Component
public class S3OngoingAccessionReportGenerator implements ReportGeneratorInterface {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Override
    public boolean isInterested(String reportType) {
        return reportType.equalsIgnoreCase(ScsbConstants.ONGOING_ACCESSION_REPORT);
    }

    @Override
    public boolean isTransmitted(String transmissionType) {
        return transmissionType.equalsIgnoreCase(ScsbCommonConstants.FTP);
    }

    @Override
    public String generateReport(String fileName, List<ReportEntity> reportEntityList) {
        String generatedFileName;
        List<OngoingAccessionReportRecord> ongoingAccessionReportRecordList = new ArrayList<>();
        OngoingAccessionReportGenerator ongoingAccessionReportGenerator = new OngoingAccessionReportGenerator();
        for(ReportEntity reportEntity : reportEntityList) {
            ongoingAccessionReportRecordList.add(ongoingAccessionReportGenerator.prepareOngoingAccessionReportRecord(reportEntity));
        }
        if(CollectionUtils.isNotEmpty(ongoingAccessionReportRecordList)) {
            Map<String, Object>  accessionMap = new HashMap<>();
            accessionMap.put(ScsbConstants.FILE_NAME, reportEntityList.get(0).getInstitutionName()+ "/"+ fileName);
            accessionMap.put(ScsbConstants.INSTITUTION_NAME, reportEntityList.get(0).getInstitutionName());
            producerTemplate.sendBodyAndHeaders(ScsbConstants.FTP_ONGOING_ACCESSON_REPORT_Q, ongoingAccessionReportRecordList, accessionMap);

            DateFormat df = new SimpleDateFormat(ScsbConstants.DATE_FORMAT_FOR_REPORT_FILE_NAME);
            generatedFileName = fileName + "-" + df.format(new Date()) + ".csv";
            return generatedFileName;
        }
        return null;
    }
}
