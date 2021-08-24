package org.recap.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.model.reports.TitleMatchedReport;
import org.recap.model.solr.SolrIndexRequest;
import org.recap.model.submitCollection.SubmitCollectionReport;
import org.recap.report.ReportGenerator;
import org.recap.repository.jpa.ReportDetailRepository;
import org.recap.util.DateUtil;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by hemalathas on 19/1/17.
 */
public class GenerateReportControllerUT extends BaseTestCaseUT {

    @InjectMocks
    GenerateReportController generateReportController;

    @Mock
    ReportGenerator reportGenerator;

    @Mock
    ReportDetailRepository reportDetailRepository;

    @Mock
    BindingResult bindingResult;

    @Mock
    Model model;

    @Mock
    DateUtil dateUtil;

    @Mock
    SubmitCollectionReport submitCollectionReprot;

    @Mock
    ReportEntity reportEntity;

    @Mock
    TitleMatchedReport titleMatchedReport;


    @Test
    @DisplayName("Test title Match count")
    public void testTitleMatchCount() throws Exception{
        Mockito.when(reportGenerator.getItemMatchCount(Mockito.any())).thenCallRealMethod();
        ResponseEntity<TitleMatchedReport> reportResponseEntity=generateReportController.titleMatchCount(titleMatchedReport);
        assertNotNull(reportResponseEntity);
    }

    @Test
    @DisplayName("Test title Match Report")
    public void testTitleMatchReport() throws Exception{
        Mockito.when(reportGenerator.getItemMatchReport(Mockito.any())).thenCallRealMethod();
        ResponseEntity<TitleMatchedReport> reportResponseEntity=generateReportController.titleMatchReport(titleMatchedReport);
        assertNotNull(reportResponseEntity);
    }

    @Test
    @DisplayName("Test title Match Report Export")
    public void testTitleMatchReportExport() throws Exception{
        Mockito.when(reportGenerator.getItemMatchReportExport(Mockito.any())).thenCallRealMethod();
        ResponseEntity<TitleMatchedReport> reportResponseEntity=generateReportController.titleMatchReportExport(titleMatchedReport);
        assertNotNull(reportResponseEntity);
    }


    @Test
    @DisplayName("Test accession summary reports for file system")
    public void testAccessionSummaryReportForFileSystem() throws Exception{
        List<ReportEntity> reportEntityList = saveSummaryReportEntity();
        Date createdDate = reportEntityList.get(0).getCreatedDate();
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setCreatedDate(createdDate);
        solrIndexRequest.setReportType(ScsbCommonConstants.ACCESSION_SUMMARY_REPORT);
        solrIndexRequest.setOwningInstitutionCode("PUL");
        solrIndexRequest.setTransmissionType(ScsbCommonConstants.FILE_SYSTEM);
        Mockito.when(reportGenerator.generateReport(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any())).thenReturn("test");
        String reponse = generateReportController.generateReports(solrIndexRequest,bindingResult,model);
        assertNotNull(reponse);
    }
    @Test
    @DisplayName("Test ongoing accession summary reports for file system")
    public void testOngoingAccessionSummaryReportForFileSystem() throws Exception{
        List<ReportEntity> reportEntityList = saveSummaryReportEntity();
        Date createdDate = reportEntityList.get(0).getCreatedDate();
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setCreatedDate(createdDate);
        solrIndexRequest.setReportType(ScsbConstants.ONGOING_ACCESSION_REPORT);
        solrIndexRequest.setOwningInstitutionCode("PUL");
        solrIndexRequest.setTransmissionType(ScsbCommonConstants.FILE_SYSTEM);
        Mockito.when(reportGenerator.generateReport(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any())).thenReturn("test");
        String reponse = generateReportController.generateReports(solrIndexRequest,bindingResult,model);
        assertNotNull(reponse);
    }
    @Test
    @DisplayName("Test deaccession summary reports for file system")
    public void testDeaccessionSummaryReportForFileSystem() throws Exception{
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setReportType(ScsbCommonConstants.DEACCESSION_SUMMARY_REPORT);
        solrIndexRequest.setOwningInstitutionCode("PUL");
        solrIndexRequest.setTransmissionType(ScsbCommonConstants.FILE_SYSTEM);
        Mockito.when(reportGenerator.generateReport(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any())).thenReturn("test");
        String reponse = generateReportController.generateReports(solrIndexRequest,bindingResult,model);
        assertNotNull(reponse);
    }
    @Test
    @DisplayName("Test solr rejected summary reports for file system")
    public void testSubmitRejectedSummaryReportForFileSystem() throws Exception{
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setReportType(ScsbCommonConstants.SUBMIT_COLLECTION_REJECTION_REPORT);
        solrIndexRequest.setOwningInstitutionCode("PUL");
        solrIndexRequest.setTransmissionType(ScsbCommonConstants.FILE_SYSTEM);
        Mockito.when(reportGenerator.generateReport(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any())).thenReturn("test");
        String reponse = generateReportController.generateReports(solrIndexRequest,bindingResult,model);
        assertNotNull(reponse);
    }
    @Test
    @DisplayName("Test solr failure summary reports for file system")
    public void testSolrFailureSummaryReportForFileSystem() throws Exception{
        Date toDate = new Date();
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setToDate(toDate);
        solrIndexRequest.setReportType("Failure");
        solrIndexRequest.setOwningInstitutionCode("PUL");
        solrIndexRequest.setTransmissionType(ScsbCommonConstants.FILE_SYSTEM);
        Mockito.when(reportGenerator.generateReport(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any())).thenReturn("test");
        String reponse = generateReportController.generateReports(solrIndexRequest,bindingResult,model);
        assertNotNull(reponse);
    }

    @Test
    @DisplayName("Test submit exception summary reports for file system")
    public void testSubmitExceptionSummaryReportForFileSystem() throws Exception{
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setReportType(ScsbCommonConstants.SUBMIT_COLLECTION_EXCEPTION_REPORT);
        solrIndexRequest.setOwningInstitutionCode("PUL");
        solrIndexRequest.setTransmissionType(ScsbCommonConstants.FILE_SYSTEM);
        Mockito.when(reportGenerator.generateReport(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any())).thenReturn("test");
        String reponse = generateReportController.generateReports(solrIndexRequest,bindingResult,model);
        assertNotNull(reponse);
    }

    @Test
    @DisplayName("Test submit success summary reports for file system")
    public void testSubmitSuccessSummaryReportForFileSystem() throws Exception{
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setReportType(ScsbCommonConstants.SUBMIT_COLLECTION_SUCCESS_REPORT);
        solrIndexRequest.setOwningInstitutionCode("PUL");
        solrIndexRequest.setTransmissionType(ScsbCommonConstants.FILE_SYSTEM);
        Mockito.when(reportGenerator.generateReport(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any())).thenReturn("test");
        String reponse = generateReportController.generateReports(solrIndexRequest,bindingResult,model);
        assertNotNull(reponse);
    }
    @Test
    @DisplayName("Test submit failure summary reports for file system")
    public void testSubmitFailureSummaryReportForFileSystem() throws Exception{
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setReportType(ScsbCommonConstants.SUBMIT_COLLECTION_FAILURE_REPORT);
        solrIndexRequest.setOwningInstitutionCode("PUL");
        solrIndexRequest.setTransmissionType(ScsbCommonConstants.FILE_SYSTEM);
        Mockito.when(reportGenerator.generateReport(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any())).thenReturn("test");
        String reponse = generateReportController.generateReports(solrIndexRequest,bindingResult,model);
        assertNotNull(reponse);
    }

    @Test
    @DisplayName("Test submit collection reports for file system")
    public void testSubmitSummaryReportForFileSystem() throws Exception{
        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
        solrIndexRequest.setReportType(ScsbConstants.SUBMIT_COLLECTION_SUMMARY_REPORT);
        solrIndexRequest.setOwningInstitutionCode("PUL");
        solrIndexRequest.setTransmissionType(ScsbCommonConstants.FILE_SYSTEM);
        Mockito.when(reportGenerator.generateReport(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any())).thenReturn("");
        String reponse = generateReportController.generateReports(solrIndexRequest,bindingResult,model);
        assertNotNull(reponse);
    }
    @Test
    @DisplayName("Test submit collection reports when export disabled")
    public void testSubmitCollectionReports() throws Exception{
        ResponseEntity<SubmitCollectionReport> reponse = generateReportController.submitCollectionReports(submitCollectionReprot);
        assertNotNull(reponse);
    }

    @Test
    @DisplayName("Test submit collection reports when export enabled")
    public void testSubmitCollectionReports1() throws Exception{
        Mockito.when(submitCollectionReprot.isExportEnabled()).thenReturn(true);
        ResponseEntity<SubmitCollectionReport> reponse = generateReportController.submitCollectionReports(submitCollectionReprot);
        assertNotNull(reponse);
    }

    @Test
    @DisplayName("Test Accession report for Exception")
    public void accessionException() throws Exception{
        Mockito.when(reportGenerator.accessionExceptionReportGenerator(Mockito.any())).thenCallRealMethod();
        Mockito.when(submitCollectionReprot.getPageNumber()).thenReturn(1);
        Mockito.when(submitCollectionReprot.getPageSize()).thenReturn(1);
        Mockito.when(submitCollectionReprot.getInstitutionName()).thenReturn(ScsbCommonConstants.PRINCETON);
        Mockito.when(submitCollectionReprot.getFrom()).thenReturn(new Date());
        Mockito.when(submitCollectionReprot.getTo()).thenReturn(new Date());
        Page<ReportEntity> reportEntityList=Mockito.mock(Page.class);
        List<ReportEntity> reportEntities=new ArrayList<>();
        reportEntities.add(reportEntity);
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();
        reportDataEntities.add(getReportDataEntity("itemBarcode", "123456"));
        reportDataEntities.add(getReportDataEntity(ScsbCommonConstants.CUSTOMER_CODE, "PA"));
        reportDataEntities.add(getReportDataEntity(ScsbCommonConstants.OWNING_INSTITUTION, ScsbCommonConstants.PRINCETON));
        reportDataEntities.add(getReportDataEntity(ScsbCommonConstants.MESSAGE, "testsubmit"));
        Mockito.when(reportEntity.getReportDataEntities()).thenReturn(reportDataEntities);
        Mockito.when(reportEntityList.getContent()).thenReturn(reportEntities);
        Mockito.when(reportEntityList.getTotalPages()).thenReturn(1);
        Mockito.when(reportEntityList.getTotalElements()).thenReturn(1l);
        Mockito.when(reportEntity.getType()).thenReturn(ScsbCommonConstants.SUBMIT_COLLECTION_EXCEPTION_REPORT);
        ReflectionTestUtils.setField(reportGenerator,"reportDetailRepository",reportDetailRepository);
        Mockito.when(reportDetailRepository.findByInstitutionAndTypeAndDateRangeAndAccession(Mockito.any(),Mockito.anyString(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(reportEntityList);
        ResponseEntity<SubmitCollectionReport> accessionException=generateReportController.accessionException(submitCollectionReprot);
        assertNotNull(accessionException);
    }

    @Test
    @DisplayName("Test Accession report for Exception")
    public void accessionExceptionReportExport() throws Exception{
        Mockito.when(submitCollectionReprot.isExportEnabled()).thenReturn(true);
        Mockito.when(submitCollectionReprot.getInstitutionName()).thenReturn(ScsbCommonConstants.PRINCETON);
        Mockito.when(submitCollectionReprot.getFrom()).thenReturn(new Date());
        Mockito.when(submitCollectionReprot.getTo()).thenReturn(new Date());
        List<ReportEntity> reportEntities=new ArrayList<>();
        reportEntities.add(reportEntity);
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();
        reportDataEntities.add(getReportDataEntity("itemBarcode", "123456"));
        reportDataEntities.add(getReportDataEntity(ScsbCommonConstants.CUSTOMER_CODE, "PA"));
        reportDataEntities.add(getReportDataEntity(ScsbCommonConstants.OWNING_INSTITUTION, ScsbCommonConstants.PRINCETON));
        reportDataEntities.add(getReportDataEntity(ScsbCommonConstants.MESSAGE, "testsubmit"));
        Mockito.when(reportEntity.getReportDataEntities()).thenReturn(reportDataEntities);
        Mockito.when(reportEntity.getType()).thenReturn(ScsbCommonConstants.SUBMIT_COLLECTION_SUCCESS_REPORT);
        ReflectionTestUtils.setField(reportGenerator,"reportDetailRepository",reportDetailRepository);
        Mockito.when(reportDetailRepository.findByInstitutionAndTypeAndDateRangeAndAccession(Mockito.any(),Mockito.anyString(),Mockito.any(),Mockito.any())).thenReturn(reportEntities);
        Mockito.when(reportGenerator.accessionExceptionReportExport(Mockito.any())).thenCallRealMethod();
        ResponseEntity<SubmitCollectionReport> accessionException=generateReportController.accessionException(submitCollectionReprot);
        assertNotNull(accessionException);
    }

    private ReportDataEntity getReportDataEntity(String barcode, String s) {
        ReportDataEntity reportDataEntity = new ReportDataEntity();
        reportDataEntity.setHeaderName(barcode);
        reportDataEntity.setHeaderValue(s);
        return reportDataEntity;
    }

    private List<ReportEntity> saveSummaryReportEntity(){
        List<ReportEntity> reportEntityList = new ArrayList<>();
        List<ReportDataEntity> reportDataEntities = new ArrayList<>();
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName(ScsbCommonConstants.ACCESSION_REPORT);
        reportEntity.setType(ScsbCommonConstants.ACCESSION_SUMMARY_REPORT);
        reportEntity.setCreatedDate(new Date());
        reportEntity.setInstitutionName("PUL");

        ReportDataEntity successBibCountReportDataEntity = new ReportDataEntity();
        successBibCountReportDataEntity.setHeaderName(ScsbCommonConstants.BIB_SUCCESS_COUNT);
        successBibCountReportDataEntity.setHeaderValue(String.valueOf(1));
        reportDataEntities.add(successBibCountReportDataEntity);

        ReportDataEntity successItemCountReportDataEntity = new ReportDataEntity();
        successItemCountReportDataEntity.setHeaderName(ScsbCommonConstants.ITEM_SUCCESS_COUNT);
        successItemCountReportDataEntity.setHeaderValue(String.valueOf(1));
        reportDataEntities.add(successItemCountReportDataEntity);

        ReportDataEntity existsBibCountReportDataEntity = new ReportDataEntity();
        existsBibCountReportDataEntity.setHeaderName(ScsbCommonConstants.NUMBER_OF_BIB_MATCHES);
        existsBibCountReportDataEntity.setHeaderValue(String.valueOf(0));
        reportDataEntities.add(existsBibCountReportDataEntity);

        ReportDataEntity failedBibCountReportDataEntity = new ReportDataEntity();
        failedBibCountReportDataEntity.setHeaderName(ScsbCommonConstants.BIB_FAILURE_COUNT);
        failedBibCountReportDataEntity.setHeaderValue(String.valueOf(0));
        reportDataEntities.add(failedBibCountReportDataEntity);

        ReportDataEntity failedItemCountReportDataEntity = new ReportDataEntity();
        failedItemCountReportDataEntity.setHeaderName(ScsbCommonConstants.ITEM_FAILURE_COUNT);
        failedItemCountReportDataEntity.setHeaderValue(String.valueOf(0));
        reportDataEntities.add(failedItemCountReportDataEntity);

        ReportDataEntity reasonForBibFailureReportDataEntity = new ReportDataEntity();
        reasonForBibFailureReportDataEntity.setHeaderName(ScsbConstants.FAILURE_BIB_REASON);
        reasonForBibFailureReportDataEntity.setHeaderValue("");
        reportDataEntities.add(reasonForBibFailureReportDataEntity);

        ReportDataEntity reasonForItemFailureReportDataEntity = new ReportDataEntity();
        reasonForItemFailureReportDataEntity.setHeaderName(ScsbConstants.FAILURE_ITEM_REASON);
        reasonForItemFailureReportDataEntity.setHeaderValue("");
        reportDataEntities.add(reasonForItemFailureReportDataEntity);

        reportEntity.setReportDataEntities(reportDataEntities);
        reportEntityList.add(reportEntity);
        return reportEntityList;

    }
}