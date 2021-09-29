package org.recap.model.csv;

import org.junit.jupiter.api.Test;
import org.recap.BaseTestCaseUT;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DeAccessionSummaryRecordUT extends BaseTestCaseUT
{
    @Test
    public void DeAccessionSummaryRecordUT() throws Exception
    {
        String reasonForFailure = "test";
        String owningInstitutionBibId = "test";
        String dateOfDeAccession = "test";
        DeAccessionSummaryRecord deAccessionSummaryRecord = new DeAccessionSummaryRecord();
        deAccessionSummaryRecord.setDateOfDeAccession(dateOfDeAccession);
        deAccessionSummaryRecord.setOwningInstitutionBibId(owningInstitutionBibId);
        deAccessionSummaryRecord.setReasonForFailure(reasonForFailure);

        assertNotNull(deAccessionSummaryRecord.getDateOfDeAccession());
        assertNotNull(deAccessionSummaryRecord.getOwningInstitutionBibId());
        assertNotNull(deAccessionSummaryRecord.getReasonForFailure());
    }

}
