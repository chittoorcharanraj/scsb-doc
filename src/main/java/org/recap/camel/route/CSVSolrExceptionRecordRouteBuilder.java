package org.recap.camel.route;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.model.csv.SolrExceptionReportCSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by angelind on 30/9/16.
 */
@Slf4j
@Component
public class CSVSolrExceptionRecordRouteBuilder {

    /**
     * This route builder is used to generate solr exception csv report to the file system.
     *
     * @param context                  the context
     * @param solrReportsDirectory the matching reports directory
     */
    @Autowired
    public CSVSolrExceptionRecordRouteBuilder(CamelContext context, @Value("${" + PropertyKeyConstants.SOLR_EXCEPTION_REPORT_DIRECTORY + "}") String solrReportsDirectory) {
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ScsbCommonConstants.CSV_SOLR_EXCEPTION_REPORT_Q)
                            .routeId(ScsbCommonConstants.CSV_SOLR_EXCEPTION_REPORT_ROUTE_ID)
                            .marshal().bindy(BindyType.Csv, SolrExceptionReportCSVRecord.class)
                            .to("file:" + solrReportsDirectory + File.separator + "?fileName=${in.header.fileName}-${date:now:ddMMMyyyy}.csv")
                            .onCompletion().log("File has been created successfully.");
                }
            });
        } catch (Exception e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
    }
}
