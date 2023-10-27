package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCaseUT;
import org.recap.camel.route.MatchingAlgorithmJobRouteBuilder;
import org.recap.controller.OngoingMatchingAlgorithmJobRestController;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Charan Raj C created on 27/10/23
 */

@ContextConfiguration(classes = MatchingAlgorithmJobRouteBuilderUT.Config.class)
public class MatchingAlgorithmJobRouteBuilderUT extends BaseTestCaseUT {

    @InjectMocks
    MatchingAlgorithmJobRouteBuilder matchingAlgorithmJobRouteBuilder;
    private CamelContext camelContext;
    @Mock
    OngoingMatchingAlgorithmJobRestController ongoingMatchingAlgorithmJobRestController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        camelContext = new DefaultCamelContext();
    }

    @Test
    public void matchingAlgorithmJobRouteBuilderTest() {
        MatchingAlgorithmJobRouteBuilder matchingAlgorithmJobRouteBuilde = new MatchingAlgorithmJobRouteBuilder(camelContext,ongoingMatchingAlgorithmJobRestController);
    }

    static class Config {
        @Bean
        public MatchingAlgorithmJobRouteBuilder matchingAlgorithmJobRouteBuilder(CamelContext camelContext, OngoingMatchingAlgorithmJobRestController ongoingMatchingAlgorithmJobRestController) {
            return new MatchingAlgorithmJobRouteBuilder(camelContext,ongoingMatchingAlgorithmJobRestController);
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
