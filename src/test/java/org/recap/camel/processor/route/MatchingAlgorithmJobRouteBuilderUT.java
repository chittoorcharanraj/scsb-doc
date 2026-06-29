package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.recap.camel.route.MatchingAlgorithmJobRouteBuilder;
import org.recap.controller.OngoingMatchingAlgorithmJobRestController;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * @author Charan Raj C created on 27/10/23
 */

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = MatchingAlgorithmJobRouteBuilderUT.Config.class)
@TestPropertySource("classpath:application.properties")
@MockitoSettings(strictness = Strictness.LENIENT)
public class MatchingAlgorithmJobRouteBuilderUT {

    @InjectMocks
    MatchingAlgorithmJobRouteBuilder matchingAlgorithmJobRouteBuilder;
    private CamelContext camelContext;
    @Mock
    OngoingMatchingAlgorithmJobRestController ongoingMatchingAlgorithmJobRestController;

    @BeforeEach
    public void setup() {        camelContext = new DefaultCamelContext();
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

        @Bean
        public OngoingMatchingAlgorithmJobRestController ongoingMatchingAlgorithmJobRestController() {
            return Mockito.mock(OngoingMatchingAlgorithmJobRestController.class);
        }
    }
}
