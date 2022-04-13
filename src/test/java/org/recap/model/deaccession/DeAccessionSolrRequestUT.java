package org.recap.model.deaccession;


import org.junit.Test;
import org.recap.BaseTestCaseUT;


import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DeAccessionSolrRequestUT extends BaseTestCaseUT
{
    @Test
    public void DeAccessionSolrRequest() throws Exception
    {
        String status = "IN";
        DeAccessionSolrRequest deAccessionSolrRequest = new DeAccessionSolrRequest();
        deAccessionSolrRequest.setStatus(status);
        assertNotNull(deAccessionSolrRequest.getStatus());
    }

}
