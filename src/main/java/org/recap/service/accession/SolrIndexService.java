package org.recap.service.accession;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.collections4.ListUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.recap.PropertyKeyConstants;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.repository.jpa.HoldingsDetailsRepository;
import org.recap.util.BibJSONUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajeshbabuk on 10/11/16.
 */
@Slf4j
@Service
public class SolrIndexService {



    @Value("${" + PropertyKeyConstants.SOLR_PARENT_CORE + "}")
    private String solrCore;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Resource
    private SolrTemplate solrTemplate;

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private HoldingsDetailsRepository holdingsDetailsRepository;

    @Autowired
    private SolrClient solrClient;

    @Value("${" + PropertyKeyConstants.SUBMIT_COLLECTION_OWNINGINSTBIBIDLIST_PARTITION_SIZE + "}")
    private Integer submitCollectionOwnInstBibIdListPartitionSize;

    @Value("${" + PropertyKeyConstants.NONHOLDINGID_INSTITUTION + "}")
    private List<String> nonHoldingInstitutionList;

    @Value("${" + PropertyKeyConstants.OCOLC_INSTITUTION + "}")
    private List<String> ocolcInstitutionList;


    /**
     * Gets logger.
     *
     * @return the logger
     */
    public Logger getLogger() {
        return log;
    }

    /**
     * Gets ProducerTemplate object.
     *
     * @return the ProducerTemplate object.
     */
    public ProducerTemplate getProducerTemplate() {
        return producerTemplate;
    }

    /**
     * Gets SolrTemplate object.
     *
     * @return the SolrTemplate object.
     */
    public SolrTemplate getSolrTemplate() {
        return solrTemplate;
    }

    /**
     * Gets BibliographicDetailsRepository object.
     *
     * @return the BibliographicDetailsRepository object.
     */
    public BibliographicDetailsRepository getBibliographicDetailsRepository() {
        return bibliographicDetailsRepository;
    }

    /**
     * Gets HoldingsDetailsRepository object.
     *
     * @return the HoldingsDetailsRepository object.
     */
    public HoldingsDetailsRepository getHoldingsDetailsRepository() {
        return holdingsDetailsRepository;
    }

    /**
     * Gets BibJSONUtil object.
     *
     * @return the BibJSONUtil object.
     */
    public BibJSONUtil getBibJSONUtil(){
        return new BibJSONUtil();
    }

    /**
     * This method is used to index by bibliographic id in solr.
     *
     * @param bibliographicId the bibliographic id
     * @return the solr input document
     */
    public SolrInputDocument indexByBibliographicId(@RequestBody Integer bibliographicId) {
        getBibJSONUtil().setProducerTemplate(getProducerTemplate());
        BibliographicEntity bibliographicEntity = getBibliographicDetailsRepository().findById(bibliographicId).orElse(null);
        return indexBibliographicEntity(bibliographicEntity);
    }

    public SolrInputDocument indexBibliographicEntity(BibliographicEntity bibliographicEntity) {
        BibJSONUtil bibJSONUtil = getBibJSONUtil();
        bibJSONUtil.setProducerTemplate(producerTemplate);
        bibJSONUtil.setNonHoldingInstitutions(nonHoldingInstitutionList);
        bibJSONUtil.setOcolcInstitutions(ocolcInstitutionList);
        SolrInputDocument solrInputDocument = bibJSONUtil.generateBibAndItemsForIndex(bibliographicEntity, getSolrTemplate(), getBibliographicDetailsRepository(), getHoldingsDetailsRepository());
        if (solrInputDocument !=null) {
            getSolrTemplate().saveDocument(solrCore, solrInputDocument);
            getSolrTemplate().commit(solrCore);
        }
        return solrInputDocument;
    }

    public void indexByOwnInstBibId(List<String> owningInstBibIdList,Integer owningInstId){
        List<List<String>> owningInstBibIdPartitionedList = ListUtils.partition(owningInstBibIdList,submitCollectionOwnInstBibIdListPartitionSize);
        for (List<String> owningInstBibIdPartitionedListForIndexing:owningInstBibIdPartitionedList) {
            List<Integer> bibliographicIdList = getBibliographicIdForIndexing(owningInstBibIdPartitionedListForIndexing,owningInstId);
            for(Integer bibliographicId:bibliographicIdList){
                indexByBibliographicId(bibliographicId);
            }
        }
    }

    public List<Integer> getBibliographicIdForIndexing(List<String> owningInstBibIdList,Integer owningInstId){
        List<Integer> bibliographicIdList = new ArrayList<>();
        List<BibliographicEntity> bibliographicEntityList = bibliographicDetailsRepository.findByOwningInstitutionBibIdInAndOwningInstitutionId(owningInstBibIdList,owningInstId);
        if (bibliographicEntityList != null) {
            for(BibliographicEntity bibliographicEntity:bibliographicEntityList){
                if(owningInstBibIdList.contains(bibliographicEntity.getOwningInstitutionBibId()) && bibliographicEntity.getId() != null){
                    bibliographicIdList.add(bibliographicEntity.getId());
                    log.info("Updated Dummy record scsb bibid to index--->{}",bibliographicEntity.getId());
                }
            }
        }
        return bibliographicIdList;
    }

    /**
     * This method is used to delete by doc id in solr.
     *
     * @param docIdParam the doc id param
     * @param docIdValue the doc id value
     * @throws IOException         the io exception
     * @throws SolrServerException the solr server exception
     */
    public void deleteByDocId(String docIdParam, String docIdValue) throws IOException, SolrServerException {
        getSolrTemplate().getSolrClient().deleteByQuery(solrCore,docIdParam+":"+docIdValue,1);
    }

    /**
     * This method is used to delete by query in solr.
     *
     * @param query
     * @throws IOException
     * @throws SolrServerException
     */
    public void deleteBySolrQuery(String query) throws IOException, SolrServerException {
        getSolrTemplate().getSolrClient().deleteByQuery(solrCore, query,1);
    }
}
