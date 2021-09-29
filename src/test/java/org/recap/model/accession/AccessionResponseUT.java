package org.recap.model.accession;

import org.junit.jupiter.api.Test;
import org.recap.BaseTestCaseUT;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class AccessionResponseUT extends BaseTestCaseUT
{
@Test
    public  void AccessionResponse()
{
    String itemBarcode = "123456";
    String message = "test";
    AccessionResponse accessionResponse = new AccessionResponse();
    accessionResponse.setItemBarcode(itemBarcode);
    accessionResponse.setMessage(message);
    assertNotNull(accessionResponse.getItemBarcode());
    assertNotNull(accessionResponse.getMessage());
}

}
