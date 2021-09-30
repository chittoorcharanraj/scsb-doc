package org.recap.service;

import com.amazonaws.services.s3.AmazonS3;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.recap.BaseTestCaseUT;
import org.recap.ScsbConstants;
import org.recap.model.jpa.ReportEntity;
import org.recap.model.reports.TitleMatchedReport;
import org.recap.model.reports.TitleMatchedReports;
import org.recap.report.ReportGenerator;
import org.recap.util.ReportsServiceUtil;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.apache.tomcat.util.http.FastHttpDateFormat.getCurrentDate;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

public class TitleMatchReportExportServiceUT extends BaseTestCaseUT {

    @InjectMocks
    TitleMatchReportExportService titleMatchReportExportService;

    @Mock
    Row row;

    @Mock
    Cell cell;

    @Mock
    ReportsServiceUtil reportsServiceUtil;

    @Mock
    TitleMatchedReport titleMatchedReport;

    @Mock
    ReportGenerator reportGenerator;

    @Mock
    FileOutputStream fileOutputStream;

    @Mock
    TitleMatchedReports titleMatchedReports;

    @Mock
    File File;

    @Mock
    AmazonS3 amazonS3;


    @Test
    public void writeTitleMatchReport() {

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
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("TEST");
        Row headerRow = sheet.createRow(0);
        ReflectionTestUtils.invokeMethod(titleMatchReportExportService, "writeTitleMatchReport", titleMatchedReports, headerRow);
    }


    @Test
    public void process() throws Exception {
        TitleMatchedReport titleMatchedReport = new TitleMatchedReport();
        titleMatchedReport.setOwningInst("PUL");
        titleMatchedReport.setTotalPageCount(1);
        String filename = "test";
        String institution_name = "PUL";
        int fileCount = 1;
        Mockito.when(reportsServiceUtil.getTitleMatchedReportsExportS3(any())).thenReturn(titleMatchedReport);
        ReflectionTestUtils.setField(titleMatchReportExportService,"titleReportDir","test");
        ReflectionTestUtils.setField(titleMatchReportExportService,"s3Client",amazonS3);
        ReflectionTestUtils.setField(titleMatchReportExportService,"s3BucketName","test");
        try {
            ReflectionTestUtils.invokeMethod(titleMatchReportExportService, "process", titleMatchedReport);
        }catch (Exception e){}



    }

    @Test
    public void createFileName() throws Exception{
        String owningInst = "PUL";
        int fileCount = 5;
        Integer fileNumber = 1;
     ReflectionTestUtils.invokeMethod(titleMatchReportExportService,"createFileName","PUL",5,1);

    }

    @Test
    public void prepareWorkbook() throws Exception
    {
        TitleMatchedReport titleMatchedReport = new TitleMatchedReport();
        TitleMatchedReports titleMatchedReports = new TitleMatchedReports();
        String filename = "test";
      ReflectionTestUtils.invokeMethod(titleMatchReportExportService,"prepareWorkbook",titleMatchedReport,filename);

    }

    @Test
    public void uploadFilesinS3() throws  Exception
    {
        String  institution_name = "PUL";

    }
    }