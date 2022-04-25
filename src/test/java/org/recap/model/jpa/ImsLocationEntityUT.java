package org.recap.model.jpa;


import org.junit.Test;
import org.recap.BaseTestCaseUT;
import java.sql.Timestamp;


import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ImsLocationEntityUT extends BaseTestCaseUT
{
 @Test
    public void ImsLocationEntity()
 {
     Integer imsLocationId = 1;
     String imsLocationName = "test";
     String description = "test";
     boolean active = false;
     String createdBy = "test";
     String updatedBy = "test";
     Timestamp timestamp = new Timestamp(System.currentTimeMillis());
     String CreatedDate = "timestamp";
     String UpdateDate = "timestamp";


     ImsLocationEntity imsLocationEntity = new ImsLocationEntity();
     imsLocationEntity.setImsLocationId(imsLocationId);
     imsLocationEntity.setImsLocationName(imsLocationName);
     imsLocationEntity.setDescription(description);
     imsLocationEntity.setActive(active);
     imsLocationEntity.setCreatedBy(createdBy);
     imsLocationEntity.setUpdatedBy(updatedBy);
     imsLocationEntity.setCreatedDate(timestamp);
     imsLocationEntity.setUpdatedDate(timestamp);

     assertNotNull(imsLocationEntity.getImsLocationId());
     assertNotNull(imsLocationEntity.getImsLocationName());
     assertNotNull(imsLocationEntity.getDescription());
     assertNotNull(imsLocationEntity.getCreatedBy());
     assertNotNull(imsLocationEntity.getUpdatedBy());
     assertNotNull(imsLocationEntity.getCreatedDate());
     assertNotNull(imsLocationEntity.getUpdatedDate());
     assertNotNull(imsLocationEntity.isActive());



 }

}
