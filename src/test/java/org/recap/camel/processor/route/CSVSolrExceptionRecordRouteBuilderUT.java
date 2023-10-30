package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCaseUT;
import org.recap.camel.route.CSVSolrExceptionRecordRouteBuilder;

/**
 * @author Charan Raj C created on 27/10/23
 */

public class CSVSolrExceptionRecordRouteBuilderUT extends BaseTestCaseUT {

    private CamelContext camelContext;

    @InjectMocks
    CSVSolrExceptionRecordRouteBuilder csvSolrExceptionRecordRouteBuilder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        camelContext = new DefaultCamelContext();
    }

    @Test
    public void csvSolRouteBuilder() throws Exception {
        CSVSolrExceptionRecordRouteBuilder csvSolrExceptionRecordRouteBuilder = new CSVSolrExceptionRecordRouteBuilder(camelContext, "solrReportsDirectory");
        camelContext.stop();
    }

    @Test
    public void csvSolRouteBuilderException() throws Exception {
        try{
        Mockito.when(new CSVSolrExceptionRecordRouteBuilder(camelContext, "solrReportsDirectory")).thenThrow(RuntimeException.class);}catch (Exception e){
            e.printStackTrace();
        }
    }
}
