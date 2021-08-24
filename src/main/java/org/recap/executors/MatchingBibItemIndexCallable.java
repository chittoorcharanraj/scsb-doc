package org.recap.executors;

import org.apache.camel.ProducerTemplate;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.repository.jpa.HoldingsDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by angelind on 30/1/17.
 */
public class MatchingBibItemIndexCallable extends CommonCallable implements Callable {

    private static final Logger logger = LoggerFactory.getLogger(MatchingBibItemIndexCallable.class);

    private final int pageNum;
    private final int docsPerPage;
    private String coreName;
    private BibliographicDetailsRepository bibliographicDetailsRepository;
    private HoldingsDetailsRepository holdingsDetailsRepository;
    private ProducerTemplate producerTemplate;
    private SolrTemplate solrTemplate;
    private String operationType;
    private Date from;
    private Date to;
    private List<String> nonHoldingInstitutionList;
    private String solrURL;

    /**
     * This method instantiates a new matching bib item index callable.
     *
     * @param coreName                       the core name
     * @param pageNum                        the page num
     * @param docsPerPage                    the docs per page
     * @param bibliographicDetailsRepository the bibliographic details repository
     * @param holdingsDetailsRepository      the holdings details repository
     * @param producerTemplate               the producer template
     * @param solrTemplate                   the solr template
     * @param operationType                  the operation type
     * @param from                           the from date
     * @param to                             the to date
     */
    public MatchingBibItemIndexCallable(String coreName, int pageNum, int docsPerPage, BibliographicDetailsRepository bibliographicDetailsRepository,
                                        HoldingsDetailsRepository holdingsDetailsRepository, ProducerTemplate producerTemplate, SolrTemplate solrTemplate, String operationType,
                                        Date from, Date to,List<String> nonHoldingInstitutionList, String solrURL) {
        this.coreName = coreName;
        this.pageNum = pageNum;
        this.docsPerPage = docsPerPage;
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
        this.holdingsDetailsRepository = holdingsDetailsRepository;
        this.producerTemplate = producerTemplate;
        this.solrTemplate = solrTemplate;
        this.operationType = operationType;
        this.from = from;
        this.to = to;
        this.nonHoldingInstitutionList = nonHoldingInstitutionList;
        this.solrURL = solrURL;
    }

    /**
     * This method is processed by thread to generate solr input documents and index to solr.
     * @return
     * @throws Exception
     */
    @Override
    public Object call() throws Exception {
        Page<BibliographicEntity> bibliographicEntities;
        bibliographicEntities = bibliographicDetailsRepository.getBibliographicEntitiesForChangedItems(PageRequest.of(pageNum, docsPerPage), operationType, from, to);
        List<SolrInputDocument> solrInputDocumentsToIndex = setSolrInputDocuments(bibliographicEntities, solrTemplate, bibliographicDetailsRepository, holdingsDetailsRepository, producerTemplate, logger,nonHoldingInstitutionList);
        if (!CollectionUtils.isEmpty(solrInputDocumentsToIndex)) {
           // producerTemplate.sendBodyAndHeader(ScsbCommonConstants.SOLR_QUEUE, solrInputDocumentsToIndex, ScsbCommonConstants.SOLR_CORE, coreName);
            SolrTemplate templateForSolr = new SolrTemplate(new HttpSolrClient.Builder(solrURL + File.separator).build());
            templateForSolr.saveDocuments(coreName, solrInputDocumentsToIndex);
            templateForSolr.commit(coreName);
        }
        return solrInputDocumentsToIndex.size();
    }
}
