package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.camel.route.S3AccessionSummaryReportRouteBuilder;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Charan Raj C created on 27/10/23
 */

@ContextConfiguration(classes = S3AccessionSummaryReportRouteBuilderUT.Config.class)
public class S3AccessionSummaryReportRouteBuilderUT extends BaseTestCaseUT {
    private CamelContext camelContext;

    @Before
    public void setup() {
        camelContext = new DefaultCamelContext();
    }

    @Test
    public void testS3Accession() throws Exception {
        S3AccessionSummaryReportRouteBuilder routeBuilder = new S3AccessionSummaryReportRouteBuilder(camelContext, true, "accessionPathS3");
    }

    static class Config {
        @Bean
        public S3AccessionSummaryReportRouteBuilder s3AccessionSummaryReportRouteBuilder(CamelContext context) {
            return new S3AccessionSummaryReportRouteBuilder(context, true, "accessionPathS3");
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
