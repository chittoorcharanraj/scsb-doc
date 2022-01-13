package org.recap.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.ScsbConstants;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.transfer.HoldingTransferResponse;
import org.recap.model.transfer.ItemTransferResponse;
import org.recap.model.transfer.TransferRequest;
import org.recap.model.transfer.TransferResponse;
import org.recap.service.transfer.TransferService;
import org.recap.util.HelperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.List;

/**
 * Created by sheiks on 12/07/17.
 */
@RestController
@RequestMapping("/transfer")
public class TransferController {



    @Autowired
    private TransferService transferService;

    @Autowired
    private HelperUtil helperUtil;

    public TransferService getTransferService() {
        return transferService;
    }

    public HelperUtil getHelperUtil() {
        return helperUtil;
    }

    /**
     * This method is transfer the item or holdings from one bib to another bib.
     *
     * @return response
     * @throws Exception
     */
    @PostMapping(value = "/processTransfer")
    public TransferResponse processTransfer(@RequestBody TransferRequest transferRequest) {
        // todo: validate transfer request
        TransferResponse transferResponse = new TransferResponse();
        String institution = transferRequest.getInstitution();

        int successCount = 0;
        int failureCount = 0;

        InstitutionEntity institutionEntity = null;
        if(StringUtils.isBlank(institution)) {
            return getTransferResponse(transferRequest, transferResponse, ScsbConstants.Transfer.INSTITUTION_EMPTY);
        } else {
            institutionEntity = getTransferService().getInstitutionDetailsRepository().findByInstitutionCode(institution);
            if(null == institutionEntity) {
                return getTransferResponse(transferRequest, transferResponse, ScsbConstants.Transfer.UNKNOWN_INSTITUTION);
            }
        }

        List<HoldingTransferResponse> holdingTransferResponses = getTransferService().processHoldingTransfer(transferRequest, institutionEntity);
        transferResponse.setHoldingTransferResponses(holdingTransferResponses);

        if(CollectionUtils.isNotEmpty(holdingTransferResponses)) {
            for (Iterator<HoldingTransferResponse> iterator = holdingTransferResponses.iterator(); iterator.hasNext(); ) {
                HoldingTransferResponse holdingTransferResponse = iterator.next();
                if(holdingTransferResponse.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }
        }

        List<ItemTransferResponse> itemTransferResponses = getTransferService().processItemTransfer(transferRequest, institutionEntity);
        transferResponse.setItemTransferResponses(itemTransferResponses);

        if(CollectionUtils.isNotEmpty(itemTransferResponses)) {
            for (Iterator<ItemTransferResponse> iterator = itemTransferResponses.iterator(); iterator.hasNext(); ) {
                ItemTransferResponse itemTransferResponse = iterator.next();
                if(itemTransferResponse.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }
        }


        if(successCount > 0 && failureCount > 0) {
            transferResponse.setMessage(ScsbConstants.Transfer.PARTIALLY_SUCCESS);
            return transferResponse;
        } else if(successCount > 0) {
            transferResponse.setMessage(ScsbConstants.Transfer.COMPLETED);
            return transferResponse;
        } else if(failureCount > 0) {
            transferResponse.setMessage(ScsbConstants.Transfer.FAILED);
            return transferResponse;
        }

        return transferResponse;

    }

    private TransferResponse getTransferResponse(@RequestBody TransferRequest transferRequest, TransferResponse transferResponse, String instituionEmpty) {
        String institution;
        transferResponse.setMessage(instituionEmpty);
        institution = ScsbConstants.UNKNOWN_INSTITUTION;
        String requestString = getHelperUtil().getJsonString(transferRequest);
        String responseString = transferResponse.getMessage();
        getTransferService().saveReportForTransfer(requestString, responseString, institution, ScsbConstants.Transfer.ROOT);
        return transferResponse;
    }
}
