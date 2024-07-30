package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.camel.route.S3SubmitCollectionSummaryReportRouteBuilder;
import org.recap.camel.route.SolrRouteBuilder;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Charan Raj C created on 20/05/24
 */
@ContextConfiguration(classes = SolrRouteBuilderUT.Config.class)
public class SolrRouteBuilderUT extends BaseTestCaseUT {
    private CamelContext camelContext;

    @Before
    public void setup() {
        camelContext = new DefaultCamelContext();
    }

    @Test
    public void testS3Accession() throws Exception {
        SolrRouteBuilder routeBuilder = new SolrRouteBuilder(camelContext, "solrUri", "solrCore");
    }

    static class Config {
        @Bean
        public SolrRouteBuilder solrRouteBuilder(CamelContext context) {
            return new SolrRouteBuilder(context, "solrUri", "solrCore");
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
