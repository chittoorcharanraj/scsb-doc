package org.recap.service;

import com.amazonaws.services.s3.AmazonS3;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
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

    @Mock
    FileWriter FileWriter;

    @Mock
    FileUtils fileUtils;


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
    public  void process() throws Exception
    {
        TitleMatchedReport titleMatchedReport = new TitleMatchedReport();
        ReflectionTestUtils.setField(titleMatchReportExportService, "s3BucketName", "test");
        ReflectionTestUtils.setField(titleMatchReportExportService, "titleReportStatusFileName", "org/recap/service/deaccession/BibContent1.xml");
        try {
            titleMatchReportExportService.process(titleMatchedReport);
        } catch (Exception e) {
        }
    }

    @Test
    public void generateTitleMatchReport() throws Exception {
        TitleMatchedReport titleMatchedReport = new TitleMatchedReport();
        titleMatchedReport.setOwningInst("PUL");
        titleMatchedReport.setTotalPageCount(2);
        titleMatchedReport.setTotalRecordsCount(1048500);
        titleMatchedReport.setTitleMatch("test1");
        String filename = "test.zip";
        String institution_name = "PUL";
        int fileCount = 2;
        Mockito.when(reportsServiceUtil.getTitleMatchedReportsExportS3(any())).thenReturn(titleMatchedReport);
        ReflectionTestUtils.setField(titleMatchReportExportService, "titleReportDir", "csv.");
        ReflectionTestUtils.setField(titleMatchReportExportService, "s3Client", amazonS3);
        ReflectionTestUtils.setField(titleMatchReportExportService, "s3BucketName", "test");
        try {
            ReflectionTestUtils.invokeMethod(titleMatchReportExportService, "generateTitleMatchReport", titleMatchedReport);
        } catch (Exception e) {
        }


    }

    @Test
    public void createFileName() throws Exception {
        String owningInst = "PUL";
        int fileCount = 5;
        Integer fileNumber = 1;
        ReflectionTestUtils.invokeMethod(titleMatchReportExportService, "createFileName", "PUL", 5, 1);

    }

    @Test
    public void prepareWorkbook() throws Exception {
        List<TitleMatchedReports> titleMatchedReports1 = new ArrayList<>();
        TitleMatchedReports titleMatchedReports = new TitleMatchedReports();
        titleMatchedReports.setTitle("test");
        titleMatchedReports.setMatchScore("1");
        TitleMatchedReport titleMatchedReport = new TitleMatchedReport();
        titleMatchedReport.setTitleMatchedReports(titleMatchedReports1);
        String filename = "test";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("TEST");
        Row headerRow = sheet.createRow(0);
        ReflectionTestUtils.invokeMethod(titleMatchReportExportService, "prepareWorkbook", titleMatchedReport, sheet);
    }

    @Test
    public void writeStatusToFile() throws Exception {
        String status = "False";
        String filename = "/home/manimaran.r/Recap-project/Phase4-SCSB-Doc/testPUL_Title_Match_20210929_192451.xlsx";
        File file = new File(filename);
        ReflectionTestUtils.invokeMethod(titleMatchReportExportService, "writeStatusToFile", file, status);

    }

    @Test
    public void updateStatusCompleteInFile() throws Exception {
        String status = "False";
        File file = new File("testPUL_Title_Match_20210929_192451.xlsx");
        ReflectionTestUtils.setField(titleMatchReportExportService, "titleReportStatusFileName", "test");
        ReflectionTestUtils.invokeMethod(titleMatchReportExportService, "updateStatusCompleteInFile");

    }

     @Test
    public void setDataExportCurrentStatus() throws  Exception
     {
         File file = new File("testPUL_Title_Match_20210929_192451.xlsx");
         ReflectionTestUtils.setField(titleMatchReportExportService, "titleReportStatusFileName", "test");
         ReflectionTestUtils.invokeMethod(titleMatchReportExportService, "setDataExportCurrentStatus");
     }

     @Test
     public void setDataExportCurrentStatus1() throws  Exception {
         try {
             // ReflectionTestUtils.setField(titleMatchReportExportService, "titleReportStatusFileName", "test");
             ReflectionTestUtils.invokeMethod(titleMatchReportExportService, "setDataExportCurrentStatus");
         } catch (Exception e) {
         }
     }
     @Test
    public void uploadFilesinS3() throws Exception {
         try {
             String institution_name = "CUL";
             ReflectionTestUtils.setField(titleMatchReportExportService, "s3Client", amazonS3);
             ReflectionTestUtils.setField(titleMatchReportExportService, "s3BucketName", "test");
             ReflectionTestUtils.setField(titleMatchReportExportService, "titleReportDir", "test");
             ReflectionTestUtils.invokeMethod(titleMatchReportExportService, "uploadFilesinS3", institution_name);
         } catch (Exception e) {
         }
     }
     @Test
    public  void zipFiles() throws Exception
     {
         ReflectionTestUtils.setField(titleMatchReportExportService,"titleReportStatusFileName","test");
         ReflectionTestUtils.setField(titleMatchReportExportService, "s3Client", amazonS3);
         ReflectionTestUtils.setField(titleMatchReportExportService, "s3BucketName", "test");
         ReflectionTestUtils.setField(titleMatchReportExportService, "titleReportDir", "test");
         ReflectionTestUtils.invokeMethod(titleMatchReportExportService,"zipFiles");
     }
}
