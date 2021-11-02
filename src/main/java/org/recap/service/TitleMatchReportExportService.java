package org.recap.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
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
import org.recap.util.CsvUtil;
import org.recap.util.ReportsServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
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

    @Value("${" + PropertyKeyConstants.TITLE_REPORT_STATUS_FILE + "}")
    private String titleReportStatusFileName;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private ReportsServiceUtil reportsServiceUtil;

    @Autowired
    private CsvUtil csvUtil;

    public CsvUtil getCsvUtil() {
        return csvUtil;
    }

    private Integer rowNumber = 1;

    private static String[] columns = {"Owning Institution", "BibId", "SCSB Id", "Item Barcode", "CGD", "ISBN", "OCLC", "LCCN", "ISSN", "Title", "Matching Identifier", "Anomaly Flag", "Match Score", "Match Score Translated", "Publisher", "Publication Date", "Chronology And Enum"};


    public TitleMatchedReport process(TitleMatchedReport titleMatchedReport) throws IOException, ParseException, SolrServerException {
        if (setDataExportCurrentStatus()) {
            Executors.newCachedThreadPool().execute(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    generateTitleMatchReport(titleMatchedReport);
                }
            });
            titleMatchedReport.setReportMessage(ScsbConstants.TITLE_MATCH_REPORT_MESSAGE + s3BucketName + "/" + ScsbConstants.TITLE_MATCH_REPORT_PATH + ScsbConstants.TITLE_MATCH_REPORT_MESSAGE_APPEND);
            return titleMatchedReport;
        } else {
            titleMatchedReport.setReportMessage(ScsbConstants.TITLE_REPORT_MESSAGE_IN_PROGRESS);
            return titleMatchedReport;
        }
    }

    private void generateTitleMatchReport(TitleMatchedReport titleMatchedReport) throws ParseException, SolrServerException, IOException {
        int count = (titleMatchedReport.getTitleMatch().equalsIgnoreCase(ScsbConstants.TITLE_MATCHED)) ? 1000:100;
        log.info("Title Match Export process started");
        if (new File(titleReportDir).listFiles() != null) {
            Arrays.stream(new File(titleReportDir).listFiles()).filter(file -> file.getName().contains(".csv")).forEach(File::delete);
        }
        if (new File(titleReportDir).listFiles() != null) {
            Arrays.stream(new File(titleReportDir).listFiles()).filter(file -> file.getName().contains(".zip")).forEach(File::delete);
        }
        titleMatchedReport = reportsServiceUtil.getTitleMatchedReportsExportS3(titleMatchedReport);
        String institution_name = titleMatchedReport.getOwningInst();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ScsbConstants.DATE_FORMAT_FOR_REPORTS);
            String formattedDate = sdf.format(new Date());
            String fileNameWithExtension = titleReportDir + File.separator + institution_name + ScsbConstants.TITLE_MATCH + ScsbConstants.UNDER_SCORE + formattedDate + ScsbConstants.CSV_EXTENSION;
            File file = getCsvUtil().createTitleMatchReportFile(fileNameWithExtension, titleMatchedReport);
            zipFiles();
            uploadFilesinS3(institution_name);
            updateStatusCompleteInFile();
            logger.info("Title Match Export process completed and file placed in s3 is :: {}",file.getAbsolutePath());
        } catch (IOException e) {
            logger.info("Exception occured while doing zip the files: {}", e.getMessage());
            updateStatusCompleteInFile();
        } catch (IllegalArgumentException ie) {
            logger.info("Exception occured while preparing work book {}", ie.getMessage());
            updateStatusCompleteInFile();
        } catch (Exception ne) {
            logger.info("Exception occured while exporting records");
            updateStatusCompleteInFile();
        }
    }

    private void prepareWorkbook(TitleMatchedReport titleMatchedReport, Sheet sheet) throws IOException {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }
        for (TitleMatchedReports titleMatchedReports : titleMatchedReport.getTitleMatchedReports()) {
            Row row = sheet.createRow(rowNumber++);
            writeTitleMatchReport(titleMatchedReports, row);
        }
    }

    private String createFileName(String owningInst, int fileCount, Integer fileNumber) throws ParseException {
        String formattedDate = getCurrentDate();
        String fileName = getFileName(owningInst, fileNumber, formattedDate, fileCount);
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
            logger.info("Exception occured while processing files to S3: {}" + e.getErrorMessage());
        }
    }

    private void zipFiles() throws IOException {
        List<File> filePaths = Files.walk(Paths.get(titleReportDir))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        try {
            Optional<File> firstFile = filePaths.stream().filter(file -> file.getName().contains("csv")).findFirst();
            String zipFileName = null;
            if (firstFile.isPresent()) {
                zipFileName = firstFile.get().getName().replace(".csv", "").concat(".zip");
            }

            FileOutputStream fos = new FileOutputStream(titleReportDir + zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File aFile : filePaths) {
                if (aFile.getName().contains(".xlsx")) {
                    zos.putNextEntry(new ZipEntry(aFile.getName()));
                    byte[] bytes = Files.readAllBytes(Path.of(aFile.getPath()));
                    zos.write(bytes, 0, bytes.length);
                    zos.closeEntry();
                }
            }
            zos.close();
        } catch (FileNotFoundException ex) {
            logger.info("A file does not exist: " + ex);
        } catch (IOException ex) {
            logger.info("I/O error: " + ex);
        }
    }

    private void writeStatusToFile(File file, String status) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file, false)) {
            fileWriter.append(status);
            fileWriter.flush();
        } catch (IOException e) {
            logger.error(ScsbConstants.EXCEPTION, e);
        }
    }

    private void updateStatusCompleteInFile() {
        File file = new File(titleReportStatusFileName);
        try (FileWriter fileWriter = new FileWriter(file, false)) {
            fileWriter.append(ScsbConstants.COMPLETED);
            fileWriter.flush();
        } catch (IOException e) {
            logger.error(ScsbConstants.EXCEPTION, e);
        }
    }

    private Boolean setDataExportCurrentStatus() {
        File file = new File(titleReportStatusFileName);
        File parentFile = file.getParentFile();
        try {
            if (file.exists()) {
                String dataDumpStatus = FileUtils.readFileToString(file, Charset.defaultCharset());
                if (dataDumpStatus.contains(ScsbConstants.COMPLETED)) {
                    writeStatusToFile(file, ScsbConstants.IN_PROGRESS);
                    return true;
                } else {
                    return false;
                }
            } else {
                parentFile.mkdirs();
                boolean newFile = file.createNewFile();
                if (newFile) {
                    writeStatusToFile(file, ScsbConstants.IN_PROGRESS);
                    return true;
                }
            }
        } catch (IOException e) {
            logger.error(ScsbConstants.ERROR, e);
            logger.error("Exception while creating or updating the file : " + e.getMessage());
        }
        return false;
    }

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
}
