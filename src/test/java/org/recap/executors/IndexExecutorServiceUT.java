package org.recap.executors;



import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.recap.BaseTestCaseUT;
import org.recap.repository.solr.temp.BibCrudRepositoryMultiCoreSupport;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

@Ignore
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