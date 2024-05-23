package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.camel.route.S3SubmitCollectionSummaryReportRouteBuilder;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Charan Raj C created on 20/05/24
 */
@ContextConfiguration(classes = S3SubmitCollectionSummaryReportRouteBuilderUT.Config.class)
public class S3SubmitCollectionSummaryReportRouteBuilderUT extends BaseTestCaseUT {

    private CamelContext camelContext;

    @Before
    public void setup() {
        camelContext = new DefaultCamelContext();
    }

    @Test
    public void testS3Accession() throws Exception {
        S3SubmitCollectionSummaryReportRouteBuilder routeBuilder = new S3SubmitCollectionSummaryReportRouteBuilder(camelContext, true);
    }

    static class Config {
        @Bean
        public S3SubmitCollectionSummaryReportRouteBuilder s3SubmitCollectionSummaryReportRouteBuilder(CamelContext context) {
            return new S3SubmitCollectionSummaryReportRouteBuilder(context, true);
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
