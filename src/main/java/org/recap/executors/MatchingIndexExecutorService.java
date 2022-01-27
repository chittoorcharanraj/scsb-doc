package org.recap.executors;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.admin.SolrAdmin;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.repository.solr.main.BibSolrCrudRepository;
import org.recap.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by angelind on 30/1/17.
 */
@Slf4j
public abstract class MatchingIndexExecutorService {


    @Autowired
    SolrAdmin solrAdmin;

    @Autowired
    ProducerTemplate producerTemplate;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    BibSolrCrudRepository bibSolrCrudRepository;

    @Value("${" + PropertyKeyConstants.SOLR_SERVER_PROTOCOL + "}")
    String solrServerProtocol;

    @Value("${" + PropertyKeyConstants.SOLR_PARENT_CORE + "}")
    String solrCore;

    @Value("${" + PropertyKeyConstants.SOLR_URL + "}")
    String solrUrl;

    @Value("${" + PropertyKeyConstants.SOLR_ROUTER_URI_TYPE + "}")
    String solrRouterURI;

    @Value("${" + PropertyKeyConstants.MATCHING_ALGORITHM_INDEXING_BATCHSIZE + "}")
    Integer batchSize;

    @Value("${" + PropertyKeyConstants.MATCHING_ALGORITHM_COMMIT_INTERVAL + "}")
    Integer commitInterval;

    /**
     * This method is used for indexing the records during the matching algorithm process.
     *
     * @param operationType the operation type
     * @return the integer
     * @throws InterruptedException the interrupted exception
     */
    @Autowired
    DateUtil dateUtil;

    public Integer indexingForMatchingAlgorithm(String operationType, Date updatedDate) throws InterruptedException {
        StopWatch stopWatch1 = new StopWatch();
        stopWatch1.start();
        Integer numThreads = 5;
        Integer docsPerThread = batchSize;
        Integer commitIndexesInterval = commitInterval;
        String coreName = solrCore;
        Integer totalBibsProcessed = 0;
        Date fromDate = dateUtil.getFromDate(updatedDate);
        Date currentDate = new Date();

        try {
            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
            Integer totalDocCount = getTotalDocCount(operationType, fromDate, currentDate);
            log.info("Total doc count -> {}" , totalDocCount);
            if(totalDocCount > 0) {
                int quotient = totalDocCount / (docsPerThread);
                int remainder = totalDocCount % (docsPerThread);
                Integer loopCount = remainder == 0 ? quotient : quotient + 1;
                log.info("Loop Count Value : {}",loopCount);
                log.info("Commit Indexes Interval : {}",commitIndexesInterval);
                Integer callableCountByCommitInterval = commitIndexesInterval / (docsPerThread);
                if (callableCountByCommitInterval == 0) {
                    callableCountByCommitInterval = 1;
                }
                log.info("Number of callables to execute to commit indexes : {}",callableCountByCommitInterval);

                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                List<Callable<Integer>> callables = new ArrayList<>();
                for (int pageNum = 0; pageNum < loopCount; pageNum++) {
                    Callable callable = getCallable(coreName, pageNum, docsPerThread, operationType, fromDate, currentDate);
                    callables.add(callable);
                }

                int futureCount = 0;
                List<List<Callable<Integer>>> partitions = Lists.partition(new ArrayList<>(callables), callableCountByCommitInterval);
                for (List<Callable<Integer>> partitionCallables : partitions) {
                    List<Future<Integer>> futures = executorService.invokeAll(partitionCallables);
                    log.info("No of Futures Added : {}", futures.size());
                    List<Future<Integer>> collectedFutures = futures.stream().map(future -> {
                        try {
                            future.get();
                            return future;
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }).collect(Collectors.toList());
                    log.info("No of Futures Collected : {}", collectedFutures.size());

                    int numOfBibsProcessed = 0;
                    for (Future<Integer> future : collectedFutures) {
                        try {
                            Integer entitiesCount = future.get();
                            numOfBibsProcessed += entitiesCount;
                            totalBibsProcessed += entitiesCount;
                            log.info("Num of bibs fetched by thread : {}", entitiesCount);
                            futureCount++;
                        } catch (ExecutionException e) {
                            log.error(ScsbCommonConstants.LOG_ERROR, e);
                        }
                    }
                    log.info("Num of Bibs Processed and indexed to core{} on commit interval : {} ",coreName,numOfBibsProcessed);
                    log.info("Total Num of Bibs Processed and indexed to core {} : {}",coreName, totalBibsProcessed);
                }
                log.info("Total futures executed: {}",futureCount);
                stopWatch.stop();
                log.info("Time taken to fetch {}  Bib Records and index to recap core :  {} seconds ",totalBibsProcessed,stopWatch.getTotalTimeSeconds());
                executorService.shutdown();
            } else {
                log.info("No records found to index for the criteria");
            }
        } catch (Exception e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        stopWatch1.stop();
        log.info("Total time taken: {} secs",stopWatch1.getTotalTimeSeconds());
        return totalBibsProcessed;
    }

    /**
     * This method gets the appropriate callabe which is to be processed by thread to generate solr input documents and index to solr
     *
     * @param coreName      the core name
     * @param pageNum       the page num
     * @param docsPerpage   the docs perpage
     * @param operationType the operation type
     * @return the callable
     */
    public abstract Callable getCallable(String coreName, int pageNum, int docsPerpage, String operationType, Date from, Date to);

    /**
     * This method gets total doc count based on the operation type.
     *
     * @param operationType the operation type
     * @return the total doc count
     */
    protected abstract Integer getTotalDocCount(String operationType, Date fromDate, Date toDate);
}
