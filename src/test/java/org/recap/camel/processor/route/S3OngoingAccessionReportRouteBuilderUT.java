package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.PropertyKeyConstants;
import org.recap.camel.route.S3OngoingAccessionReportRouteBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Charan Raj C created on 27/10/23
 */
@ContextConfiguration(classes = S3OngoingAccessionReportRouteBuilderUT.Config.class)
public class S3OngoingAccessionReportRouteBuilderUT extends BaseTestCaseUT {

    private CamelContext camelContext;

    @Before
    public void setup() {
        camelContext = new DefaultCamelContext();
    }

    @Mock
    ApplicationContext applicationContext;

    @Value("${" + PropertyKeyConstants.S3_ONGOING_ACCESSION_COLLECTION_REPORT_DIR + "}")
    String ongoingAccessionPathS3;

    @Test
    public void testS3OngoingAccession() throws Exception {
        S3OngoingAccessionReportRouteBuilder routeBuilder = new S3OngoingAccessionReportRouteBuilder(camelContext, true, ongoingAccessionPathS3, applicationContext);
    }

    static class Config {
        @Bean
        public S3OngoingAccessionReportRouteBuilder s3OngoingAccessionReportRouteBuilder(CamelContext context, @Value("${" + PropertyKeyConstants.S3_ADD_S3_ROUTES_ON_STARTUP + "}") boolean addS3RoutesOnStartup, @Value("${" + PropertyKeyConstants.S3_ONGOING_ACCESSION_COLLECTION_REPORT_DIR + "}") String ongoingAccessionPathS3, ApplicationContext applicationContext) {
            return new S3OngoingAccessionReportRouteBuilder(context, true, ongoingAccessionPathS3, applicationContext);
        }

        @Bean
        public CamelContext camelContext() {
            return Mockito.mock(CamelContext.class);
        }

        @Bean
        public Logger logger() {
            return Mockito.mock(Logger.class);
        }
    }
}
