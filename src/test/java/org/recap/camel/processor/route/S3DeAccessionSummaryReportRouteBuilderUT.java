package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.camel.route.S3DeAccessionSummaryReportRouteBuilder;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Charan Raj C created on 27/10/23
 */

@ContextConfiguration(classes = S3DeAccessionSummaryReportRouteBuilderUT.Config.class)
public class S3DeAccessionSummaryReportRouteBuilderUT extends BaseTestCaseUT {
    private CamelContext camelContext;

    @Before
    public void setup() {
        camelContext = new DefaultCamelContext();
    }

    @Test
    public void testS3DeAccession() throws Exception {
        S3DeAccessionSummaryReportRouteBuilder routeBuilder = new S3DeAccessionSummaryReportRouteBuilder(camelContext, true, "accessionPathS3");
    }

    static class Config {
        @Bean
        public S3DeAccessionSummaryReportRouteBuilder s3DeAccessionSummaryReportRouteBuilder(CamelContext context) {
            return new S3DeAccessionSummaryReportRouteBuilder(context, true, "deaccessionPathS3");
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
