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
import org.recap.camel.route.FSSubmitCollectionExceptionReportRouteBuilder;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Charan Raj C created on 27/10/23
 */

public class FSSubmitCollectionExceptionReportRouteBuilderUT extends BaseTestCaseUT {

    private CamelContext camelContext;

    @InjectMocks
    FSSubmitCollectionExceptionReportRouteBuilder fsSubmitCollectionExceptionReportRouteBuilder;

    @Value("${" + PropertyKeyConstants.SUBMIT_COLLECTION_REPORT_DIRECTORY + "}")
    String reportsDirectory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        camelContext = new DefaultCamelContext();
    }

    @Test
    public void fsSubmitCollection() throws Exception {
        FSSubmitCollectionExceptionReportRouteBuilder routeBuilder = new FSSubmitCollectionExceptionReportRouteBuilder(camelContext, reportsDirectory);
        camelContext.stop();
    }

    @Test
    public void fsSubmitCollectionException() throws Exception {
        try{
        Mockito.when(new FSSubmitCollectionExceptionReportRouteBuilder(camelContext, reportsDirectory)).thenThrow(RuntimeException.class);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
