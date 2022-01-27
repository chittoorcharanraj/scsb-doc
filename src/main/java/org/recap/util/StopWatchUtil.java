package org.recap.util;

import lombok.extern.slf4j.Slf4j;
import org.recap.ScsbConstants;
import org.springframework.util.StopWatch;

import java.util.function.IntConsumer;
import java.util.function.Supplier;

@Slf4j
public class StopWatchUtil  {

    private StopWatchUtil(){
        throw new IllegalStateException("Util class");
    }



    public static <T> void executeAndEstimateTotalTimeTaken(Supplier<T> supplier,String functionName) {
        StopWatch stopWatchFunc = new StopWatch();
        stopWatchFunc.start();
        supplier.get();
        stopWatchFunc.stop();
        stopWatchFunc.getTotalTimeSeconds();
        log.info(ScsbConstants.LOG_EXECUTION_TIME,functionName,stopWatchFunc.getTotalTimeSeconds());
    }

    public static void executeAndEstimateTotalTimeTaken(IntConsumer consumer, Integer batchSize, String functionName) {
        StopWatch stopWatchFunc = new StopWatch();
        stopWatchFunc.start();
        consumer.accept(batchSize);
        stopWatchFunc.stop();
        stopWatchFunc.getTotalTimeSeconds();
        log.info(ScsbConstants.LOG_EXECUTION_TIME ,functionName, stopWatchFunc.getTotalTimeSeconds());
    }
}
