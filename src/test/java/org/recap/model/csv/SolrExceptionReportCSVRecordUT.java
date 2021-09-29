package org.recap.model.csv;

import org.junit.jupiter.api.Test;
import org.recap.BaseTestCaseUT;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SolrExceptionReportCSVRecordUT extends BaseTestCaseUT
{
    @Test
    public void SolrExceptionReportCSVRecordUT()
    {
        String docType = "xml";
        String owningInstitutionBibId = "1";
        String bibId ="1";
        String holdingsId = "2";
        String itemId = "123";
        SolrExceptionReportCSVRecord solrExceptionReportCSVRecord = new SolrExceptionReportCSVRecord();
        solrExceptionReportCSVRecord.setDocType(docType);
        solrExceptionReportCSVRecord.setOwningInstitutionBibId(owningInstitutionBibId);
        solrExceptionReportCSVRecord.setBibId(bibId);
        solrExceptionReportCSVRecord.setHoldingsId(holdingsId);
        solrExceptionReportCSVRecord.setItemId(itemId);

        assertNotNull(solrExceptionReportCSVRecord.getBibId());
        assertNotNull(solrExceptionReportCSVRecord.getDocType());
        assertNotNull(solrExceptionReportCSVRecord.getOwningInstitutionBibId());
        assertNotNull(solrExceptionReportCSVRecord.getItemId());
        assertNotNull(solrExceptionReportCSVRecord.getHoldingsId());



    }
}
