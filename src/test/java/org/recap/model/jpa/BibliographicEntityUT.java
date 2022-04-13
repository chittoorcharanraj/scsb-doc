package org.recap.model.jpa;


import org.junit.Test;
import org.recap.BaseTestCaseUT;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BibliographicEntityUT extends BaseTestCaseUT {

    @Test
    public void bibliographicEntity()throws Exception{
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("sourceBibContent".getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setCreatedBy("tst");
        bibliographicEntity.setLastUpdatedBy("tst");
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId("1421");
        bibliographicEntity.setId(1);
        bibliographicEntity.setDeleted(true);
        bibliographicEntity.hashCode();
        bibliographicEntity.equals(new BibliographicEntity());
        assertNotNull(bibliographicEntity);
    }
}
