package org.recap.model;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Created by hemalathas on 28/9/17.
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class BibAvailabilityResponse extends DocTypeAvailabilityResponse {
    private String collectionGroupDesignation;
}
