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
import org.recap.camel.route.FSSubmitCollectionSuccessReportRouteBuilder;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Charan Raj C created on 27/10/23
 */

public class FSSubmitCollectionSuccessReportRouteBuilderUT extends BaseTestCaseUT {

    private CamelContext camelContext;

    @InjectMocks
    FSSubmitCollectionSuccessReportRouteBuilder fsSubmitCollectionSuccessReportRouteBuilder;

    @Value("${" + PropertyKeyConstants.SUBMIT_COLLECTION_REPORT_DIRECTORY + "}")
    String reportsDirectory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        camelContext = new DefaultCamelContext();
    }

    @Test
    public void fsSubmitSuccess() throws Exception {
        FSSubmitCollectionSuccessReportRouteBuilder routeBuilder = new FSSubmitCollectionSuccessReportRouteBuilder(camelContext, reportsDirectory);
        camelContext.stop();
    }

    @Test
    public void fsSubmitSuccessException() throws Exception {
        try{
        Mockito.when(new FSSubmitCollectionSuccessReportRouteBuilder(camelContext, reportsDirectory)).thenThrow(RuntimeException.class);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
