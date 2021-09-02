package org.recap.executors;

import org.apache.camel.ProducerTemplate;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.matchingalgorithm.MatchingAlgorithmCGDProcessor;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.jpa.MatchingAlgorithmReportDataEntity;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.repository.jpa.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by angelind on 31/5/17.
 */
public class MatchingAlgorithmMVMsCGDCallable extends  CommonCallable implements Callable {

    private MatchingAlgorithmReportDataDetailsRepository matchingAlgorithmReportDataDetailsRepository;
    private BibliographicDetailsRepository bibliographicDetailsRepository;
    private int pageNum;
    private Integer batchSize;
    private ProducerTemplate producerTemplate;
    private Map collectionGroupMap;
    private Map institutionMap;
    private ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;
    private ItemDetailsRepository itemDetailsRepository;
    private InstitutionDetailsRepository institutionDetailsRepository;
    private List<String> nonHoldingInstitutionList;

    public MatchingAlgorithmMVMsCGDCallable(MatchingAlgorithmReportDataDetailsRepository matchingAlgorithmReportDataDetailsRepository, BibliographicDetailsRepository bibliographicDetailsRepository, int pageNum, Integer batchSize,
                                            ProducerTemplate producerTemplate, Map collectionGroupMap, Map institutionMap, ItemChangeLogDetailsRepository itemChangeLogDetailsRepository,
                                            CollectionGroupDetailsRepository collectionGroupDetailsRepository, ItemDetailsRepository itemDetailsRepository, InstitutionDetailsRepository institutionDetailsRepository,List<String> nonHoldingInstitutionList) {
        this.matchingAlgorithmReportDataDetailsRepository = matchingAlgorithmReportDataDetailsRepository;
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
        this.pageNum = pageNum;
        this.batchSize = batchSize;
        this.producerTemplate = producerTemplate;
        this.collectionGroupMap = collectionGroupMap;
        this.institutionMap = institutionMap;
        this.itemChangeLogDetailsRepository = itemChangeLogDetailsRepository;
        this.collectionGroupDetailsRepository = collectionGroupDetailsRepository;
        this.itemDetailsRepository = itemDetailsRepository;
        this.institutionDetailsRepository =institutionDetailsRepository;
        this.nonHoldingInstitutionList=nonHoldingInstitutionList;
    }

    @Override
    public Object call() throws Exception {
        long from = pageNum * Long.valueOf(batchSize);
        List<MatchingAlgorithmReportDataEntity> reportDataEntities =  matchingAlgorithmReportDataDetailsRepository.getReportDataEntityForMatchingMVMs(ScsbCommonConstants.BIB_ID, from, batchSize);
        for(MatchingAlgorithmReportDataEntity reportDataEntity : reportDataEntities) {
            Map<Integer, ItemEntity> itemEntityMap = new HashMap<>();
            List<Integer> bibIdList = getBibIdListFromString(reportDataEntity);
            MatchingAlgorithmCGDProcessor matchingAlgorithmCGDProcessor = new MatchingAlgorithmCGDProcessor(bibliographicDetailsRepository, producerTemplate, collectionGroupMap,
                    institutionMap, itemChangeLogDetailsRepository, ScsbConstants.INITIAL_MATCHING_OPERATION_TYPE, collectionGroupDetailsRepository, itemDetailsRepository, institutionDetailsRepository,nonHoldingInstitutionList);
            matchingAlgorithmCGDProcessor.populateItemEntityMap(itemEntityMap, bibIdList);
            matchingAlgorithmCGDProcessor.updateItemsCGD(itemEntityMap);
        }
        return null;
    }

}
