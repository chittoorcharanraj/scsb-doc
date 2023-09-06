package org.recap.service;

import lombok.extern.slf4j.Slf4j;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.BibAvailabilityResponse;
import org.recap.model.BibItemAvailabityStatusRequest;
import org.recap.model.ItemAvailabilityResponse;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.CollectionGroupEntity;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.jpa.ItemStatusEntity;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.repository.jpa.ItemDetailsRepository;
import org.recap.repository.jpa.ItemStatusDetailsRepository;
import org.recap.util.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by premkb on 10/11/16.
 */
@Slf4j
@Service
public class ItemAvailabilityService {


    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    private PropertyUtil propertyUtil;

    /**
     * This method gets item status by item's barcode and isDeleted field which is false.
     *
     * @param barcode the barcode
     * @return the item status by barcode and is deleted false
     */
    public String getItemStatusByBarcodeAndIsDeletedFalse(String barcode) {
        return itemDetailsRepository.getItemStatusByBarcodeAndIsDeletedFalse(barcode,Boolean.FALSE);
    }

    /**
     * This method gets item status by item's barcode and isDeleted field which is false.
     *
     * @param barcodeList the barcode list
     * @return the item status by barcode and is deleted false list
     */
    public List<ItemAvailabilityResponse> getItemStatusByBarcodeAndIsDeletedFalseList(List<String> barcodeList) {
        List<String> barcodes = new ArrayList<>();
        for (String barcode : barcodeList) {
            barcodes.add(barcode.trim());
        }
        Map<String, String> barcodeStatusMap = new HashMap<>();
        List<ItemAvailabilityResponse> itemAvailabilityResponses = new ArrayList<>();
        List<ItemEntity> itemEntityList = itemDetailsRepository.getItemStatusByBarcodeAndIsDeletedFalseList(barcodes,Boolean.FALSE);
        Map<String, String> propertyMap = propertyUtil.getPropertyByKeyForAllInstitutions(PropertyKeyConstants.ILS.ILS_ENABLE_CIRCULATION_FREEZE);
        ItemStatusEntity itemNotAvailableStatusEntity = itemStatusDetailsRepository.findByStatusCode(ScsbCommonConstants.NOT_AVAILABLE);
        for (ItemEntity itemEntity : itemEntityList) {
            String institutionCode = itemEntity.getInstitutionEntity().getInstitutionCode().toUpperCase();
            boolean isCirculationFreezeEnabled = Boolean.parseBoolean(propertyMap.get(institutionCode));
            barcodeStatusMap.put(itemEntity.getBarcode(), isCirculationFreezeEnabled ? itemNotAvailableStatusEntity.getStatusDescription() : itemEntity.getItemStatusEntity().getStatusDescription());
        }
        for (String requestedBarcode : barcodes) {
            ItemAvailabilityResponse itemAvailabilityResponse = new ItemAvailabilityResponse();
            if (barcodeStatusMap.containsKey(requestedBarcode)) {
                itemAvailabilityResponse.setItemBarcode(requestedBarcode);
                itemAvailabilityResponse.setItemAvailabilityStatus(barcodeStatusMap.get(requestedBarcode));
            } else {
                itemAvailabilityResponse.setItemBarcode(requestedBarcode);
                itemAvailabilityResponse.setItemAvailabilityStatus(ScsbConstants.ITEM_BARCDE_DOESNOT_EXIST);
            }
            itemAvailabilityResponses.add(itemAvailabilityResponse);
        }
        return itemAvailabilityResponses;
    }

    /**
     * This method gets bibItem availability status based on the bib item availability status request.
     *
     * @param bibItemAvailabilityStatusRequest the bib item availability status request
     * @return the item availability status
     */
    public List<BibAvailabilityResponse> getBibItemAvailabilityStatus(BibItemAvailabityStatusRequest bibItemAvailabilityStatusRequest) {
        List<BibAvailabilityResponse> bibAvailabilityResponses = new ArrayList<>();
        BibliographicEntity bibliographicEntity;
        try {
            Map<String, String> propertyMap = propertyUtil.getPropertyByKeyForAllInstitutions(PropertyKeyConstants.ILS.ILS_ENABLE_CIRCULATION_FREEZE);
            ItemStatusEntity itemNotAvailableStatusEntity = itemStatusDetailsRepository.findByStatusCode(ScsbCommonConstants.NOT_AVAILABLE);
            if (bibItemAvailabilityStatusRequest.getInstitutionId().equalsIgnoreCase(ScsbConstants.SCSB)) {
                bibliographicEntity = bibliographicDetailsRepository.findById(Integer.parseInt(bibItemAvailabilityStatusRequest.getBibliographicId())).orElse(null);
            } else {
                InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode(bibItemAvailabilityStatusRequest.getInstitutionId());
                bibliographicEntity = bibliographicDetailsRepository.findByOwningInstitutionIdAndOwningInstitutionBibId(institutionEntity.getId(), bibItemAvailabilityStatusRequest.getBibliographicId());
            }
            if (bibliographicEntity != null) {
                String institutionCode = bibliographicEntity.getInstitutionEntity().getInstitutionCode().toUpperCase();
                boolean isCirculationFreezeEnabled = Boolean.parseBoolean(propertyMap.get(institutionCode));
                for (ItemEntity itemEntity : bibliographicEntity.getItemEntities()) {
                    if (!itemEntity.isDeleted()) {
                        BibAvailabilityResponse bibAvailabilityResponse = new BibAvailabilityResponse();
                        bibAvailabilityResponse.setItemBarcode(itemEntity.getBarcode());
                        bibAvailabilityResponse.setItemAvailabilityStatus(isCirculationFreezeEnabled ? itemNotAvailableStatusEntity.getStatusDescription() : itemEntity.getItemStatusEntity().getStatusDescription());
                        CollectionGroupEntity collectionGroupEntity = itemEntity.getCollectionGroupEntity();
                        if (collectionGroupEntity != null && !StringUtils.isEmpty(collectionGroupEntity.getCollectionGroupDescription())) {
                            bibAvailabilityResponse.setCollectionGroupDesignation(collectionGroupEntity.getCollectionGroupDescription());
                        }
                        bibAvailabilityResponses.add(bibAvailabilityResponse);
                    }
                }
            } else {
                BibAvailabilityResponse bibAvailabilityResponse = new BibAvailabilityResponse();
                bibAvailabilityResponse.setItemBarcode("");
                bibAvailabilityResponse.setErrorMessage(ScsbConstants.BIB_ITEM_DOESNOT_EXIST);
                bibAvailabilityResponses.add(bibAvailabilityResponse);
            }
        } catch (Exception e) {
            log.error(ScsbConstants.EXCEPTION, e);
        }
        return bibAvailabilityResponses;
    }
}
