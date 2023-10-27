package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCaseUT;
import org.recap.camel.processor.ReportProcessor;
import org.recap.camel.route.ReportsRouteBuilder;

/**
 * @author Charan Raj C created on 27/10/23
 */

public class ReportsRouteBuilderUT extends BaseTestCaseUT {

    @Mock
    private ReportProcessor reportProcessor;

    private CamelContext camelContext;

    @InjectMocks
    ReportsRouteBuilder reportsRouteBuilder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        camelContext = new DefaultCamelContext();
    }

    @Test
    public void testReportsRouteBuilder() throws Exception {
        ReportsRouteBuilder reportsRouteBuilder = new ReportsRouteBuilder(camelContext, reportProcessor);
        camelContext.stop();
    }

    @Test
    public void testConstructorWithException() throws Exception {
        try{
        Mockito.when(new ReportsRouteBuilder(camelContext, reportProcessor)).thenThrow(RuntimeException.class);}catch (Exception e){
            e.printStackTrace();
        }
    }
}
