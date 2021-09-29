package org.recap.executors;

import org.apache.solr.client.solrj.SolrClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.repository.solr.temp.BibCrudRepositoryMultiCoreSupport;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;


public class IndexExecutorServiceUT extends BaseTestCaseUT

{
    @Mock
    IndexExecutorService indexExecutorService;

    @Mock
    BibCrudRepositoryMultiCoreSupport bibCrudRepositoryMultiCoreSupport;


    @Test
    public void deleteTempIndexes() throws Exception {
        List<String> coreNames = new ArrayList<>();
        coreNames.add(0, "test");
        String solrUrl = "test";
        try{
            ReflectionTestUtils.invokeMethod(indexExecutorService, "deleteTempIndexes", coreNames, solrUrl);
        }catch(Exception e){}
    }

}