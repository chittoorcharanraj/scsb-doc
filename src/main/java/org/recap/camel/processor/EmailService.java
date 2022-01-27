package org.recap.camel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbConstants;
import org.recap.model.camel.EmailPayLoad;
import org.recap.util.CommonUtil;
import org.recap.util.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Created by angelind on 25/7/17.
 */
@Slf4j
@Service
@Scope("prototype")
public class EmailService {



    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private PropertyUtil propertyUtil;

    @Autowired
    private CommonUtil commonUtil;

    @Value("${" + PropertyKeyConstants.SCSB_EMAIL_ASSIST_TO + "}")
    private String recapSupportEmailTo;

    @Value("${" + PropertyKeyConstants.SCSB_CGD_REPORT_MAIL_SUBJECT + "}")
    private String cgdReportEmailSubject;

    private String institutionCode;

    /**
     * Instantiates a new Email service.
     */
    public EmailService() {
        //Do Nothing
    }

    /**
     * Gets institution code.
     *
     * @return the institution code
     */
    public String getInstitutionCode() {
        return institutionCode;
    }

    /**
     * Sets institution code.
     *
     * @param institutionCode the institution code
     */
    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    /**
     * Send email for matching reports.
     *
     * @param exchange the exchange
     */
    public void sendEmailForMatchingReports(Exchange exchange) {
        log.info("matching algorithm reports email started ");
        String headerValue=Optional.ofNullable(exchange.getIn().getHeader(ScsbConstants.SUBJECT)).orElse("").equals(cgdReportEmailSubject)?cgdReportEmailSubject:ScsbConstants.MATCHING_REPORTS;
        producerTemplate.sendBodyAndHeader(ScsbConstants.EMAIL_Q, getEmailPayLoadForMatching(exchange), ScsbConstants.EMAIL_FOR, headerValue);
    }

    public void sendEmailForMatchingCGDReports(Exchange exchange) {
        log.info("matching algorithm reports email started ");
        String headerValue=Optional.ofNullable(exchange.getIn().getHeader(ScsbConstants.SUBJECT)).orElse("").equals(cgdReportEmailSubject)?cgdReportEmailSubject:ScsbConstants.MATCHING_REPORTS;
        producerTemplate.sendBodyAndHeader(ScsbConstants.EMAIL_Q, getEmailPayLoadForMatchingCGDReports(exchange), ScsbConstants.EMAIL_FOR, headerValue);
    }
    /**
     * Send email for accession reports.
     *
     * @param exchange the exchange
     */
    public void sendEmailForAccessionReports(Exchange exchange) {
        log.info("accession reports email started ");
        producerTemplate.sendBodyAndHeader(ScsbConstants.EMAIL_Q, getEmailPayLoadForAccession(exchange), ScsbConstants.EMAIL_FOR, ScsbConstants.ACCESSION_REPORTS);
    }

    /**
     * Get email pay load for matching email pay load.
     *
     * @param exchange the exchange
     * @return the email pay load
     */
    public EmailPayLoad getEmailPayLoadForMatching(Exchange exchange){
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        String fileNameWithPath = (String)exchange.getIn().getHeader("CamelAwsS3Key");
        File file = FileUtils.getFile(fileNameWithPath);
        String path = file.getParent();
        emailPayLoad.setTo(recapSupportEmailTo);
        setEmailRecipients(emailPayLoad);
        String subject = (String) exchange.getIn().getHeader(ScsbConstants.SUBJECT);
        Optional.ofNullable(subject).ifPresent(ins->emailPayLoad.setSubject(subject));
        emailPayLoad.setMessage(ScsbConstants.SUBJECT_MESSAGE_FOR_MATCHING_ALGORITHM + path);
        log.info("Matching Algorithm Reports email has been sent to : {} and cc : {} ",emailPayLoad.getTo(),emailPayLoad.getCc());
        return emailPayLoad;
    }

    public EmailPayLoad getEmailPayLoadForMatchingCGDReports(Exchange exchange){
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        String fileNameWithPath = (String)exchange.getIn().getHeader("CamelAwsS3Key");
        File file = FileUtils.getFile(fileNameWithPath);
        String path = file.getParent();
        setEmailRecipients(emailPayLoad,(String) exchange.getIn().getHeader(ScsbConstants.INSTITUTION));
        String subject = (String) exchange.getIn().getHeader(ScsbConstants.SUBJECT);
        Optional.ofNullable(subject).ifPresent(ins->emailPayLoad.setSubject(subject));
        emailPayLoad.setMessage(ScsbConstants.SUBJECT_MESSAGE_FOR_MATCHING_ALGORITHM + path);
        log.info("Matching Algorithm Reports email has been sent to : {} and cc : {} ",emailPayLoad.getTo(),emailPayLoad.getCc());
        return emailPayLoad;
    }

    /**
     * Get email pay load for accession email pay load.
     *
     * @param exchange the exchange
     * @return the email pay load
     */
    public EmailPayLoad getEmailPayLoadForAccession(Exchange exchange){
        EmailPayLoad emailPayLoad = new EmailPayLoad();
       String fileNameWithPath = (String)exchange.getIn().getHeader("CamelAwsS3Key");
        institutionCode = (String) exchange.getIn().getHeader(ScsbConstants.INSTITUTION_NAME);
        File file = FileUtils.getFile(fileNameWithPath);
        String absolutePath = file.getParent();
        String fileName = file.getName();
        emailPayLoad.setTo(recapSupportEmailTo);
        getCcBasedOnInstitution(emailPayLoad);
        emailPayLoad.setMessage("The Report " + fileName + " is available at the s3 location " + absolutePath);
        log.info("Accession Reports email has been sent to : {} and cc : {} ",emailPayLoad.getTo(),emailPayLoad.getCc());
        return emailPayLoad;
    }

    public void setEmailRecipients(EmailPayLoad emailPayLoad) {
        StringBuilder cc = new StringBuilder();
        List<String> institutionCodes = commonUtil.findAllInstitutionCodesExceptSupportInstitution();
        String matchingEmailTo="";
        for (String institution : institutionCodes) {
             matchingEmailTo = propertyUtil.getPropertyByInstitutionAndKey(institution, PropertyKeyConstants.ILS.ILS_EMAIL_MATCHING_CGD_REPORTS_TO);
             cc.append(StringUtils.isNotBlank(matchingEmailTo) ? matchingEmailTo + "," : "");

        }
        emailPayLoad.setCc(cc.toString());
    }

    public void setEmailRecipients(EmailPayLoad emailPayLoad, String institutionCode) {
        emailPayLoad.setTo(propertyUtil.getPropertyByInstitutionAndKey(institutionCode, PropertyKeyConstants.ILS.ILS_EMAIL_MATCHING_CGD_REPORTS_TO));
        emailPayLoad.setCc(propertyUtil.getPropertyByInstitutionAndKey(institutionCode, PropertyKeyConstants.ILS.ILS_EMAIL_MATCHING_CGD_REPORTS_CC));
    }

    private void getCcBasedOnInstitution(EmailPayLoad emailPayLoad) {
        emailPayLoad.setCc(propertyUtil.getPropertyByInstitutionAndKey(institutionCode, PropertyKeyConstants.ILS.ILS_EMAIL_MATCHING_REPORTS_TO));
    }
}
