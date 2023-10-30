package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.PropertyKeyConstants;
import org.recap.camel.route.S3MatchingReportsRouteBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Charan Raj C created on 27/10/23
 */
@ContextConfiguration(classes = S3MatchingReportsRouteBuilderUT.Config.class)
public class S3MatchingReportsRouteBuilderUT extends BaseTestCaseUT {

    private CamelContext camelContext;

    @Before
    public void setup() {
        camelContext = new DefaultCamelContext();
    }

    @Mock
    ApplicationContext applicationContext;

    @Test
    public void testS3MatchingReports() throws Exception {
        S3MatchingReportsRouteBuilder routeBuilder = new S3MatchingReportsRouteBuilder(camelContext, true, "matchingReportsDirectory", "s3MatchingReportsDirectory", applicationContext);
    }

    static class Config {
        @Bean
        public S3MatchingReportsRouteBuilder s3MatchingReportsRouteBuilder(CamelContext context, @Value("${" + PropertyKeyConstants.S3_ADD_S3_ROUTES_ON_STARTUP + "}") boolean addS3RoutesOnStartup, @Value("${" + PropertyKeyConstants.ONGOING_MATCHING_REPORT_DIRECTORY + "}") String matchingReportsDirectory,
                                                                           @Value("${" + PropertyKeyConstants.S3_MATCHINGALGORITHM_REPORTS_DIR + "}") String s3MatchingReportsDirectory, ApplicationContext applicationContext) {
            return new S3MatchingReportsRouteBuilder(context, true, matchingReportsDirectory, s3MatchingReportsDirectory, applicationContext);
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
