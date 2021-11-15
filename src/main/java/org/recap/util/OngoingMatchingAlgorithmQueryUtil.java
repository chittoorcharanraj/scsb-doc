package org.recap.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.solr.SolrIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by rajeshbabuk on 19/Oct/2021
 */
@Slf4j
@Component
public class OngoingMatchingAlgorithmQueryUtil {

    @Autowired
    private SolrQueryBuilder solrQueryBuilder;

    @Autowired
    private DateUtil dateUtil;

    @Resource(name = "recapSolrTemplate")
    private SolrTemplate solrTemplate;

    /**
     * This method gets query for ongoing matching for Grouping Process.
     *
     * @return the solr document list
     */
    public String prepareQueryForOngoingMatchingCgdUpdateProcessBasedOnCriteria(SolrIndexRequest solrIndexRequest) throws Exception {
        return prepareQueryForOngoingMatchingGroupingOrCgdUpdateProcessBasedOnCriteria(solrIndexRequest, true);
    }

    /**
     * This method gets query for ongoing matching for Grouping Process.
     *
     * @return the solr document list
     */
    public String prepareQueryForOngoingMatchingGroupingProcessBasedOnCriteria(SolrIndexRequest solrIndexRequest) throws Exception {
        return prepareQueryForOngoingMatchingGroupingOrCgdUpdateProcessBasedOnCriteria(solrIndexRequest, false);
    }

    /**
     * This method gets query for ongoing matching for Grouping Process.
     *
     * @return the solr document list
     */
    public String prepareQueryForOngoingMatchingGroupingOrCgdUpdateProcessBasedOnCriteria(SolrIndexRequest solrIndexRequest, boolean isCgdProcess) throws Exception {
        String query = null;
        String matchBy = solrIndexRequest.getMatchBy();
        boolean includeMaQualifier = solrIndexRequest.isIncludeMaQualifier();
        if (StringUtils.isBlank(matchBy)) {
            query = solrQueryBuilder.getQueryForOngoingMatchingForGroupingOrCgdUpdateProcess(includeMaQualifier, isCgdProcess);
        } else if (matchBy.equalsIgnoreCase(ScsbConstants.FROM_DATE)) {
            log.info("From Date : {}", solrIndexRequest.getFromDate());
            query = solrQueryBuilder.getQueryForOngoingMatchingBasedOnDateForGroupingOrCgdUpdateProcess(getFormattedDateString(getFormattedDate(solrIndexRequest.getFromDate())), includeMaQualifier, isCgdProcess);
        } else if (matchBy.equalsIgnoreCase(ScsbConstants.DATE_RANGE)) {
            log.info("From Date : {}, To Date : {}", solrIndexRequest.getDateFrom(), solrIndexRequest.getDateTo());
            query = solrQueryBuilder.getQueryForOngoingMatchingBasedOnDateRangeForGroupingOrCgdUpdateProcess(getUTCFormatDateString(getFormattedDateFrom(solrIndexRequest.getDateFrom())), getUTCFormatDateString(getFormattedDateTo(solrIndexRequest.getDateTo())), includeMaQualifier, isCgdProcess);
        } else if (matchBy.equalsIgnoreCase(ScsbConstants.BIB_ID_LIST)) {
            query = solrQueryBuilder.getQueryForOngoingMatchingBasedOnBibIdsForGroupingOrCgdUpdateProcess(solrIndexRequest.getBibIds(), includeMaQualifier, isCgdProcess);
        } else if (matchBy.equalsIgnoreCase(ScsbConstants.BIB_ID_RANGE)) {
            log.info("From Bib Id : {}, To Bib Id : {}", solrIndexRequest.getFromBibId(), solrIndexRequest.getToBibId());
            query = solrQueryBuilder.getQueryForOngoingMatchingBasedOnBibIdRangeForGroupingOrCgdUpdateProcess(solrIndexRequest.getFromBibId(), solrIndexRequest.getToBibId(), includeMaQualifier, isCgdProcess);
        }
        return query;
    }

    private Date getFormattedDate(String fromDate) throws ParseException {
        Date date = new SimpleDateFormat(ScsbConstants.ONGOING_MATCHING_DATE_FORMAT).parse(fromDate);
        return dateUtil.getFromDate(date);
    }

    private Date getFormattedDateFrom(String dateFrom) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(ScsbConstants.ONGOING_MATCHING_DATE_TIME_FORMAT);
        return StringUtils.isNotBlank(dateFrom) ? dateFormatter.parse(dateFrom) : dateUtil.getFromDate(new Date());
    }

    private Date getFormattedDateTo(String dateTo) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(ScsbConstants.ONGOING_MATCHING_DATE_TIME_FORMAT);
        return StringUtils.isNotBlank(dateTo) ? dateFormatter.parse(dateTo) : dateUtil.getToDate(new Date());
    }

    /**
     * This method gets formatted date.
     *
     * @param inputDate the input date
     * @return the formatted date string
     */
    public String getFormattedDateString(Date inputDate) {
        return getUTCFormatDateString(inputDate) + ScsbCommonConstants.SOLR_DATE_RANGE_TO_NOW;
    }

    private String getUTCFormatDateString(Date inputDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ScsbCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        String utcStr = null;
        try {
            String inputDateString = simpleDateFormat.format(inputDate);
            Date date = simpleDateFormat.parse(inputDateString);
            DateFormat format = new SimpleDateFormat(ScsbCommonConstants.UTC_DATE_FORMAT);
            format.setTimeZone(TimeZone.getTimeZone(ScsbCommonConstants.UTC));
            utcStr = format.format(date);
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        return utcStr;
    }

    /**
     * This method fetches data for ongoing matching for the passed query.
     *
     * @param batchSize the batch size
     * @param start     the start
     * @return the solr document list
     */
    public QueryResponse fetchDataByQuery(String query, Integer batchSize, Integer start) {
        try {
            SolrQuery solrQuery = new SolrQuery(query);
            solrQuery.setStart(start);
            solrQuery.setRows(batchSize);
            return solrTemplate.getSolrClient().query(solrQuery, SolrRequest.METHOD.POST);

        } catch (SolrServerException | IOException e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return null;
    }


}
