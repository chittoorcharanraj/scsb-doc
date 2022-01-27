package org.recap.controller;

import lombok.extern.slf4j.Slf4j;
import org.recap.ScsbCommonConstants;
import org.recap.util.UpdateCgdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by rajeshbabuk on 3/1/17.
 */
@Slf4j
@RestController
@RequestMapping("/updateCgdService")
public class UpdateCgdRestController {


    @Autowired
    private UpdateCgdUtil updateCgdUtil;

    /**
     * This method is used to update cgd for item in both solr and database and sends email notification on successful completion.
     *
     * @param itemBarcode                   the item barcode
     * @param owningInstitution             the owning institution
     * @param oldCollectionGroupDesignation the old collection group designation
     * @param newCollectionGroupDesignation the new collection group designation
     * @param cgdChangeNotes                the cgd change notes
     * @return the string statusMessage
     */
    @GetMapping(value="/updateCgd")
    public String updateCgdForItem(@RequestParam String itemBarcode, @RequestParam String owningInstitution, @RequestParam String oldCollectionGroupDesignation, @RequestParam String newCollectionGroupDesignation, @RequestParam String cgdChangeNotes, @RequestParam String userName) {
        String statusMessage = null;
        try {
            statusMessage = updateCgdUtil.updateCGDForItem(itemBarcode, owningInstitution, oldCollectionGroupDesignation, newCollectionGroupDesignation, cgdChangeNotes, userName);
        } catch (Exception e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return statusMessage;
    }
}
