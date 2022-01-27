package org.recap.util;

import com.csvreader.CsvWriter;
import lombok.extern.slf4j.Slf4j;
import org.recap.ScsbCommonConstants;
import org.recap.model.matchingreports.TitleExceptionReport;
import org.recap.model.reports.TitleMatchedReport;
import org.recap.model.reports.TitleMatchedReports;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by angelind on 27/6/17.
 */
@Slf4j
@Component
public class CsvUtil {

    @Autowired
    private ReportsServiceUtil reportsServiceUtil;

    /**
     * Create title exception report file file.
     *
     * @param fileNameWithExtension the file name with extension
     * @param maxTitleCount         the max title count
     * @param titleExceptionReports the title exception reports
     * @return the file
     */
    public File createTitleExceptionReportFile(String fileNameWithExtension, int maxTitleCount, List<TitleExceptionReport> titleExceptionReports) {
        File file = new File(fileNameWithExtension);
        CsvWriter csvOutput = null;
        try (FileWriter fileWriter = new FileWriter(file, true)){
            csvOutput = new CsvWriter(fileWriter, ',');
            writeHeaderRowForTitleExceptionReport(csvOutput, maxTitleCount);
            for(TitleExceptionReport exceptionReport : titleExceptionReports) {
                writeDataRowForTitleExceptionReport(exceptionReport, csvOutput);
            }
        } catch (Exception e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        } finally {
            if(csvOutput != null) {
                csvOutput.flush();
                csvOutput.close();
            }
        }
        return file;
    }

    /**
     * Write header row for title exception report.
     *
     * @param csvOutput     the csv output
     * @param maxTitleCount the max title count
     * @throws IOException the io exception
     */
    public void writeHeaderRowForTitleExceptionReport(CsvWriter csvOutput, int maxTitleCount) throws IOException {
        csvOutput.write("OwningInstitution");
        csvOutput.write("BibId");
        csvOutput.write("OwningInstitutionBibId");
        csvOutput.write("MaterialType");
        csvOutput.write("OCLCNumber");
        csvOutput.write("ISBN");
        csvOutput.write("ISSN");
        csvOutput.write("LCCN");
        for(int i=1; i<=maxTitleCount; i++) {
            csvOutput.write("Title" + i);
        }
        csvOutput.endRecord();
    }

    /**
     * Write data row for title exception report.
     *
     * @param titleExceptionReport the title exception report
     * @param csvOutput            the csv output
     * @throws IOException the io exception
     */
    public void writeDataRowForTitleExceptionReport(TitleExceptionReport titleExceptionReport, CsvWriter csvOutput) throws IOException {
        csvOutput.write(titleExceptionReport.getOwningInstitution());
        csvOutput.write(titleExceptionReport.getBibId());
        csvOutput.write(titleExceptionReport.getOwningInstitutionBibId());
        csvOutput.write(titleExceptionReport.getMaterialType());
        csvOutput.write(titleExceptionReport.getOclc());
        csvOutput.write(titleExceptionReport.getIsbn());
        csvOutput.write(titleExceptionReport.getIssn());
        csvOutput.write(titleExceptionReport.getLccn());
        for(String title : titleExceptionReport.getTitleList()) {
            csvOutput.write(title);
        }
        csvOutput.endRecord();
    }

    /**
     * Write header row for serils and mvms report.
     *
     * @param csvWriter the csv writer
     * @throws IOException the io exception
     */
    public void writeHeaderRowForSerilsAndMvmsReport(CsvWriter csvWriter) throws IOException {

    }

    public void writeDataRowForTitleMatchExportReport(TitleMatchedReports titleMatchedReports, CsvWriter csvOutput) throws IOException {

        csvOutput.write(titleMatchedReports.getOwningInstitution());

        csvOutput.write(titleMatchedReports.getBibId());

        csvOutput.write(String.valueOf(titleMatchedReports.getScsbId()));

        csvOutput.write(titleMatchedReports.getItemBarcode());

        csvOutput.write(titleMatchedReports.getCgd());

        csvOutput.write(titleMatchedReports.getIsbn());

        csvOutput.write(titleMatchedReports.getOclc());

        csvOutput.write(titleMatchedReports.getLccn());

        csvOutput.write(titleMatchedReports.getIssn());

        csvOutput.write(titleMatchedReports.getTitle());

        csvOutput.write(titleMatchedReports.getDuplicateCode());

        csvOutput.write(titleMatchedReports.getAnamolyFlag());

        csvOutput.write(titleMatchedReports.getMatchScore());

        csvOutput.write(titleMatchedReports.getMatchScoreTranslated());

        csvOutput.write(titleMatchedReports.getPublisher());

        csvOutput.write(titleMatchedReports.getPublicationDate());

        csvOutput.write(titleMatchedReports.getChronologyAndEnum());

        csvOutput.endRecord();
    }

    public void writeHeaderRowForTitleMatchReport(CsvWriter csvOutput) throws IOException {
        csvOutput.write("OwningInstitution");
        csvOutput.write("BibId");
        csvOutput.write("SCSBId");
        csvOutput.write("ItemBarcode");
        csvOutput.write("CGD");
        csvOutput.write("ISBN");
        csvOutput.write("OCLC");
        csvOutput.write("LCCN");
        csvOutput.write("ISSN");
        csvOutput.write("Title");
        csvOutput.write("MatchingIdentifier");
        csvOutput.write("AnomalyFLag");
        csvOutput.write("MatchScore");
        csvOutput.write("MaTchScoreTranslated");
        csvOutput.write("Publisher");
        csvOutput.write("PublicationDate");
        csvOutput.write("ChronologyAndEnum");
        csvOutput.endRecord();
    }

    public File createTitleMatchReportFile(String fileNameWithExtension, TitleMatchedReport titleMatchedReport) {
        File file = new File(fileNameWithExtension);
        CsvWriter csvOutput = null;
        try (FileWriter fileWriter = new FileWriter(file, true)){
            csvOutput = new CsvWriter(fileWriter, ',');
            writeHeaderRowForTitleMatchReport(csvOutput);

            for (int i = 0; i <titleMatchedReport.getTotalPageCount() ; i++) {
                titleMatchedReport.setPageNumber(i);
                titleMatchedReport = reportsServiceUtil.getTitleMatchedReportsExportS3(titleMatchedReport);
                for(TitleMatchedReports titleMatchedReports : titleMatchedReport.getTitleMatchedReports()) {
                    writeDataRowForTitleMatchExportReport(titleMatchedReports, csvOutput);
                }
            }

        } catch (Exception e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        } finally {
            if(csvOutput != null) {
                csvOutput.flush();
                csvOutput.close();
            }
        }
        return file;
    }
}
