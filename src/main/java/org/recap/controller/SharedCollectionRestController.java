package org.recap.controller;

import lombok.extern.slf4j.Slf4j;
import org.recap.ScsbConstants;
import org.recap.model.BibAvailabilityResponse;
import org.recap.model.BibItemAvailabityStatusRequest;
import org.recap.model.ItemAvailabilityResponse;
import org.recap.model.ItemAvailabityStatusRequest;
import org.recap.service.ItemAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chenchulakshmig on 6/10/16.
 */
@Slf4j
@RestController
@RequestMapping("/sharedCollection")
public class SharedCollectionRestController {



    @Autowired
    private ItemAvailabilityService itemAvailabilityService;

    /**
     * Gets ItemAvailabilityService object..
     *
     * @return the ItemAvailabilityService object.
     */
    public ItemAvailabilityService getItemAvailabilityService() {
        return itemAvailabilityService;
    }

    /**
     * This method is used to get the item availability status.
     *
     * @param itemAvailabityStatusRequest the item availabity status request
     * @return the response entity
     */
    @PostMapping(value = "/itemAvailabilityStatus", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity itemAvailabilityStatus(@RequestBody ItemAvailabityStatusRequest itemAvailabityStatusRequest) {
        List<ItemAvailabilityResponse> itemAvailabilityResponses = new ArrayList<>();
        ResponseEntity responseEntity;
        try {
            itemAvailabilityResponses = getItemAvailabilityService().getItemStatusByBarcodeAndIsDeletedFalseList(itemAvailabityStatusRequest.getBarcodes());
        } catch (Exception exception) {
            responseEntity = new ResponseEntity(ScsbConstants.SCSB_PERSISTENCE_SERVICE_IS_UNAVAILABLE, getHttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE);
            log.error(ScsbConstants.EXCEPTION, exception);
            return responseEntity;
        }
        responseEntity = new ResponseEntity(itemAvailabilityResponses, getHttpHeaders(), HttpStatus.OK);
        return responseEntity;
    }

    /**
     * This method is used to get the bib availability status.
     *
     * @param bibItemAvailabityStatusRequest the bib item availability status request
     * @return the response entity
     */
    @PostMapping(value = "/bibAvailabilityStatus", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity bibAvailabilityStatus(@RequestBody BibItemAvailabityStatusRequest bibItemAvailabityStatusRequest) {
        List<BibAvailabilityResponse> bibAvailabilityResponses;
        ResponseEntity responseEntity;
        bibAvailabilityResponses = getItemAvailabilityService().getBibItemAvailabilityStatus(bibItemAvailabityStatusRequest);
        if (bibAvailabilityResponses.isEmpty()) {
            BibAvailabilityResponse bibAvailabilityResponse = new BibAvailabilityResponse();
            bibAvailabilityResponse.setErrorMessage(ScsbConstants.BIB_ITEM_DOESNOT_EXIST);
            bibAvailabilityResponses.add(bibAvailabilityResponse);
        }
        responseEntity = new ResponseEntity(bibAvailabilityResponses, getHttpHeaders(), HttpStatus.OK);
        return responseEntity;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ScsbConstants.DATE, new Date().toString());
        return responseHeaders;
    }
}
