package org.recap.model;

import lombok.Data;


/**
 * Created by rajeshbabuk on 25/6/20.
 */
@Data
public class DocTypeAvailabilityResponse {
    private String itemBarcode;
    private String itemAvailabilityStatus;
    private String errorMessage;
}
