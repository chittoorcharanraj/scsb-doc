package org.recap.report;

import org.recap.ScsbCommonConstants;
import org.recap.model.jpa.ReportEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by angelind on 30/9/16.
 */
@Component
public class CSVSolrExceptionReportGenerator extends CommonReportGenerator implements ReportGeneratorInterface{

    @Override
    public boolean isInterested(String reportType) {
        return reportType.equalsIgnoreCase(ScsbCommonConstants.SOLR_INDEX_EXCEPTION);
    }

    @Override
    public boolean isTransmitted(String transmissionType) {
        return transmissionType.equalsIgnoreCase(ScsbCommonConstants.FILE_SYSTEM);
    }

    @Override
    public String generateReport(String fileName, List<ReportEntity> reportEntityList) {
        return generateReportForSolrExceptionCsvRecords(fileName, ScsbCommonConstants.CSV_SOLR_EXCEPTION_REPORT_Q, reportEntityList);
    }
}
