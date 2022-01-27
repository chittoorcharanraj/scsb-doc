package org.recap.camel.route;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.recap.ScsbCommonConstants;
import org.recap.camel.processor.ReportProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by angelind on 28/9/16.
 */
@Slf4j
@Component
public class ReportsRouteBuilder {

    /**
     * This method instantiates a new reports route builder to save in database.
     *
     * @param camelContext    the camel context
     * @param reportProcessor the report processor
     */
    @Autowired
    public ReportsRouteBuilder(CamelContext camelContext, ReportProcessor reportProcessor) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ScsbCommonConstants.REPORT_Q + "?concurrentConsumers=10")
                            .routeId(ScsbCommonConstants.REPORT_ROUTE_ID).threads(10)
                            .process(reportProcessor);
                }
            });
        } catch (Exception e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
    }
}
