package org.recap.model.search;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by rajeshbabuk on 11/7/16.
 */
@Data
public class SearchResultRow implements Serializable {
    private Integer bibId;
    private String title;
    private String author;
    private String publisher;
    private String publisherDate;
    private String owningInstitution;
    private String customerCode;
    private String collectionGroupDesignation;
    private String useRestriction;
    private String barcode;
    private String summaryHoldings;
    private String availability;
    private String leaderMaterialType;
    private boolean selected = false;
    private boolean showItems = false;
    private boolean selectAllItems = false;
    private List<SearchItemResultRow> searchItemResultRows = new ArrayList<>();
    private Integer itemId;
    private String owningInstitutionBibId;
    private String owningInstitutionHoldingsId;
    private String owningInstitutionItemId;
    private String imsLocation;

    private Integer patronBarcode;
    private String requestingInstitution;
    private String deliveryLocation;
    private String requestType;
    private String requestNotes;
    private Date bibCreatedDate;
    private String authorSearch;
    private String matchingIdentifier;
}