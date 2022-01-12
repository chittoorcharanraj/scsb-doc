package org.recap.executors;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.matchingalgorithm.MatchingAlgorithmCGDProcessor;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.jpa.MatchingAlgorithmReportDataEntity;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.repository.jpa.CollectionGroupDetailsRepository;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.repository.jpa.ItemChangeLogDetailsRepository;
import org.recap.repository.jpa.ItemDetailsRepository;
import org.recap.repository.jpa.MatchingAlgorithmReportDataDetailsRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by angelind on 6/1/17.
 */
public class MatchingAlgorithmMonographCGDCallable extends  CommonCallable implements Callable {

    private InstitutionDetailsRepository institutionDetailsRepository;
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
    private boolean isPendingMatch;
    private List<String> nonHoldingInstitutionList;

    /**
     * This method instantiates a new matching algorithm cgd callable.
     *  @param matchingAlgorithmReportDataDetailsRepository      the report data details repository
     * @param bibliographicDetailsRepository   the bibliographic details repository
     * @param pageNum                          the page num
     * @param batchSize                        the batch size
     * @param producerTemplate                 the producer template
     * @param collectionGroupMap               the collection group map
     * @param institutionMap                   the institution map
     * @param itemChangeLogDetailsRepository   the item change log details repository
     * @param collectionGroupDetailsRepository the collection group details repository
     * @param itemDetailsRepository            the item details repository
     * @param isPendingMatch                   the is pending match
     * @param institutionDetailsRepository
     */
    public MatchingAlgorithmMonographCGDCallable(MatchingAlgorithmReportDataDetailsRepository matchingAlgorithmReportDataDetailsRepository, BibliographicDetailsRepository bibliographicDetailsRepository,
                                                 int pageNum, Integer batchSize, ProducerTemplate producerTemplate, Map collectionGroupMap, Map institutionMap,
                                                 ItemChangeLogDetailsRepository itemChangeLogDetailsRepository, CollectionGroupDetailsRepository collectionGroupDetailsRepository,
                                                 ItemDetailsRepository itemDetailsRepository, boolean isPendingMatch, InstitutionDetailsRepository institutionDetailsRepository,List<String> nonHoldingInstitutionList) {
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
        this.isPendingMatch = isPendingMatch;
        this.institutionDetailsRepository = institutionDetailsRepository;
        this.nonHoldingInstitutionList = nonHoldingInstitutionList;
    }

    /**
     * This method is used to check for monograph status of bib and updates the CGD .
     * @return
     * @throws Exception
     */
    @Override
    public Object call() throws Exception {

        long from = pageNum * Long.valueOf(batchSize);
        List<MatchingAlgorithmReportDataEntity> reportDataEntities;
        if(isPendingMatch) {
            reportDataEntities = matchingAlgorithmReportDataDetailsRepository.getReportDataEntityForPendingMatchingMonographs(ScsbCommonConstants.BIB_ID, from, batchSize);
        } else {
            reportDataEntities =  matchingAlgorithmReportDataDetailsRepository.getReportDataEntityForMatchingMonographs(ScsbCommonConstants.BIB_ID, from, batchSize);
        }
        List<Integer> nonMonographRecordNums = new ArrayList<>();
        List<Integer> exceptionRecordNums = new ArrayList<>();
        Map<String, List<Integer>> unProcessedRecordNumMap = new HashMap<>();
        for(MatchingAlgorithmReportDataEntity reportDataEntity : reportDataEntities) {
            Map<Integer, ItemEntity> itemEntityMap = new HashMap<>();
            List<Integer> bibIdList = getBibIdListFromString(reportDataEntity);
            Set<String> materialTypeSet = new HashSet<>();
            MatchingAlgorithmCGDProcessor matchingAlgorithmCGDProcessor = new MatchingAlgorithmCGDProcessor(bibliographicDetailsRepository, producerTemplate, collectionGroupMap,
                    institutionMap, itemChangeLogDetailsRepository, ScsbConstants.INITIAL_MATCHING_OPERATION_TYPE, collectionGroupDetailsRepository, itemDetailsRepository,institutionDetailsRepository,nonHoldingInstitutionList);
            boolean isMonograph = matchingAlgorithmCGDProcessor.checkForMonographAndPopulateValues(materialTypeSet, itemEntityMap, bibIdList,ScsbConstants.INITIAL_MATCHING_OPERATION_TYPE);
            if(isMonograph) {
                matchingAlgorithmCGDProcessor.updateCGDProcess(itemEntityMap);
            } else {
                if(materialTypeSet.size() > 1) {
                    exceptionRecordNums.add(Integer.valueOf(reportDataEntity.getRecordNum()));
                } else {
                    if(materialTypeSet.contains(ScsbConstants.MONOGRAPHIC_SET)) {
                        nonMonographRecordNums.add(Integer.valueOf(reportDataEntity.getRecordNum()));
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(nonMonographRecordNums)) {
            unProcessedRecordNumMap.put("NonMonographRecordNums", nonMonographRecordNums);
        }
        if(CollectionUtils.isNotEmpty(exceptionRecordNums)) {
            unProcessedRecordNumMap.put("ExceptionRecordNums", exceptionRecordNums);
        }
        return unProcessedRecordNumMap;
    }
}
