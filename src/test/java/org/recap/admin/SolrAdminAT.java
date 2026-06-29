
package org.recap.admin;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.recap.BaseTestCaseUT;
import org.recap.PropertyKeyConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


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
        List<String> tempCores = new ArrayList<>();
        CoreAdminResponse cores=new CoreAdminResponse();
        cores.setResponse(new NamedList<>());
        SolrClient solrAdminClient= Mockito.mock(SolrClient.class);
        CoreAdminRequest coreAdminRequest= Mockito.mock(CoreAdminRequest.class);
        SolrClient solrClient= Mockito.mock(SolrClient.class);
        ReflectionTestUtils.setField(solrAdmin,"solrAdminClient",solrAdminClient);
        ReflectionTestUtils.setField(solrAdmin,"solrClient",solrClient);
        ReflectionTestUtils.setField(solrAdmin,"coreAdminRequest",coreAdminRequest);
        CoreAdminResponse coreAdminResponse = solrAdmin.createSolrCores(tempCores);
        assertNull(coreAdminResponse);

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
