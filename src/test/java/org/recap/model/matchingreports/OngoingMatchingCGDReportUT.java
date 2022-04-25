package org.recap.model.matchingreports;



import org.junit.Test;
import org.recap.BaseTestCaseUT;


import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OngoingMatchingCGDReportUT extends BaseTestCaseUT {

    @Test
    public  void OngoingMatchingCGDReport() throws  Exception{
        OngoingMatchingCGDReport ongoingMatchingCGDReport = new OngoingMatchingCGDReport();
        String itemBarcode = "123456";
        String oldCgd = "shared";
        String newCgd = "open";
        String Date = "12.01.1018";
        ongoingMatchingCGDReport.setItemBarcode(itemBarcode);
        ongoingMatchingCGDReport.setOldCgd(oldCgd);
        ongoingMatchingCGDReport.setNewCgd(newCgd);
        ongoingMatchingCGDReport.setDate(Date);

        assertNotNull(ongoingMatchingCGDReport.getOldCgd());
        assertNotNull(ongoingMatchingCGDReport.getNewCgd());
        assertNotNull(ongoingMatchingCGDReport.getItemBarcode());
        assertNotNull(ongoingMatchingCGDReport.getDate());

    }
}

