package org.recap.camel.route;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.csv.DeAccessionSummaryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by chenchulakshmig on 13/10/16.
 */
@Slf4j
@Component
public class FSDeAccessionSummaryReportRouteBuilder {


    /**
     * This method instantiates a new route builder to generate deaccession summary report to the file system.
     *
     * @param context          the context
     * @param reportsDirectory the reports directory
     */
    @Autowired
    public FSDeAccessionSummaryReportRouteBuilder(CamelContext context, @Value("${" + PropertyKeyConstants.SCSB_COLLECTION_REPORT_DIRECTORY + "}") String reportsDirectory) {
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ScsbCommonConstants.FS_DE_ACCESSION_SUMMARY_REPORT_Q)
                            .routeId(ScsbConstants.FS_DE_ACCESSION_SUMMARY_REPORT_ID)
                            .marshal().bindy(BindyType.Csv, DeAccessionSummaryRecord.class)
                            .to("file:" + reportsDirectory + File.separator + "?fileName=${in.header.fileName}-${date:now:ddMMMyyyyHHmmss}.csv&fileExist=append");
                }
            });
        } catch (Exception e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
    }

}
