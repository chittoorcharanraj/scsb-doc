package org.recap.model.csv;



import org.junit.Test;
import org.recap.BaseTestCaseUT;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AccessionSummaryRecordUT extends BaseTestCaseUT
{
@Test
public  void AccessionSummaryRecord()
{
    String reasonForFailureBib = "test";
    String reasonForFailureItem = "test";
    AccessionSummaryRecord accessionSummaryRecord = new AccessionSummaryRecord();
    accessionSummaryRecord.setReasonForFailureBib(reasonForFailureBib);
    accessionSummaryRecord.setReasonForFailureItem(reasonForFailureItem);
    assertNotNull(accessionSummaryRecord.getReasonForFailureBib());
    assertNotNull(accessionSummaryRecord.getReasonForFailureItem());

}

}
