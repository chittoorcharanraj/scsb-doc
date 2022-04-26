package org.recap.model.jpa;

import org.junit.Test;
import org.recap.BaseTestCaseUT;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ItemBarcodeHistoryEntityUT extends BaseTestCaseUT
{
@Test
    public void ItemBarcodeHistoryEntity()
    {
        String owningingInstitution = "PUL";
        String owningingInstitutionItemId = "1";
        String oldBarcode = "123456";
        String newBarcode = "345667";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Date createdDate;

        ItemBarcodeHistoryEntity itemBarcodeHistoryEntity = new ItemBarcodeHistoryEntity();
        itemBarcodeHistoryEntity.setNewBarcode(newBarcode);
        itemBarcodeHistoryEntity.setOldBarcode(oldBarcode);
        itemBarcodeHistoryEntity.setOwningingInstitutionItemId(owningingInstitutionItemId);
        itemBarcodeHistoryEntity.setOwningingInstitution(owningingInstitution);
        itemBarcodeHistoryEntity.setCreatedDate(timestamp);

        assertNotNull(itemBarcodeHistoryEntity.getNewBarcode());
        assertNotNull(itemBarcodeHistoryEntity.getOldBarcode());
        assertNotNull(itemBarcodeHistoryEntity.getOwningingInstitution());
        assertNotNull(itemBarcodeHistoryEntity.getOwningingInstitutionItemId());
        assertNotNull(itemBarcodeHistoryEntity.getCreatedDate());

    }
}
