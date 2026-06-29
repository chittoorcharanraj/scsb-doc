package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.recap.PropertyKeyConstants;
import org.recap.camel.route.S3SubmitCollectionSuccessReportRouteBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.TestPropertySource;

/**
 * @author Charan Raj C created on 27/10/23
 */

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = S3SubmitCollectionSuccessReportRouteBuilderUT.Config.class)
@TestPropertySource("classpath:application.properties")
@MockitoSettings(strictness = Strictness.LENIENT)
public class S3SubmitCollectionSuccessReportRouteBuilderUT {

    private CamelContext camelContext;

    @BeforeEach
    public void setup() {
        camelContext = new DefaultCamelContext();
    }

    @Value("${" + PropertyKeyConstants.S3_SUBMIT_COLLECTION_SUPPORT_TEAM_REPORT_DIR + "}")
    String submitCollectionS3ReportPath;

    @Test
    public void testS3SubmitReport() throws Exception {
        S3SubmitCollectionSuccessReportRouteBuilder routeBuilder = new S3SubmitCollectionSuccessReportRouteBuilder(camelContext, true, submitCollectionS3ReportPath);
    }

    static class Config {
        @Bean
        public S3SubmitCollectionSuccessReportRouteBuilder s3SubmitCollectionSuccessReportRouteBuilder(CamelContext context, @Value("${" + PropertyKeyConstants.S3_ADD_S3_ROUTES_ON_STARTUP + "}") boolean addS3RoutesOnStartup, @Value("${" + PropertyKeyConstants.S3_SUBMIT_COLLECTION_SUPPORT_TEAM_REPORT_DIR + "}") String submitCollectionS3ReportPath)  {
            return new S3SubmitCollectionSuccessReportRouteBuilder(context, true, submitCollectionS3ReportPath);
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
