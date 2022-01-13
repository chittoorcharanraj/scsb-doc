package org.recap.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.recap.ScsbCommonConstants;


import java.util.Arrays;


/**
 * Created by angelind on 22/6/17.
 */
@Slf4j
public class StopRouteProcessor implements Processor {


    private String routeId;

    /**
     * Instantiates a new Stop route processor.
     *
     * @param routeId the route id
     */
    public StopRouteProcessor(String routeId) {
        this.routeId = routeId;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Thread stopThread;
        stopThread = new Thread() {
            @Override
            public void run() {
                try {
                    exchange.getContext().getRouteController().stopRoute(routeId);
                } catch (Exception e) {
                    log.error("Exception while stop route : {}" , routeId);
                    log.error(ScsbCommonConstants.LOCAL_ITEM_ID, Arrays.toString(e.getStackTrace()));           }
            }
        };
        stopThread.start();
    }
}
