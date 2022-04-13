package org.recap.util;

import com.csvreader.CsvWriter;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.model.matchingreports.TitleExceptionReport;
import org.recap.model.reports.TitleMatchCount;
import org.recap.model.reports.TitleMatchedReport;
import org.recap.model.reports.TitleMatchedReports;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CsvUtilUT extends BaseTestCaseUT {

    @InjectMocks
    CsvUtil csvUtil;

    @Mock
    ReportsServiceUtil reportsServiceUtil;

    @Test
    public void createTitleExceptionReportFile()throws Exception {
        TitleExceptionReport titleExceptionReport = new TitleExceptionReport();
        titleExceptionReport.setTitleList(Arrays.asList("test"));
        List<TitleExceptionReport> titleExceptionReports = new ArrayList<>();
        titleExceptionReports.add(titleExceptionReport);
        File file = csvUtil.createTitleExceptionReportFile("test", 1, titleExceptionReports);
        assertNotNull(file);
    }

        @Test
    public void createTitleMatchReportFile()throws Exception {
           List<TitleMatchedReports> report = new ArrayList<>();
            TitleMatchedReports titleMatchedReports = new TitleMatchedReports();
            titleMatchedReports.setOwningInstitution("PUL");
            titleMatchedReports.setBibId("1");
            titleMatchedReports.setScsbId(2);
            titleMatchedReports.setItemBarcode("12345");
            titleMatchedReports.setLccn("1234");
            titleMatchedReports.setOclc("7654");
            titleMatchedReports.setCgd("open");
            titleMatchedReports.setDuplicateCode("test");
            titleMatchedReports.setIssn("12345");
            titleMatchedReports.setIsbn("8765");
            titleMatchedReports.setTitle("Book");
            titleMatchedReports.setAnamolyFlag("Y");
            titleMatchedReports.setChronologyAndEnum("test");
            titleMatchedReports.setMatchScore("test");
            titleMatchedReports.setMatchScoreTranslated("test");
            titleMatchedReports.setPublisher("test");
            titleMatchedReports.setPublicationDate("");
            report.add(titleMatchedReports);
            List<TitleMatchCount> titleMatchCounts = new ArrayList<>();
        TitleMatchCount titleMatchCount = new TitleMatchCount();
        titleMatchCount.setTitleMatched("Matched");
        titleMatchCounts.add(0,titleMatchCount);
            TitleMatchedReport titleMatchedReport = new TitleMatchedReport();
            titleMatchedReport.setReportMessage("test");
            titleMatchedReport.setTitleMatch("Matched");
            titleMatchedReport.setTotalRecordsCount(10l);
            titleMatchedReport.setTotalPageCount(5);
            titleMatchedReport.setOwningInst("PUL");
            titleMatchedReport.setPageNumber(3);
            titleMatchedReport.setTitleMatchedReports(report);
            titleMatchedReport.setTitleMatchCounts(titleMatchCounts);
            Mockito.when(reportsServiceUtil.getTitleMatchedReportsExportS3(titleMatchedReport)).thenReturn(titleMatchedReport);
            csvUtil.createTitleMatchReportFile("test", titleMatchedReport);

        }
    @Test
    public void writeDataRowForTitleMatchExportReport() throws Exception
    {
        TitleMatchedReports titleMatchedReports = new TitleMatchedReports();
        titleMatchedReports.setOwningInstitution("PUL");
        titleMatchedReports.setBibId("1");
        titleMatchedReports.setScsbId(2);
        titleMatchedReports.setItemBarcode("12345");
        titleMatchedReports.setLccn("1234");
        titleMatchedReports.setOclc("7654");
        titleMatchedReports.setCgd("open");
        titleMatchedReports.setDuplicateCode("test");
        titleMatchedReports.setIssn("12345");
        titleMatchedReports.setIsbn("8765");
        titleMatchedReports.setTitle("Book");
        titleMatchedReports.setAnamolyFlag("Y");
        titleMatchedReports.setChronologyAndEnum("test");
        titleMatchedReports.setMatchScore("test");
        titleMatchedReports.setMatchScoreTranslated("test");
        titleMatchedReports.setPublisher("test");
        titleMatchedReports.setPublicationDate("");
        CsvWriter csvOutput = new CsvWriter("test");
       csvUtil.writeDataRowForTitleMatchExportReport(titleMatchedReports,csvOutput);

    } @Test
    public void writeHeaderRowForTitleMatchReport() throws Exception
    {
        TitleMatchedReports titleMatchedReports = new TitleMatchedReports();
        titleMatchedReports.setOwningInstitution("PUL");
        titleMatchedReports.setBibId("1");
        titleMatchedReports.setScsbId(2);
        titleMatchedReports.setItemBarcode("12345");
        titleMatchedReports.setLccn("1234");
        titleMatchedReports.setOclc("7654");
        titleMatchedReports.setCgd("open");
        titleMatchedReports.setDuplicateCode("test");
        titleMatchedReports.setIssn("12345");
        titleMatchedReports.setIsbn("8765");
        titleMatchedReports.setTitle("Book");
        titleMatchedReports.setAnamolyFlag("Y");
        titleMatchedReports.setChronologyAndEnum("test");
        titleMatchedReports.setMatchScore("test");
        titleMatchedReports.setMatchScoreTranslated("test");
        titleMatchedReports.setPublisher("test");
        titleMatchedReports.setPublicationDate("");
        CsvWriter csvOutput = new CsvWriter("test");
       csvUtil.writeHeaderRowForTitleMatchReport(csvOutput);

    }
}
