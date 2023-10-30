package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCaseUT;
import org.recap.PropertyKeyConstants;
import org.recap.camel.route.FSOngoingAccessionReportRouteBuilder;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Charan Raj C created on 27/10/23
 */

public class FSOngoingAccessionReportRouteBuilderUT extends BaseTestCaseUT {

    private CamelContext camelContext;

    @InjectMocks
    FSOngoingAccessionReportRouteBuilder fsOngoingAccessionReportRouteBuilder;

    @Value("${" + PropertyKeyConstants.ONGOING_ACCESSION_COLLECTION_REPORT_DIRECTORY + "}")
    String reportsDirectory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        camelContext = new DefaultCamelContext();
    }

    @Test
    public void fsOngoingTest() throws Exception {
        FSOngoingAccessionReportRouteBuilder routeBuilder = new FSOngoingAccessionReportRouteBuilder(camelContext, reportsDirectory);
        camelContext.stop();
    }

    @Test
    public void tfsOngoingException() throws Exception {
        try{
        Mockito.when(new FSOngoingAccessionReportRouteBuilder(camelContext, reportsDirectory)).thenThrow(RuntimeException.class);}catch (Exception e){
            e.printStackTrace();
        }
    }
}
