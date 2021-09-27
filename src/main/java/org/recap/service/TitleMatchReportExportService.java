package org.recap.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.solr.client.solrj.SolrServerException;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbConstants;
import org.recap.model.reports.TitleMatchedReport;
import org.recap.model.reports.TitleMatchedReports;
import org.recap.util.ReportsServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/**
 * @author dinakar on 15/09/21
 */
@Service
@Slf4j
public class TitleMatchReportExportService {
    private static final Logger logger = LoggerFactory.getLogger(TitleMatchReportExportService.class);

    @Value("${" + PropertyKeyConstants.AWS_ACCESS_KEY + "}")
    private String awsAccessKey;

    @Value("${" + PropertyKeyConstants.AWS_ACCESS_SECRET_KEY + "}")
    private String awsAccessSecretKey;

    @Value("${" + PropertyKeyConstants.SCSB_BUCKET_NAME + "}")
    private String s3BucketName;

    @Value("${" + PropertyKeyConstants.TITLE_MATCH_REPORT_DIR + "}")
    private String titleReportDir;
    
    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private ReportsServiceUtil reportsServiceUtil;

    private static String[] columns = {"Owning Institution", "BibId", "SCSB Id", "Item Barcode", "CGD", "ISBN", "OCLC", "LCCN", "ISSN", "Title", "Matching Identifier", "Anomaly Flag", "Match Score", "Match Score Translated", "Publisher", "Publication Date", "Chronology And Enum"};

    private void writeTitleMatchReport(TitleMatchedReports titleMatchedReports, Row row) {
        Cell cell = row.createCell(0);
        cell.setCellValue(titleMatchedReports.getOwningInstitution());

        cell = row.createCell(1);
        cell.setCellValue(titleMatchedReports.getBibId());

        cell = row.createCell(2);
        cell.setCellValue(titleMatchedReports.getScsbId());

        cell = row.createCell(3);
        cell.setCellValue(titleMatchedReports.getItemBarcode());

        cell = row.createCell(4);
        cell.setCellValue(titleMatchedReports.getCgd());

        cell = row.createCell(5);
        cell.setCellValue(titleMatchedReports.getIsbn());

        cell = row.createCell(6);
        cell.setCellValue(titleMatchedReports.getOclc());

        cell = row.createCell(7);
        cell.setCellValue(titleMatchedReports.getLccn());

        cell = row.createCell(8);
        cell.setCellValue(titleMatchedReports.getIssn());

        cell = row.createCell(9);
        cell.setCellValue(titleMatchedReports.getTitle());

        cell = row.createCell(10);
        cell.setCellValue(titleMatchedReports.getDuplicateCode());

        cell = row.createCell(11);
        cell.setCellValue(titleMatchedReports.getAnamolyFlag());

        cell = row.createCell(12);
        cell.setCellValue(titleMatchedReports.getMatchScore());

        cell = row.createCell(13);
        cell.setCellValue(titleMatchedReports.getMatchScoreTranslated());

        cell = row.createCell(14);
        cell.setCellValue(titleMatchedReports.getPublisher());

        cell = row.createCell(15);
        cell.setCellValue(titleMatchedReports.getPublicationDate());

        cell = row.createCell(16);
        cell.setCellValue(titleMatchedReports.getChronologyAndEnum());

    }

    public TitleMatchedReport process(TitleMatchedReport titleMatchedReport) throws IOException, ParseException, SolrServerException {
        if(new File(titleReportDir).listFiles() != null) {
            Arrays.stream(new File(titleReportDir).listFiles()).forEach(File::delete);
        }
        titleMatchedReport= reportsServiceUtil.getTitleMatchedReportsExportS3(titleMatchedReport);
        String institution_name = titleMatchedReport.getOwningInst();
        int fileCount = titleMatchedReport.getTotalPageCount();
        for (int i = 0; i < titleMatchedReport.getTotalPageCount(); i++) {
            String filename = createFileName(titleMatchedReport.getOwningInst(), fileCount,i);
            if (!(i <= 0)) {
                titleMatchedReport.setPageNumber(i);
                titleMatchedReport = reportsServiceUtil.getTitleMatchedReportsExportS3(titleMatchedReport);
            }
            prepareWorkbook(titleMatchedReport, filename);
        }
        try {
            zipFiles();
            uploadFilesinS3(institution_name);
        } catch (IOException e) {
          logger.info("Exception occured while doing zip the files: {}"+e.getMessage());
        }
        titleMatchedReport.setMessage("Report is Generated in S3 location is: " + s3BucketName +"/"+ ScsbConstants.TITLE_MATCH_REPORT_PATH);
        log.info("Report is Generated in S3 location is: "+ s3BucketName +"/"+ ScsbConstants.TITLE_MATCH_REPORT_PATH);
        return  titleMatchedReport;
    }

    private void prepareWorkbook(TitleMatchedReport titleMatchedReport, String filename) throws IOException {
        int rowNum =1;
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(filename);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }
        for (TitleMatchedReports titleMatchedReports : titleMatchedReport.getTitleMatchedReports()) {
                Row row = sheet.createRow(rowNum++);
                writeTitleMatchReport(titleMatchedReports, row);
        }
        FileOutputStream fileOut = new FileOutputStream(titleReportDir + filename);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }

    private String createFileName(String owningInst, int fileCount, Integer fileNumber) throws ParseException {
        String formattedDate = getCurrentDate();
        String fileName = getFileName(owningInst, fileNumber, formattedDate,fileCount);
        return fileName;
    }

    private String getCurrentDate() {
        DateTimeFormatter dateFormatForReport = DateTimeFormatter.ofPattern(ScsbConstants.DATE_FORMAT_FOR_FILE_NAME);
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = dateFormatForReport.format(now);
        return formattedDate;
    }

    private String getFileName(String owningInst, Integer fileNumber, String formattedDate, int fileCount) {
        String fileName = (fileCount == 1) ? owningInst + "_Title_Match_" + formattedDate + ".xlsx" :
                owningInst + "_Title_Match_" + formattedDate + "_" + fileNumber + ".xlsx";
        return fileName;
    }

    private void uploadFilesinS3(String institution_name) throws IOException {
        List<File> filesInFolder = Files.walk(Paths.get(titleReportDir))
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(".zip"))
                .map(Path::toFile)
                .collect(Collectors.toList());
        TransferManager xfer_mgr = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
        try {
            Upload xfer = xfer_mgr.upload(s3BucketName, ScsbConstants.TITLE_MATCH_REPORT_PATH + institution_name + "/" + institution_name + ScsbConstants.TITLE_MATCH + getCurrentDate() + ".zip", filesInFolder.get(0));
            XferMgrProgress.waitForCompletion(xfer);
        } catch (AmazonServiceException e) {
            logger.info("Exception occured while processing files to S3: {}"+e.getErrorMessage());
        }
    }

    private void zipFiles() throws IOException {
        List<File> filePaths = Files.walk(Paths.get(titleReportDir))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        try {
            File firstFile = new File(filePaths.get(0).getName());
            String zipFileName = firstFile.getName().replace(".xlsx", "").concat(".zip");

            FileOutputStream fos = new FileOutputStream(titleReportDir + zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File aFile : filePaths) {
                zos.putNextEntry(new ZipEntry(aFile.getName()));
                byte[] bytes = Files.readAllBytes(Path.of(aFile.getPath()));
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
            }
            zos.close();
        } catch (FileNotFoundException ex) {
            logger.info("A file does not exist: " + ex);
        } catch (IOException ex) {
            logger.info("I/O error: " + ex);
        }
    }
}
