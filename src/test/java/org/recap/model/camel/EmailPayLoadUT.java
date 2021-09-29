package org.recap.model.camel;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.recap.BaseTestCaseUT;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EmailPayLoadUT extends BaseTestCaseUT {

    @Test
    public void Emailpayload() throws Exception {
        String itemBarcode = "123456";
        String itemInstitution = "PUL";
        String oldCgd = "shared";
        String newCgd = "open";
        String notes  = "test";
        String jobName = "Acession";
        String jobDescription = "test";
        String jobAction = "test";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String status = "IN";
        String message = "test ";
        String from = "test";
        String to = "test";
        String cc = "test";
        String subject = "Emailpayload";

        EmailPayLoad  emailpayload = new EmailPayLoad();
        emailpayload.setItemBarcode(itemBarcode);
        emailpayload.setItemInstitution(itemInstitution);
        emailpayload.setOldCgd(oldCgd);
        emailpayload.setNewCgd(newCgd);
        emailpayload.setNotes(notes);
        emailpayload.setJobName(jobName);
        emailpayload.setJobDescription(jobDescription);
        emailpayload.setJobAction(jobAction);
        emailpayload.setStartDate(timestamp);
        emailpayload.setStatus(status);
        emailpayload.setMessage(message);
        emailpayload.setFrom(from);
        emailpayload.setTo(to);
        emailpayload.setCc(cc);
        emailpayload.setSubject(subject);
        assertNotNull(emailpayload.getItemBarcode());
        assertNotNull(emailpayload.getItemInstitution());
        assertNotNull(emailpayload.getOldCgd());
        assertNotNull(emailpayload.getNewCgd());
        assertNotNull(emailpayload.getNotes());
        assertNotNull(emailpayload.getJobName());
        assertNotNull(emailpayload.getJobDescription());
        assertNotNull(emailpayload.getStatus());
        assertNotNull(emailpayload.getMessage());
        assertNotNull(emailpayload.getFrom());
        assertNotNull(emailpayload.getCc());
        assertNotNull(emailpayload.getTo());
        assertNotNull(emailpayload.getSubject());
        assertNotNull(emailpayload.getStartDate());
        assertNotNull(emailpayload.getJobAction());


    }
}