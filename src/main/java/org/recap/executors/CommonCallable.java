package org.recap.executors;

import org.apache.camel.ProducerTemplate;
import org.apache.solr.common.SolrInputDocument;
import org.recap.ScsbCommonConstants;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.MatchingAlgorithmReportDataEntity;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.repository.jpa.HoldingsDetailsRepository;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.solr.core.SolrTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class CommonCallable {

    public List<SolrInputDocument> setSolrInputDocuments(Page<BibliographicEntity> bibliographicEntities, SolrTemplate solrTemplate, BibliographicDetailsRepository bibliographicDetailsRepository,
                                                         HoldingsDetailsRepository holdingsDetailsRepository, ProducerTemplate producerTemplate, Logger logger,List<String> nonHoldingInstitutionList, List<String> ocolcInstitutionList) {
        logger.info("Num Bibs Fetched : " + bibliographicEntities.getNumberOfElements());
        Iterator<BibliographicEntity> iterator = bibliographicEntities.iterator();


        ExecutorService executorService = Executors.newFixedThreadPool(50);
        List<Future> futures = new ArrayList<>();
        while (iterator.hasNext()) {
            BibliographicEntity bibliographicEntity = iterator.next();
            Future submit = executorService.submit(new BibItemRecordSetupCallable(bibliographicEntity, solrTemplate, bibliographicDetailsRepository, holdingsDetailsRepository, producerTemplate,nonHoldingInstitutionList, ocolcInstitutionList));
            futures.add(submit);
        }

        logger.info("Num futures to prepare Bib and Associated data : {} ", futures.size());

        List<SolrInputDocument> solrInputDocumentsToIndex = new ArrayList<>();
        for (Iterator<Future> futureIterator = futures.iterator(); futureIterator.hasNext(); ) {
            try {
                Future future = futureIterator.next();
                SolrInputDocument solrInputDocument = (SolrInputDocument) future.get();
                if (solrInputDocument != null)
                    solrInputDocumentsToIndex.add(solrInputDocument);
            } catch (Exception e) {
                logger.error(ScsbCommonConstants.LOG_ERROR, e);
            }
        }

        executorService.shutdown();
        return solrInputDocumentsToIndex;
    }

    public List<Integer> getBibIdListFromString(MatchingAlgorithmReportDataEntity reportDataEntity) {
        String bibId = reportDataEntity.getHeaderValue();
        String[] bibIds = bibId.split(",");
        List<Integer> bibIdList = new ArrayList<>();
        for(int i=0; i< bibIds.length; i++) {
            bibIdList.add(Integer.valueOf(bibIds[i]));
        }
        return bibIdList;
    }
}
