package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.PropertyKeyConstants;
import org.recap.camel.route.S3SubmitCollectionReportRouteBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Charan Raj C created on 27/10/23
 */

@ContextConfiguration(classes = S3SubmitCollectionReportRouteBuilderUT.Config.class)
public class S3SubmitCollectionReportRouteBuilderUT extends BaseTestCaseUT {

    private CamelContext camelContext;

    @Before
    public void setup() {
        camelContext = new DefaultCamelContext();
    }

    @Value("${" + PropertyKeyConstants.S3_SUBMIT_COLLECTION_SUPPORT_TEAM_REPORT_DIR + "}")
    String submitCollectionS3ReportPath;

    @Test
    public void testS3SubmitReport() throws Exception {
        S3SubmitCollectionReportRouteBuilder routeBuilder = new S3SubmitCollectionReportRouteBuilder(camelContext, true, submitCollectionS3ReportPath);
    }

    static class Config {
        @Bean
        public S3SubmitCollectionReportRouteBuilder s3SubmitCollectionReportRouteBuilder(CamelContext context, @Value("${" + PropertyKeyConstants.S3_ADD_S3_ROUTES_ON_STARTUP + "}") boolean addS3RoutesOnStartup, @Value("${" + PropertyKeyConstants.S3_SUBMIT_COLLECTION_SUPPORT_TEAM_REPORT_DIR + "}") String submitCollectionS3ReportPath)  {
            return new S3SubmitCollectionReportRouteBuilder(context, true, submitCollectionS3ReportPath);
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
