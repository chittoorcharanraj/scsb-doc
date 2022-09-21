package org.recap.controller;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by akulak on 16/5/17.
 */
@RestController
@RequestMapping("/accessionReconcilationService")
public class AccessionReconcilationRestController {

    @Resource(name = "recapSolrTemplate")
    private SolrTemplate solrTemplate;

    /**
     *This method is used to start accession reconcilation to find the missing barcodes in scsb.
     * @param barcodesAndCustomerCodes
     * @return
     * @throws IOException
     * @throws SolrServerException
     */
    @PostMapping(value = "/startAccessionReconcilation")
    public Map<String,String> startAccessionReconcilation(@RequestBody Map<String,String> barcodesAndCustomerCodes) throws IOException, SolrServerException {
        SolrClient solrClient = solrTemplate.getSolrClient();
        Set<String> barcodes = barcodesAndCustomerCodes.keySet();
        String splittedBarcodes = barcodes.stream().map(String::trim).collect(Collectors.joining(","));
        SolrQuery solrQuery = getSolrQuery(splittedBarcodes, barcodes.size());
        QueryResponse queryResponse = solrClient.query(solrQuery, SolrRequest.METHOD.POST);
        return getDifference(queryResponse,barcodesAndCustomerCodes);
    }

    /**
     * This method is used to find the missing barcodes in scsb.
     * @param queryResponse
     * @param barcodesAndCustomerCodes
     * @return
     */
    private static Map<String, String> getDifference(QueryResponse queryResponse, Map<String, String> barcodesAndCustomerCodes) {
        for (Object barcode : queryResponse.getFieldStatsInfo().get(ScsbCommonConstants.BARCODE).getDistinctValues()) {
            barcodesAndCustomerCodes.entrySet().removeIf(p -> p.getKey().contains(barcode.toString()));
        }
        return barcodesAndCustomerCodes;
    }

    /**
     * This method is used to build the solr query for the given barcode.
     * @param barcode
     * @param rows
     * @return
     */
    private static SolrQuery getSolrQuery(String barcode,int rows) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(ScsbConstants.DOC_TYPE_ITEM);
        solrQuery.setRows(rows);
        solrQuery.addFilterQuery(ScsbCommonConstants.BARCODE+":"+ StringEscapeUtils.escapeJava(barcode).replace(",","\" \""));
        solrQuery.setFields(ScsbCommonConstants.BARCODE);
        solrQuery.setGetFieldStatistics(true);
        solrQuery.setGetFieldStatistics(ScsbCommonConstants.BARCODE);
        solrQuery.addStatsFieldCalcDistinct(ScsbCommonConstants.BARCODE, true);
        return solrQuery;
    }

}

