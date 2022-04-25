package org.recap.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.transfer.Transfer;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;

public class XferMgrProgressUT extends BaseTestCaseUT {

    @InjectMocks
    XferMgrProgress xferMgrProgress;

    @Mock
    Transfer transfer;

    @Test
    public void waitForCompletion() throws Exception {

        try {
            xferMgrProgress.waitForCompletion(transfer);
        } catch (Exception e) {
        }
    }


    @Test
    public void waitForCompletionAmazonServiceException() throws Exception {

        try {
            Mockito.doThrow(new AmazonServiceException("Amazon Service error")).when(transfer).waitForCompletion();
            xferMgrProgress.waitForCompletion(transfer);
        } catch (Exception e) {
        }
    }

    @Test
    public void waitForCompletionAmazonClientException() throws Exception {

        try {
            Mockito.doThrow(new AmazonClientException("Amazon Service error")).when(transfer).waitForCompletion();
            xferMgrProgress.waitForCompletion(transfer);
        } catch (Exception e) {
        }
    }

    @Test
    public void waitForCompletionInterruptedException() throws Exception {

        try {
            Mockito.doThrow(new InterruptedException("Amazon Service error")).when(transfer).waitForCompletion();
            xferMgrProgress.waitForCompletion(transfer);
        } catch (Exception e) {
        }
    }

}
