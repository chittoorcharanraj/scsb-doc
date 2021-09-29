package org.recap.model.matchingreports;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OngoingMatchingCGDReportUT extends Exception{

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

