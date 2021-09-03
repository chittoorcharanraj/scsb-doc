package org.recap.util;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.recap.BaseTestCaseUT;
import org.recap.model.jpa.MatchingAlgorithmReportDataEntity;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.matchingreports.TitleExceptionReport;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OngoingMatchingAlgorithmReportGeneratorUT extends BaseTestCaseUT {

    @InjectMocks
    OngoingMatchingAlgorithmReportGenerator ongoingMatchingAlgorithmReportGenerator;

    @Test
    public void prepareTitleExceptionReportRecord() throws Exception {
        List<MatchingAlgorithmReportDataEntity> reportDataEntities=new ArrayList<>();
        MatchingAlgorithmReportDataEntity reportDataEntity=new MatchingAlgorithmReportDataEntity();
        reportDataEntity.setHeaderName("owningInstitution");
        reportDataEntity.setHeaderValue("1");
        reportDataEntities.add(reportDataEntity);
        TitleExceptionReport titleExceptionReport =ongoingMatchingAlgorithmReportGenerator.prepareTitleExceptionReportRecord(reportDataEntities) ;
        assertNotNull(titleExceptionReport);
    }
}
