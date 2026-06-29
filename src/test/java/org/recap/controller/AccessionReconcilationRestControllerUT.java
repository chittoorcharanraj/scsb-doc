package org.recap.controller;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;


import org.recap.BaseTestCaseUT;
import org.recap.ScsbCommonConstants;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AccessionReconcilationRestControllerUT extends BaseTestCaseUT {

    @InjectMocks
    AccessionReconcilationRestController accessionReconcilationRestController;

    @BeforeEach
    public void setUp()throws Exception {    }

    @Test
    public void startAccessionReconcilation()throws Exception{
        Map<String,String> barcodesAndCustomerCodes=new HashMap<>();
        barcodesAndCustomerCodes.put("barcode","123456");
        SolrTemplate mocksolrTemplate1 = Mockito.mock(SolrTemplate.class);
        SolrClient solrClient=Mockito.mock(SolrClient.class);
        Mockito.when(mocksolrTemplate1.getSolrClient()).thenReturn(solrClient);
        QueryResponse queryResponse= Mockito.mock(QueryResponse.class);
        Mockito.when(solrClient.query(Mockito.any(SolrQuery.class),Mockito.any())).thenReturn(queryResponse);
        Map<String, FieldStatsInfo> getFieldStatsInfo=new HashMap<>();
        FieldStatsInfo fieldStatsInfo=Mockito.mock(FieldStatsInfo.class);
        Collection<Object> barcodes=new ArrayList<>();
        barcodes.add("123456");
        Mockito.when(fieldStatsInfo.getDistinctValues()).thenReturn(barcodes);
        getFieldStatsInfo.put(ScsbCommonConstants.BARCODE,fieldStatsInfo);
        Mockito.when(queryResponse.getFieldStatsInfo()).thenReturn(getFieldStatsInfo);
        ReflectionTestUtils.setField(accessionReconcilationRestController,"solrTemplate",mocksolrTemplate1);
        Map<String,String> responseMessage=accessionReconcilationRestController.startAccessionReconcilation(barcodesAndCustomerCodes);
        assertNotNull(responseMessage);
        assertEquals(barcodesAndCustomerCodes,responseMessage);
    }

}
