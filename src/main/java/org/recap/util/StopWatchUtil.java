package org.recap.util;

import org.recap.ScsbConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class StopWatchUtil  {

    private StopWatchUtil(){
        throw new IllegalStateException("Util class");
    }

    private static final Logger logger = LoggerFactory.getLogger(StopWatchUtil.class);

    public static <T> void executeAndEstimateTotalTimeTaken(Supplier<T> supplier,String functionName) {
        StopWatch stopWatchFunc = new StopWatch();
        stopWatchFunc.start();
        supplier.get();
        stopWatchFunc.stop();
        stopWatchFunc.getTotalTimeSeconds();
        logger.info(ScsbConstants.LOG_EXECUTION_TIME,functionName,stopWatchFunc.getTotalTimeSeconds());
    }

    public static void executeAndEstimateTotalTimeTaken(IntConsumer consumer, Integer batchSize, String functionName) {
        StopWatch stopWatchFunc = new StopWatch();
        stopWatchFunc.start();
        consumer.accept(batchSize);
        stopWatchFunc.stop();
        stopWatchFunc.getTotalTimeSeconds();
        logger.info(ScsbConstants.LOG_EXECUTION_TIME ,functionName, stopWatchFunc.getTotalTimeSeconds());
    }
}
