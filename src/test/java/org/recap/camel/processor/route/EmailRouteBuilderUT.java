package org.recap.camel.processor.route;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.PropertyKeyConstants;
import org.recap.camel.route.EmailRouteBuilder;
import org.recap.camel.route.S3SubmitCollectionReportRouteBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Charan Raj C created on 20/05/24
 */
@ContextConfiguration(classes = EmailRouteBuilderUT.Config.class)
public class EmailRouteBuilderUT extends BaseTestCaseUT {

    private CamelContext camelContext;

    @Before
    public void setup() {
        camelContext = new DefaultCamelContext();
    }

    @Value("${" + PropertyKeyConstants.S3_SUBMIT_COLLECTION_SUPPORT_TEAM_REPORT_DIR + "}")
    String submitCollectionS3ReportPath;

    @Test
    public void testS3SubmitReport() throws Exception {
        EmailRouteBuilder routeBuilder = new EmailRouteBuilder(camelContext, "username", "passwordDirectory", "from", "upadteCgdTo", "batchJobTo", "updateCgdSubject", "batchJobSubject", "smtpServer" ,"test","test@gmail.com");
    }

    static class Config {
        @Bean
        public EmailRouteBuilder emailRouteBuilder(CamelContext context,@Value("${" + PropertyKeyConstants.EMAIL_SMTP_SERVER_USERNAME + "}") String username, @Value("${" + PropertyKeyConstants.EMAIL_SMTP_SERVER_PASSWORD_FILE + "}") String passwordDirectory,
                                                   @Value("${" + PropertyKeyConstants.EMAIL_SMTP_SERVER_ADDRESS_FROM + "}") String from, @Value("${" + PropertyKeyConstants.EMAIL_SCSB_UPDATECGD_TO + "}") String upadteCgdTo, @Value("${" + PropertyKeyConstants.EMAIL_SCSB_UPDATECGD_CC + "}") String updateCGDCC, @Value("${" + PropertyKeyConstants.EMAIL_SCSB_BATCH_JOB_TO + "}") String batchJobTo,
                                                   @Value("${" + PropertyKeyConstants.EMAIL_SCSB_UPDATECGD_SUBJECT + "}") String updateCgdSubject, @Value("${" + PropertyKeyConstants.EMAIL_SCSB_BATCH_JOB_SUBJECT + "}") String batchJobSubject, @Value("${" + PropertyKeyConstants.EMAIL_SMTP_SERVER + "}") String smtpServer
                ,@Value("${" + PropertyKeyConstants.SCSB_CGD_REPORT_MAIL_SUBJECT + "}")
                                                           String cgdReportEmailSubject)  {
            return new EmailRouteBuilder(context, "username", "passwordDirectory", "from", "upadteCgdTo", "batchJobTo", "updateCgdSubject", "batchJobSubject", "smtpServer" ,"test","test@gmail.com");
        }

        @Bean
        public CamelContext camelContext() {
            return Mockito.mock(CamelContext.class);
        }

        @Bean
        public Logger logger() {
            return Mockito.mock(Logger.class);
        }
    }



}
