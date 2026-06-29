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
import org.recap.camel.route.S3SolrExceptionRecordRouteBuilder;
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
@ContextConfiguration(classes = S3SolrExceptionRecordRouteBuilderUT.Config.class)
@TestPropertySource("classpath:application.properties")
@MockitoSettings(strictness = Strictness.LENIENT)
public class S3SolrExceptionRecordRouteBuilderUT {

    private CamelContext camelContext;

    @BeforeEach
    public void setup() {
        camelContext = new DefaultCamelContext();
    }

    @Value("${" + PropertyKeyConstants.S3_SOLR_REPORTS_DIR + "}")
    String solrReportsS3Path;

    @Test
    public void testS3SolrException() throws Exception {
        S3SolrExceptionRecordRouteBuilder routeBuilder = new S3SolrExceptionRecordRouteBuilder(camelContext, true, solrReportsS3Path);
    }

    static class Config {
        @Bean
        public S3SolrExceptionRecordRouteBuilder s3SolrExceptionRecordRouteBuilder(CamelContext context, @Value("${" + PropertyKeyConstants.S3_ADD_S3_ROUTES_ON_STARTUP + "}") boolean addS3RoutesOnStartup, @Value("${" + PropertyKeyConstants.S3_SOLR_REPORTS_DIR + "}") String solrReportsS3Path) {
            return new S3SolrExceptionRecordRouteBuilder(context, true, solrReportsS3Path);
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
