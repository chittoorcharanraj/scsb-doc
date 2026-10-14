package org.recap.admin;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.recap.BaseTestCaseUT;
import org.recap.PropertyKeyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;


/**
 * Created by pvsubrah on 6/12/16.
 */

public class SolrAdminAT extends BaseTestCaseUT {

    @InjectMocks
    SolrAdmin solrAdmin;

    @Mock
    CoreAdminRequest.Create coreAdminCreateRequest;

    @Value("${" + PropertyKeyConstants.SOLR_SOLR_HOME + "}")
    String solrHome;

    @Value("${" + PropertyKeyConstants.SOLR_PARENT_CORE + "}")
    private String solrParentCore;

    String tempCoreName1 = "temp0";
    String tempCoreName2 = "temp1";
    String tempCoreName3 = "temp2";

    @BeforeEach
    public void setUp() throws Exception {        ReflectionTestUtils.setField(solrAdmin,"solrHome",solrHome);
        ReflectionTestUtils.setField(solrAdmin,"solrParentCore","recap");

    }

    @Test
    public void createSolrCoresTest() throws Exception {
        SolrAdmin spySolrAdmin = Mockito.spy(solrAdmin);
        SolrClient solrAdminClient = Mockito.mock(SolrClient.class);
        CoreAdminRequest.Create coreAdminRequestCreate = Mockito.mock(CoreAdminRequest.Create.class);
        CoreAdminResponse successResponse = Mockito.mock(CoreAdminResponse.class);
        CoreAdminResponse failureResponse = Mockito.mock(CoreAdminResponse.class);

        ReflectionTestUtils.setField(spySolrAdmin, "solrAdminClient", solrAdminClient);
        ReflectionTestUtils.setField(spySolrAdmin, "solrClient", Mockito.mock(SolrClient.class));
        Mockito.doReturn(coreAdminRequestCreate).when(spySolrAdmin).getCoreAdminCreateRequest();
        Mockito.doReturn(false).when(spySolrAdmin).isCoreExist("temp0");
        Mockito.doReturn(true).when(spySolrAdmin).isCoreExist("temp1");
        Mockito.doReturn(false).when(spySolrAdmin).isCoreExist("temp2");
        Mockito.doReturn(false).when(spySolrAdmin).isCoreExist("temp3");
        Mockito.when(successResponse.getStatus()).thenReturn(0);
        Mockito.when(failureResponse.getStatus()).thenReturn(1);
        Mockito.when(coreAdminRequestCreate.process(solrAdminClient))
                .thenReturn(successResponse)
                .thenReturn(failureResponse)
                .thenThrow(new IOException("create failed"));

        List<String> tempCores = asList("temp0", "temp1", "temp2", "temp3");
        CoreAdminResponse coreAdminResponse = spySolrAdmin.createSolrCores(tempCores);

        assertSame(failureResponse, coreAdminResponse);
        Mockito.verify(coreAdminRequestCreate).setCoreName("temp0");
        Mockito.verify(coreAdminRequestCreate).setCoreName("temp2");
        Mockito.verify(coreAdminRequestCreate).setCoreName("temp3");
        Mockito.verify(coreAdminRequestCreate, Mockito.times(3)).process(solrAdminClient);
    }

   @Test
    public void mergeCores() throws Exception {
        CoreAdminRequest coreAdminRequest= Mockito.mock(CoreAdminRequest.class);
        SolrClient solrAdminClient= Mockito.mock(SolrClient.class);
        SolrClient solrClient= Mockito.mock(SolrClient.class);
        ReflectionTestUtils.setField(solrAdmin,"solrAdminClient",solrAdminClient);
        ReflectionTestUtils.setField(solrAdmin,"coreAdminRequest",coreAdminRequest);
        ReflectionTestUtils.setField(solrAdmin,"solrClient",solrClient);
        List<String> tempCores = asList(tempCoreName1, tempCoreName2, tempCoreName3);
        solrAdmin.mergeCores(tempCores);
        assertNotNull(tempCores);

    }

    @Test
    public void unLoadCores() throws Exception {
        List<String> tempCores = asList(tempCoreName1, tempCoreName2, tempCoreName3);
        CoreAdminRequest coreAdminRequest= Mockito.mock(CoreAdminRequest.class);
        SolrClient solrAdminClient= Mockito.mock(SolrClient.class);
        SolrClient solrClient= Mockito.mock(SolrClient.class);
        ReflectionTestUtils.setField(solrAdmin,"solrAdminClient",solrAdminClient);
        ReflectionTestUtils.setField(solrAdmin,"coreAdminRequest",coreAdminRequest);
        ReflectionTestUtils.setField(solrAdmin,"solrClient",solrClient);
        solrAdmin.unLoadCores(tempCores);
        assertNotNull(tempCores);
    }

}
