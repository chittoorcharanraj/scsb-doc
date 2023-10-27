package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCaseUT;
import org.recap.camel.route.MatchingAlgorithmRouteBuilder;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Charan Raj C created on 27/10/23
 */

@ContextConfiguration(classes = MatchingAlgorithmRouteBuilderUT.Config.class)
public class MatchingAlgorithmRouteBuilderUT extends BaseTestCaseUT {

    private CamelContext camelContext;

    @InjectMocks
    MatchingAlgorithmRouteBuilder matchingAlgorithmRouteBuilder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        camelContext = new DefaultCamelContext();
    }

    @Test
    public void matchingAlgorithm() {
        MatchingAlgorithmRouteBuilder routeBuilder = new MatchingAlgorithmRouteBuilder(camelContext);
    }


    @Test
    public void matchingAlgorithmTest() throws Exception {
        try {
            Mockito.when(new MatchingAlgorithmRouteBuilder(camelContext)).thenThrow(RuntimeException.class);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    static class Config {
        @Bean
        public MatchingAlgorithmRouteBuilder matchingAlgorithmRouteBuilder(CamelContext camelContext) {
            return new MatchingAlgorithmRouteBuilder(camelContext);
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
