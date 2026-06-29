package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.PropertyKeyConstants;
import org.recap.camel.route.FSSubmitCollectionFailureReportRouteBuilder;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Charan Raj C created on 27/10/23
 */

public class FSSubmitCollectionFailureReportRouteBuilderUT extends BaseTestCaseUT {

    private CamelContext camelContext;

    @InjectMocks
    FSSubmitCollectionFailureReportRouteBuilder fsSubmitCollectionFailureReportRouteBuilder;

    @Value("${" + PropertyKeyConstants.SUBMIT_COLLECTION_REPORT_DIRECTORY + "}")
    String reportsDirectory;

    @BeforeEach
    public void setup() {        camelContext = new DefaultCamelContext();
    }

    @Test
    public void fsSubmitFailureTest() throws Exception {
        FSSubmitCollectionFailureReportRouteBuilder routeBuilder = new FSSubmitCollectionFailureReportRouteBuilder(camelContext, reportsDirectory);
        camelContext.stop();
    }

    @Test
    public void fsSubmitFailureException() throws Exception {
        try{
        Mockito.when(new FSSubmitCollectionFailureReportRouteBuilder(camelContext, reportsDirectory)).thenThrow(RuntimeException.class);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
