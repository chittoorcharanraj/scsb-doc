package org.recap.matchingalgorithm.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.executors.MatchingAlgorithmMVMsCGDCallable;
import org.recap.executors.MatchingAlgorithmMonographCGDCallable;
import org.recap.executors.MatchingAlgorithmSerialsCGDCallable;
import org.recap.matchingalgorithm.MatchingCounter;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.CollectionGroupEntity;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.jpa.MatchingAlgorithmReportDataEntity;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.repository.jpa.CollectionGroupDetailsRepository;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.repository.jpa.ItemChangeLogDetailsRepository;
import org.recap.repository.jpa.ItemDetailsRepository;
import org.recap.repository.jpa.MatchingAlgorithmReportDataDetailsRepository;
import org.recap.service.ActiveMqQueuesInfo;
import org.recap.util.CommonUtil;
import org.recap.util.MatchingAlgorithmUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.recap.ScsbConstants.MATCHING_COUNTER_SHARED;

/**
 * Created by angelind on 11/1/17.
 */
@Slf4j
@Component
public class MatchingAlgorithmUpdateCGDService {


    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private MatchingAlgorithmReportDataDetailsRepository matchingAlgorithmReportDataDetailsRepository;

    @Autowired
    private ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    @Autowired
    private MatchingAlgorithmUtil matchingAlgorithmUtil;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private ActiveMqQueuesInfo activeMqQueuesInfo;

    @Autowired
    private CommonUtil commonUtil;

    @Value("${" + PropertyKeyConstants.NONHOLDINGID_INSTITUTION + "}")
    private List<String> nonHoldingInstitutionList;

    @Value("${" + PropertyKeyConstants.OCOLC_INSTITUTION + "}")
    private List<String> ocolcInstitutionList;

    /**
     * Gets report data details repository.
     *
     * @return the report data details repository
     */
    public MatchingAlgorithmReportDataDetailsRepository getMatchingAlgorithmReportDataDetailsRepository() {
        return matchingAlgorithmReportDataDetailsRepository;
    }

    public MatchingAlgorithmUtil getMatchingAlgorithmUtil() {
        return matchingAlgorithmUtil;
    }

    public BibliographicDetailsRepository getBibliographicDetailsRepository() {
        return bibliographicDetailsRepository;
    }

    public ProducerTemplate getProducerTemplate() {
        return producerTemplate;
    }

    public CollectionGroupDetailsRepository getCollectionGroupDetailsRepository() {
        return collectionGroupDetailsRepository;
    }

    public InstitutionDetailsRepository getInstitutionDetailsRepository() {
        return institutionDetailsRepository;
    }

    public ItemChangeLogDetailsRepository getItemChangeLogDetailsRepository() {
        return itemChangeLogDetailsRepository;
    }

    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }

    public static Logger getLogger() {
        return log;
    }

    public ActiveMqQueuesInfo getActiveMqQueuesInfo() {
        return activeMqQueuesInfo;
    }

    private ExecutorService executorService;
    private Map collectionGroupMap;
    private Map institutionMap;

    /**
     * This method is used to update cgd process for monographs.
     *
     * @param batchSize the batch size
     * @throws IOException         the io exception
     * @throws SolrServerException the solr server exception
     */
    public void updateCGDProcessForMonographs(Integer batchSize) throws IOException, SolrServerException, InterruptedException {
        getLogger().info("Start CGD Process For Monographs.");
        getMatchingAlgorithmUtil().populateMatchingCounter();
        ExecutorService executor = getExecutorService(50);

        processCallablesForMonographs(batchSize, executor, false);
        Integer updateItemsQ = getUpdatedItemsQ();
        processCallablesForMonographs(batchSize, executor, true);
        getMatchingAlgorithmUtil().saveCGDUpdatedSummaryReport(ScsbConstants.MATCHING_SUMMARY_MONOGRAPH);
        List<String> allInstitutionCodesExceptSupportInstitution = commonUtil.findAllInstitutionCodesExceptSupportInstitution();
        for (String institutionCode : allInstitutionCodesExceptSupportInstitution) {
            log.info("{} Final Counter Value:{} " ,institutionCode, MatchingCounter.getSpecificInstitutionCounterMap(institutionCode).get(MATCHING_COUNTER_SHARED));
        }

        while (updateItemsQ != 0) {
            Thread.sleep(10000);
            updateItemsQ = getActiveMqQueuesInfo().getActivemqQueuesInfo(ScsbConstants.UPDATE_ITEMS_Q);
        }
        executor.shutdown();
    }

    private Integer getUpdatedItemsQ() throws InterruptedException {
        Integer updateItemsQ = getActiveMqQueuesInfo().getActivemqQueuesInfo(ScsbConstants.UPDATE_ITEMS_Q);
        if (updateItemsQ != null) {
            while (updateItemsQ != 0) {
                Thread.sleep(10000);
                updateItemsQ = getActiveMqQueuesInfo().getActivemqQueuesInfo(ScsbConstants.UPDATE_ITEMS_Q);
            }
        }
        return updateItemsQ;
    }

    private void processCallablesForMonographs(Integer batchSize, ExecutorService executor, boolean isPendingMatch) {
        List<Callable<Integer>> callables = new ArrayList<>();
        long countOfRecordNum;
        if(isPendingMatch) {
            countOfRecordNum = getMatchingAlgorithmReportDataDetailsRepository().getCountOfRecordNumForMatchingPendingMonograph(ScsbCommonConstants.BIB_ID);
        } else {
            countOfRecordNum = getMatchingAlgorithmReportDataDetailsRepository().getCountOfRecordNumForMatchingMonograph(ScsbCommonConstants.BIB_ID);
        }
        log.info(ScsbConstants.TOTAL_RECORDS + "{}", countOfRecordNum);
        int totalPagesCount = (int) (countOfRecordNum / batchSize);
        log.info(ScsbConstants.TOTAL_PAGES + "{}" , totalPagesCount);
        for(int pageNum = 0; pageNum < totalPagesCount + 1; pageNum++) {
            Callable callable = new MatchingAlgorithmMonographCGDCallable(getMatchingAlgorithmReportDataDetailsRepository(), getBibliographicDetailsRepository(), pageNum, batchSize, getProducerTemplate(),
                    getCollectionGroupMap(), getInstitutionEntityMap(), getItemChangeLogDetailsRepository(), getCollectionGroupDetailsRepository(), getItemDetailsRepository(),isPendingMatch,getInstitutionDetailsRepository(),nonHoldingInstitutionList, ocolcInstitutionList);
            callables.add(callable);
        }
        Map<String, List<Integer>> unProcessedRecordNumberMap = executeCallables(executor, callables);

        List<Integer> nonMonographRecordNums = unProcessedRecordNumberMap.get(ScsbConstants.NON_MONOGRAPH_RECORD_NUMS);
        List<Integer> exceptionRecordNums = unProcessedRecordNumberMap.get(ScsbConstants.EXCEPTION_RECORD_NUMS);

        getMatchingAlgorithmUtil().updateMonographicSetRecords(nonMonographRecordNums, batchSize);

        getMatchingAlgorithmUtil().updateExceptionRecords(exceptionRecordNums, batchSize);
    }

    /**
     * Update cgd process for serials.
     *
     * @param batchSize the batch size
     * @throws IOException         the io exception
     * @throws SolrServerException the solr server exception
     */
    public void updateCGDProcessForSerials(Integer batchSize) throws IOException, SolrServerException, InterruptedException {
        log.info("Start CGD Process For Serials.");
        getMatchingAlgorithmUtil().populateMatchingCounter();
        ExecutorService executor = getExecutorService(50);
        List<Callable<Integer>> callables = new ArrayList<>();
        long countOfRecordNum = getMatchingAlgorithmReportDataDetailsRepository().getCountOfRecordNumForMatchingSerials(ScsbCommonConstants.BIB_ID);
        log.info(ScsbConstants.TOTAL_RECORDS + "{}", countOfRecordNum);
        int totalPagesCount = (int) (countOfRecordNum / batchSize);
        log.info(ScsbConstants.TOTAL_PAGES + "{}" , totalPagesCount);
        for(int pageNum=0; pageNum < totalPagesCount + 1; pageNum++) {
            Callable callable = new MatchingAlgorithmSerialsCGDCallable(getMatchingAlgorithmReportDataDetailsRepository(), getBibliographicDetailsRepository(), pageNum, batchSize, getProducerTemplate(), getCollectionGroupMap(),
                    getInstitutionEntityMap(), getItemChangeLogDetailsRepository(), getCollectionGroupDetailsRepository(), getItemDetailsRepository(),institutionDetailsRepository,nonHoldingInstitutionList, ocolcInstitutionList);
            callables.add(callable);
        }
        setCGDUpdateSummaryReport(executor, callables, ScsbConstants.MATCHING_SUMMARY_SERIAL);
    }

    /**
     * Update cgd process for mv ms.
     *
     * @param batchSize the batch size
     * @throws IOException         the io exception
     * @throws SolrServerException the solr server exception
     */
    public void updateCGDProcessForMVMs(Integer batchSize) throws IOException, SolrServerException, InterruptedException {
        log.info("Start CGD Process For MVMs.");

        getMatchingAlgorithmUtil().populateMatchingCounter();

        ExecutorService executor = getExecutorService(50);
        List<Callable<Integer>> callables = new ArrayList<>();
        long countOfRecordNum = getMatchingAlgorithmReportDataDetailsRepository().getCountOfRecordNumForMatchingMVMs(ScsbCommonConstants.BIB_ID);
        log.info(ScsbConstants.TOTAL_RECORDS + "{}", countOfRecordNum);
        int totalPagesCount = (int) (countOfRecordNum / batchSize);
        log.info(ScsbConstants.TOTAL_PAGES + "{}" , totalPagesCount);
        for(int pageNum=0; pageNum < totalPagesCount + 1; pageNum++) {
            Callable callable = new MatchingAlgorithmMVMsCGDCallable(getMatchingAlgorithmReportDataDetailsRepository(), getBibliographicDetailsRepository(), pageNum, batchSize, getProducerTemplate(), getCollectionGroupMap(),
                    getInstitutionEntityMap(), getItemChangeLogDetailsRepository(), getCollectionGroupDetailsRepository(), getItemDetailsRepository(),institutionDetailsRepository,nonHoldingInstitutionList, ocolcInstitutionList);
            callables.add(callable);
        }
        setCGDUpdateSummaryReport(executor, callables, ScsbConstants.MATCHING_SUMMARY_MVM);
    }

    private Map<String, List<Integer>> executeCallables(ExecutorService executorService, List<Callable<Integer>> callables) {
        List<Integer> nonMonographRecordNumbers = new ArrayList<>();
        List<Integer> exceptionRecordNumbers = new ArrayList<>();
        Map<String, List<Integer>> unProcessedRecordNumberMap = new HashMap<>();
        List<Future<Integer>> futures = getFutures(executorService, callables);

        if(futures != null) {
            for (Iterator<Future<Integer>> iterator = futures.iterator(); iterator.hasNext(); ) {
                Future future = iterator.next();
                fetchAndPopulateRecordNumbers(nonMonographRecordNumbers, exceptionRecordNumbers, future);
            }
        }
        unProcessedRecordNumberMap.put(ScsbConstants.NON_MONOGRAPH_RECORD_NUMS, nonMonographRecordNumbers);
        unProcessedRecordNumberMap.put(ScsbConstants.EXCEPTION_RECORD_NUMS, exceptionRecordNumbers);
        return unProcessedRecordNumberMap;
    }

    private void fetchAndPopulateRecordNumbers(List<Integer> nonMonographRecordNumbers, List<Integer> exceptionRecordNumbers, Future future) {
        try {
            Map<String, List<Integer>> recordNumberMap = (Map<String, List<Integer>>) future.get();
            if(recordNumberMap != null) {
                if(CollectionUtils.isNotEmpty(recordNumberMap.get(ScsbConstants.NON_MONOGRAPH_RECORD_NUMS))) {
                    nonMonographRecordNumbers.addAll(recordNumberMap.get(ScsbConstants.NON_MONOGRAPH_RECORD_NUMS));
                }
                if(CollectionUtils.isNotEmpty(recordNumberMap.get(ScsbConstants.EXCEPTION_RECORD_NUMS))) {
                    exceptionRecordNumbers.addAll(recordNumberMap.get(ScsbConstants.EXCEPTION_RECORD_NUMS));
                }
            }
        } catch (InterruptedException e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
    }

    private List<Future<Integer>> getFutures(ExecutorService executorService, List<Callable<Integer>> callables) {
        List<Future<Integer>> collectedFutures = null;
        try {
            List<Future<Integer>> futures = executorService.invokeAll(callables);
            collectedFutures = futures.stream().map(future -> {
                try {
                    future.get();
                    return future;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                } catch (ExecutionException e) {
                    throw new IllegalStateException(e);
                }
            }).collect(Collectors.toCollection(ArrayList::new));
            log.info("No of Futures Collected : {}", collectedFutures.size());
        } catch (Exception e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return collectedFutures;
    }

    private ExecutorService getExecutorService(Integer numThreads) {
        if (null == executorService || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(numThreads);
        }
        return executorService;
    }

    /**
     * This method gets items count for serials matching.
     *
     * @param batchSize the batch size
     */
    public void getItemsCountForSerialsMatching(Integer batchSize) {
        long countOfRecordNum = getMatchingAlgorithmReportDataDetailsRepository().getCountOfRecordNumForMatchingSerials(ScsbCommonConstants.BIB_ID);
        log.info(ScsbConstants.TOTAL_RECORDS + "{}", countOfRecordNum);
        int totalPagesCount = (int) (countOfRecordNum / batchSize);
        log.info(ScsbConstants.TOTAL_PAGES + "{}" , totalPagesCount);
        for(int pageNum = 0; pageNum < totalPagesCount + 1; pageNum++) {
            long from = pageNum * Long.valueOf(batchSize);
            List<MatchingAlgorithmReportDataEntity> reportDataEntities =  getMatchingAlgorithmReportDataDetailsRepository().getReportDataEntityForMatchingSerials(ScsbCommonConstants.BIB_ID, from, batchSize);
            List<List<MatchingAlgorithmReportDataEntity>> reportDataEntityList = Lists.partition(reportDataEntities, 1000);
            for(List<MatchingAlgorithmReportDataEntity> dataEntityList : reportDataEntityList) {
                updateMatchingCounter(pageNum, dataEntityList);
            }
        }
    }

    private void updateMatchingCounter(int pageNum, List<MatchingAlgorithmReportDataEntity> dataEntityList) {
        List<Integer> bibIds = new ArrayList<>();
        for(MatchingAlgorithmReportDataEntity reportDataEntity : dataEntityList) {
            List<String> bibIdList = Arrays.asList(reportDataEntity.getHeaderValue().split(","));
            bibIds.addAll(bibIdList.stream().map(Integer::parseInt).collect(Collectors.toCollection(ArrayList::new)));
        }
        log.info("Bibs count in Page {} : {} " ,pageNum,bibIds.size());
        List<BibliographicEntity> bibliographicEntities = getBibliographicDetailsRepository().findByIdIn(bibIds);
        for(BibliographicEntity bibliographicEntity : bibliographicEntities) {
            for(ItemEntity itemEntity : bibliographicEntity.getItemEntities()) {
                if(itemEntity.getCollectionGroupId().equals(getCollectionGroupMap().get(ScsbCommonConstants.SHARED_CGD))) {
                    MatchingCounter.updateCGDCounter(itemEntity.getInstitutionEntity().getInstitutionCode(),false);
                }
            }
        }
    }

    /**
     * This method gets all collection group and puts it in a map.
     *
     * @return the collection group map
     */
    public Map getCollectionGroupMap() {
        if (null == collectionGroupMap) {
            collectionGroupMap = new HashMap();
            Iterable<CollectionGroupEntity> collectionGroupEntities = getCollectionGroupDetailsRepository().findAll();
            for (Iterator<CollectionGroupEntity> iterator = collectionGroupEntities.iterator(); iterator.hasNext(); ) {
                CollectionGroupEntity collectionGroupEntity = iterator.next();
                collectionGroupMap.put(collectionGroupEntity.getCollectionGroupCode(), collectionGroupEntity.getId());
            }
        }
        return collectionGroupMap;
    }

    /**
     * This method gets all institution entity and puts it in a map.
     *
     * @return the institution entity map
     */
    public Map getInstitutionEntityMap() {
        if (null == institutionMap) {
            institutionMap = new HashMap();
            Iterable<InstitutionEntity> institutionEntities = getInstitutionDetailsRepository().findAll();
            for (Iterator<InstitutionEntity> iterator = institutionEntities.iterator(); iterator.hasNext(); ) {
                InstitutionEntity institutionEntity = iterator.next();
                institutionMap.put(institutionEntity.getInstitutionCode(), institutionEntity.getId());
            }
        }
        return institutionMap;
    }

    private void setCGDUpdateSummaryReport(ExecutorService executor, List<Callable<Integer>> callables, String reportName) throws InterruptedException {
        getFutures(executor, callables);
        getMatchingAlgorithmUtil().saveCGDUpdatedSummaryReport(reportName);
        List<String> allInstitutionCodesExceptSupportInstitution = commonUtil.findAllInstitutionCodesExceptSupportInstitution();
        for (String institutionCode : allInstitutionCodesExceptSupportInstitution) {
            log.info("{} Final Counter Value:{} " ,institutionCode, MatchingCounter.getSpecificInstitutionCounterMap(institutionCode).get(MATCHING_COUNTER_SHARED));
        }
        getUpdatedItemsQ();
        executor.shutdown();

    }
}
