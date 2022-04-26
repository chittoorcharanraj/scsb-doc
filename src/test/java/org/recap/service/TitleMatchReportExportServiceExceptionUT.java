package org.recap.service;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCaseUT;
import org.recap.model.reports.TitleMatchedReport;
import org.recap.util.CsvUtil;
import org.recap.util.ReportsServiceUtil;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.assertNotNull;

/**
 * @author dinakar on 30/11/21
 */
public class TitleMatchReportExportServiceExceptionUT extends BaseTestCaseUT {

    @Mock
    TitleMatchReportExportService titleMatchReportExportService;

    @Mock
    CsvUtil csvUtil;

    @Mock
    ReportsServiceUtil reportsServiceUtil;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    public CsvUtil getCsvUtil() {
        return csvUtil;
    }

    @Test
    public void generateTitleMatchReport() throws SolrServerException, IOException, ParseException {
        TitleMatchedReport titleMatchedReport = new TitleMatchedReport();
        titleMatchedReport.setOwningInst("PUL");
        titleMatchedReport.setTotalPageCount(2);
        titleMatchedReport.setTotalRecordsCount(1048500);
        titleMatchedReport.setTitleMatch("test1");
        ReflectionTestUtils.setField(titleMatchReportExportService, "titleReportDir", "org/recap/service");
        ReflectionTestUtils.setField(titleMatchReportExportService, "reportsServiceUtil",reportsServiceUtil);
        ReflectionTestUtils.setField(titleMatchReportExportService, "titleReportStatusFileName","org/recap/service/LoadStatusCompleted.txt");

        Mockito.when(titleMatchReportExportService.getCsvUtil()).thenReturn(csvUtil);
        Mockito.when(reportsServiceUtil.getTitleMatchedReportsExportS3(Mockito.any())).thenReturn(titleMatchedReport);
        Mockito.when(titleMatchReportExportService.getCsvUtil().createTitleMatchReportFile(Mockito.anyString(),Mockito.any())).thenThrow(new IllegalArgumentException());
        Mockito.when(titleMatchReportExportService.process(titleMatchedReport)).thenCallRealMethod();
        TitleMatchedReport response = titleMatchReportExportService.process(titleMatchedReport);
        assertNotNull(response);
    }
}
