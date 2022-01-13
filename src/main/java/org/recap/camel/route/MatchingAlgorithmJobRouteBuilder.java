package org.recap.camel.route;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.controller.OngoingMatchingAlgorithmJobRestController;
import org.recap.model.solr.SolrIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Created by rajeshbabuk on 5/9/17.
 */
@Slf4j
@Component
public class MatchingAlgorithmJobRouteBuilder {

    /**
     * Instantiates a new Matching algorithm job route builder.
     *
     * @param camelContext                              the camel context
     * @param ongoingMatchingAlgorithmJobRestController the ongoing matching algorithm job rest controller
     */
    @Autowired
    public MatchingAlgorithmJobRouteBuilder(CamelContext camelContext, OngoingMatchingAlgorithmJobRestController ongoingMatchingAlgorithmJobRestController) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ScsbCommonConstants.MATCHING_ALGORITHM_JOB_INITIATE_QUEUE)
                            .routeId(ScsbConstants.MATCHING_ALGORITHM_JOB_INITIATE_ROUTE_ID)
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) {
                                    String jobId = null;
                                    try {
                                        Map<String, String> requestMap = (Map<String, String>) exchange.getIn().getBody();
                                        jobId = requestMap.get(ScsbCommonConstants.JOB_ID);
                                        log.info("Ongoing Matching Algorithm Job Initiated for Job Id : {}" , jobId);
                                        SolrIndexRequest solrIndexRequest = new SolrIndexRequest();
                                        solrIndexRequest.setProcessType(requestMap.get(ScsbCommonConstants.PROCESS_TYPE));
                                        solrIndexRequest.setCreatedDate(new SimpleDateFormat(ScsbConstants.MATCHING_BATCH_JOB_DATE_FORMAT).parse(requestMap.get(ScsbCommonConstants.CREATED_DATE)));
                                        String matchingAlgorithmJobStatus = ongoingMatchingAlgorithmJobRestController.startMatchingAlgorithmJob(solrIndexRequest);
                                        log.info("Job Id : {} Ongoing Matching Algorithm Job Status : {}", jobId, matchingAlgorithmJobStatus);
                                        exchange.getIn().setBody("JobId:" + jobId + "|" + matchingAlgorithmJobStatus);
                                    } catch (Exception ex) {
                                        exchange.getIn().setBody("JobId:" + jobId + "|" + ex.getMessage());
                                        log.info(ScsbCommonConstants.LOG_ERROR, ex);
                                    }
                                }
                            })
                            .onCompletion()
                            .to(ScsbCommonConstants.MATCHING_ALGORITHM_JOB_COMPLETION_OUTGOING_QUEUE)
                            .end();
                }
            });
        } catch (Exception ex) {
            log.error(ScsbConstants.EXCEPTION, ex);
        }
    }
}
