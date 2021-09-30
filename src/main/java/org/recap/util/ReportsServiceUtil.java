package org.recap.util;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.IncompleteReportBibDetails;
import org.recap.model.jpa.DeaccessionItemChangeLog;
import org.recap.model.jpa.MatchingScoreTranslationEntity;
import org.recap.model.reports.ItemDetails;
import org.recap.model.reports.ReportsInstitutionForm;
import org.recap.model.reports.ReportsRequest;
import org.recap.model.reports.ReportsResponse;
import org.recap.model.reports.TitleMatchCount;
import org.recap.model.reports.TitleMatchedReport;
import org.recap.model.reports.TitleMatchedReports;
import org.recap.model.search.DeaccessionItemResultsRow;
import org.recap.model.search.IncompleteReportResultsRow;
import org.recap.model.solr.BibItem;
import org.recap.model.solr.Item;
import org.recap.repository.jpa.DeaccesionItemChangeLogDetailsRepository;
import org.recap.repository.jpa.MatchingScoreTranslationRepository;
import org.recap.repository.solr.impl.BibSolrDocumentRepositoryImpl;
import org.recap.service.TitleMatchReportExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by rajeshbabuk on 13/1/17.
 */
@Service
public class ReportsServiceUtil {

    @Resource(name = "recapSolrTemplate")
    private SolrTemplate solrTemplate;

    @Autowired
    private SolrQueryBuilder solrQueryBuilder;

    @Autowired
    private BibSolrDocumentRepositoryImpl bibSolrDocumentRepository;

    @Autowired
    private DeaccesionItemChangeLogDetailsRepository deaccesionItemChangeLogDetailsRepository;

    @Autowired
    private MatchingScoreTranslationRepository matchingScoreTranslationRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private TitleMatchReportExportService titleMatchReportExportService;


    @Value("${" + PropertyKeyConstants.TITLE_REPORT_LIMIT_BIBS_PER_FILE + "}")
    private Integer titleReportExportBibsLimitPerFile;



    /**
     * This method populates accession and deaccession item counts from solr for report screen in UI.
     *
     * @param reportsRequest the reports request
     * @return the reports response
     * @throws Exception the exception
     */
    public ReportsResponse populateAccessionDeaccessionItemCounts(ReportsRequest reportsRequest) throws Exception {
        ReportsResponse reportsResponse = new ReportsResponse();
        reportsResponse.setReportsInstitutionFormList(new ArrayList<>());
        String solrFormattedDate = getSolrFormattedDates(reportsRequest.getAccessionDeaccessionFromDate(), reportsRequest.getAccessionDeaccessionToDate());
        populateAccessionCounts(reportsRequest, reportsResponse, solrFormattedDate);
        populateDeaccessionCounts(reportsRequest, reportsResponse, solrFormattedDate);
        return reportsResponse;
    }

    /**
     * This method populates cgd item counts from solr for report screen in UI.
     *
     * @param reportsRequest the reports request
     * @return the reports response
     * @throws Exception the exception
     */
    public ReportsResponse populateCgdItemCounts(ReportsRequest reportsRequest) throws Exception {
        ReportsResponse reportsResponse = new ReportsResponse();
        reportsResponse.setReportsInstitutionFormList(new ArrayList<>());
        for (String owningInstitution : reportsRequest.getOwningInstitutions()) {
            ReportsInstitutionForm reportsInstitutionForm = new ReportsInstitutionForm();
            reportsInstitutionForm.setInstitution(owningInstitution);
            for (String collectionGroupDesignation : reportsRequest.getCollectionGroupDesignations()) {
                SolrQuery query = solrQueryBuilder.buildSolrQueryForCGDReports(owningInstitution, collectionGroupDesignation);
                long numFound = getNumFound(query);
                if (collectionGroupDesignation.equalsIgnoreCase(ScsbCommonConstants.REPORTS_OPEN)) {
                    reportsInstitutionForm.setOpenCgdCount(numFound);
                } else if (collectionGroupDesignation.equalsIgnoreCase(ScsbCommonConstants.REPORTS_SHARED)) {
                    reportsInstitutionForm.setSharedCgdCount(numFound);
                } else if (collectionGroupDesignation.equalsIgnoreCase(ScsbCommonConstants.REPORTS_PRIVATE)) {
                    reportsInstitutionForm.setPrivateCgdCount(numFound);
                }else if (collectionGroupDesignation.equalsIgnoreCase(ScsbCommonConstants.REPORTS_COMMITTED)) {
                    reportsInstitutionForm.setCommittedCgdCount(numFound);
                }else if (collectionGroupDesignation.equalsIgnoreCase(ScsbCommonConstants.REPORTS_UNCOMMITTABLE)) {
                    reportsInstitutionForm.setUncommittableCgdCount(numFound);
                }
            }
            reportsResponse.getReportsInstitutionFormList().add(reportsInstitutionForm);
        }
        return reportsResponse;
    }

    private long getNumFound(SolrQuery query) throws SolrServerException, IOException {
        query.setRows(0);
        query.setGetFieldStatistics(true);
        query.setGetFieldStatistics(ScsbConstants.DISTINCT_VALUES_FALSE);
        query.addStatsFieldCalcDistinct(ScsbCommonConstants.BARCODE, true);
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(query);
        return queryResponse.getFieldStatsInfo().get(ScsbCommonConstants.BARCODE).getCountDistinct();
    }

    /**
     * This method gets deaccession information results from solr and populate them in report screen (UI).
     *
     * @param reportsRequest the reports request
     * @return the reports response
     * @throws Exception the exception
     */
    public ReportsResponse populateDeaccessionResults(ReportsRequest reportsRequest) throws Exception {
        List<Item> itemList = new ArrayList<>();
        List<Integer> itemIdList = new ArrayList<>();
        List<Integer> bibIdList = new ArrayList<>();
        ReportsResponse reportsResponse = new ReportsResponse();
        String date = getSolrFormattedDates(reportsRequest.getAccessionDeaccessionFromDate(), reportsRequest.getAccessionDeaccessionToDate());
        SolrQuery query = solrQueryBuilder.buildSolrQueryForDeaccesionReportInformation(date, reportsRequest.getDeaccessionOwningInstitution(), true);
        query.setRows(reportsRequest.getPageSize());
        query.setStart(reportsRequest.getPageNumber() * reportsRequest.getPageSize());
        query.set(ScsbConstants.GROUP, true);
        query.set(ScsbConstants.GROUP_FIELD, ScsbCommonConstants.BARCODE);
        query.setGetFieldStatistics(true);
        query.setGetFieldStatistics(ScsbConstants.DISTINCT_VALUES_FALSE);
        query.addStatsFieldCalcDistinct(ScsbCommonConstants.BARCODE, true);
        query.setSort(ScsbConstants.ITEM_LAST_UPDATED_DATE, SolrQuery.ORDER.desc);
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(query);
        List<GroupCommand> values = queryResponse.getGroupResponse().getValues();
        for (GroupCommand groupCommand : values) {
            List<Group> groupList = groupCommand.getValues();
            for (Group group : groupList) {
                SolrDocumentList result = group.getResult();
                for (SolrDocument solrDocument : result) {
                    boolean isDeletedItem = (boolean) solrDocument.getFieldValue(ScsbCommonConstants.IS_DELETED_ITEM);
                    if (isDeletedItem) {
                        Item item = commonUtil.getItem(solrDocument);
                        itemList.add(item);
                        itemIdList.add(item.getItemId());
                        bibIdList.add(item.getItemBibIdList().get(0));
                    }
                }

            }
        }
        long numFound = queryResponse.getFieldStatsInfo().get(ScsbCommonConstants.BARCODE).getCountDistinct();
        reportsResponse.setTotalRecordsCount(String.valueOf(numFound));
        int totalPagesCount = (int) Math.ceil((double) numFound / (double) reportsRequest.getPageSize());
        if (totalPagesCount == 0) {
            reportsResponse.setTotalPageCount(1);
        } else {
            reportsResponse.setTotalPageCount(totalPagesCount);
        }
        String bibIdJoin = StringUtils.join(bibIdList, ",");
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(ScsbConstants.BIB_DOC_TYPE);
        solrQuery.addFilterQuery(ScsbConstants.SOLR_BIB_ID + StringEscapeUtils.escapeJava(bibIdJoin).replace(",", "\" \""));
        solrQuery.setFields(ScsbCommonConstants.BIB_ID, ScsbConstants.TITLE_DISPLAY);
        solrQuery.setRows(Integer.MAX_VALUE);
        QueryResponse response = solrTemplate.getSolrClient().query(solrQuery);
        Map<Integer, String> map = new HashMap<>();
        SolrDocumentList list = response.getResults();
        for (Iterator<SolrDocument> iterator = list.iterator(); iterator.hasNext(); ) {
            SolrDocument solrDocument = iterator.next();
            map.put((Integer) solrDocument.getFieldValue(ScsbCommonConstants.BIB_ID), (String) solrDocument.getFieldValue(ScsbConstants.TITLE_DISPLAY));
        }
        SimpleDateFormat simpleDateFormat = getSimpleDateFormatForReports();
        List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = new ArrayList<>();
        for (Item item : itemList) {
            DeaccessionItemResultsRow deaccessionItemResultsRow = new DeaccessionItemResultsRow();
            deaccessionItemResultsRow.setItemId(item.getItemId());
            String deaccessionDate = simpleDateFormat.format(item.getItemLastUpdatedDate());
            if (map.containsKey(item.getItemBibIdList().get(0))) {
                deaccessionItemResultsRow.setTitle(map.get(item.getItemBibIdList().get(0)));
            }
            deaccessionItemResultsRow.setDeaccessionDate(deaccessionDate);
            deaccessionItemResultsRow.setDeaccessionOwnInst(item.getOwningInstitution());
            deaccessionItemResultsRow.setItemBarcode(item.getBarcode());
            List<DeaccessionItemChangeLog> itemChangeLogEntityList = deaccesionItemChangeLogDetailsRepository.findByRecordIdAndOperationTypeAndOrderByUpdatedDateDesc(item.getItemId(), ScsbCommonConstants.REPORTS_DEACCESSION);
            if (CollectionUtils.isNotEmpty(itemChangeLogEntityList)) {
                DeaccessionItemChangeLog itemChangeLogEntity = itemChangeLogEntityList.get(0);
                deaccessionItemResultsRow.setDeaccessionNotes(itemChangeLogEntity.getNotes());
            }
            deaccessionItemResultsRow.setDeaccessionCreatedBy(item.getItemLastUpdatedBy());
            deaccessionItemResultsRow.setCgd(item.getCollectionGroupDesignation());
            deaccessionItemResultsRowList.add(deaccessionItemResultsRow);
        }
        reportsResponse.setDeaccessionItemResultsRows(deaccessionItemResultsRowList);
        return reportsResponse;
    }

    /**
     * This method is used to populate incomplete records report.
     *
     * @param reportsRequest the reports request
     * @return the reports response
     * @throws Exception the exception
     */
    public ReportsResponse populateIncompleteRecordsReport(ReportsRequest reportsRequest) throws Exception {
        ReportsResponse reportsResponse = new ReportsResponse();
        List<Integer> bibIdList = new ArrayList<>();
        List<Item> itemList = new ArrayList<>();
        SolrQuery solrQuery;
        QueryResponse queryResponse;
        solrQuery = solrQueryBuilder.buildSolrQueryForIncompleteReports(reportsRequest.getIncompleteRequestingInstitution());
        if (!reportsRequest.isExport()) {
            solrQuery.setStart(reportsRequest.getIncompletePageSize() * reportsRequest.getIncompletePageNumber());
            solrQuery.setRows(reportsRequest.getIncompletePageSize());
        }
        solrQuery.set(ScsbConstants.GROUP, true);
        solrQuery.set(ScsbConstants.GROUP_FIELD, ScsbCommonConstants.BARCODE);
        solrQuery.setGetFieldStatistics(true);
        solrQuery.setGetFieldStatistics(ScsbConstants.DISTINCT_VALUES_FALSE);
        solrQuery.addStatsFieldCalcDistinct(ScsbCommonConstants.BARCODE, true);
        solrQuery.setSort(ScsbConstants.ITEM_CREATED_DATE, SolrQuery.ORDER.desc);
        queryResponse = solrTemplate.getSolrClient().query(solrQuery);
        long numFound = queryResponse.getFieldStatsInfo().get(ScsbCommonConstants.BARCODE).getCountDistinct();
        if (reportsRequest.isExport()) {
            solrQuery = solrQueryBuilder.buildSolrQueryForIncompleteReports(reportsRequest.getIncompleteRequestingInstitution());
            solrQuery.setStart(0);
            solrQuery.setRows((int) numFound);
            solrQuery.set(ScsbConstants.GROUP, true);
            solrQuery.set(ScsbConstants.GROUP_FIELD, ScsbCommonConstants.BARCODE);
            solrQuery.setGetFieldStatistics(true);
            solrQuery.setGetFieldStatistics(ScsbConstants.DISTINCT_VALUES_FALSE);
            solrQuery.addStatsFieldCalcDistinct(ScsbCommonConstants.BARCODE, true);
            solrQuery.setSort(ScsbConstants.ITEM_CREATED_DATE, SolrQuery.ORDER.desc);
            queryResponse = solrTemplate.getSolrClient().query(solrQuery);
        }

        List<GroupCommand> values = queryResponse.getGroupResponse().getValues();
        for (GroupCommand groupCommand : values) {
            List<Group> groupList = groupCommand.getValues();
            for (Group group : groupList) {
                SolrDocumentList result = group.getResult();
                for (SolrDocument itemDocument : result) {
                    Item item = commonUtil.getItem(itemDocument);
                    itemList.add(item);
                    bibIdList.add(item.getItemBibIdList().get(0));
                }
            }

        }
        if (!bibIdList.isEmpty()) {
            Map<Integer, IncompleteReportBibDetails> bibDetailsMap = new HashMap<>();
            List<List<Integer>> partionedBibIdList = Lists.partition(bibIdList, 1000);
            for (List<Integer> bibIds : partionedBibIdList) {
                bibDetailsMap = getBibDetailsIncompleteReport(bibIds, bibDetailsMap);
            }
            List<IncompleteReportResultsRow> incompleteReportResultsRows = new ArrayList<>();
            for (Item item : itemList) {
                IncompleteReportResultsRow incompleteReportResultsRow = new IncompleteReportResultsRow();
                incompleteReportResultsRow.setOwningInstitution(item.getOwningInstitution());
                IncompleteReportBibDetails incompleteReportBibDetails = bibDetailsMap.get(item.getItemBibIdList().get(0));
                if (incompleteReportBibDetails != null) {
                    incompleteReportResultsRow.setTitle(incompleteReportBibDetails.getTitle());
                    incompleteReportResultsRow.setAuthor(incompleteReportBibDetails.getAuthorDisplay());
                }
                incompleteReportResultsRow.setCreatedDate(getFormattedDates(item.getItemCreatedDate()));
                incompleteReportResultsRow.setCustomerCode(item.getCustomerCode());
                incompleteReportResultsRow.setBarcode(item.getBarcode());
                incompleteReportResultsRows.add(incompleteReportResultsRow);
            }
            int totalPagesCount = (int) Math.ceil((double) numFound / (double) reportsRequest.getIncompletePageSize());
            reportsResponse.setIncompleteTotalPageCount(totalPagesCount);
            reportsResponse.setIncompleteTotalRecordsCount(String.valueOf(numFound));
            reportsResponse.setIncompleteReportResultsRows(incompleteReportResultsRows);
        }
        return reportsResponse;
    }

    private Map<Integer, IncompleteReportBibDetails> getBibDetailsIncompleteReport(List<Integer> bibIdList, Map<Integer, IncompleteReportBibDetails> bibDetailsMap) throws SolrServerException, IOException {
        SolrQuery bibDetailsQuery = solrQueryBuilder.buildSolrQueryToGetBibDetails(bibIdList, Integer.MAX_VALUE);
        QueryResponse bibDetailsResponse = solrTemplate.getSolrClient().query(bibDetailsQuery, SolrRequest.METHOD.POST);
        SolrDocumentList bibDocumentList = bibDetailsResponse.getResults();
        for (SolrDocument bibDetail : bibDocumentList) {
            IncompleteReportBibDetails incompleteReportBibDetails = new IncompleteReportBibDetails();
            incompleteReportBibDetails.setTitle((String) bibDetail.getFieldValue(ScsbConstants.TITLE_DISPLAY));
            incompleteReportBibDetails.setAuthorDisplay((String) bibDetail.getFieldValue(ScsbConstants.AUTHOR_DISPLAY));
            bibDetailsMap.put((Integer) bibDetail.getFieldValue(ScsbCommonConstants.BIB_ID), incompleteReportBibDetails);
        }
        return bibDetailsMap;
    }

    private String getFormattedDates(Date gotDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ScsbCommonConstants.SIMPLE_DATE_FORMAT_REPORTS);
        return simpleDateFormat.format(gotDate);

    }


    /**
     * This method gets the accession count from solr
     * @param reportsRequest
     * @param reportsResponse
     * @param solrFormattedDate
     * @throws Exception
     */
    private void populateAccessionCounts(ReportsRequest reportsRequest, ReportsResponse reportsResponse, String solrFormattedDate) throws Exception {
        for (String owningInstitution : reportsRequest.getOwningInstitutions()) {
            ReportsInstitutionForm reportsInstitutionForm = getReportInstitutionFormByInstitution(owningInstitution, reportsResponse.getReportsInstitutionFormList());
            reportsInstitutionForm.setInstitution(owningInstitution);
            for (String collectionGroupDesignation : reportsRequest.getCollectionGroupDesignations()) {
                SolrQuery query = solrQueryBuilder.buildSolrQueryForAccessionReports(solrFormattedDate, owningInstitution, false, collectionGroupDesignation);
                long numFound = getNumFound(query);
                if (collectionGroupDesignation.equalsIgnoreCase(ScsbCommonConstants.REPORTS_OPEN)) {
                    reportsInstitutionForm.setAccessionOpenCount(numFound);
                } else if (collectionGroupDesignation.equalsIgnoreCase(ScsbCommonConstants.REPORTS_SHARED)) {
                    reportsInstitutionForm.setAccessionSharedCount(numFound);
                } else if (collectionGroupDesignation.equalsIgnoreCase(ScsbCommonConstants.REPORTS_PRIVATE)) {
                    reportsInstitutionForm.setAccessionPrivateCount(numFound);
                }
            }
            reportsResponse.getReportsInstitutionFormList().add(reportsInstitutionForm);
        }
    }

    /**
     * This method gets the deaccession count from the solr
     * @param reportsRequest
     * @param reportsResponse
     * @param solrFormattedDate
     * @throws Exception
     */
    private void populateDeaccessionCounts(ReportsRequest reportsRequest, ReportsResponse reportsResponse, String solrFormattedDate) throws Exception {
        for (String owningInstitution : reportsRequest.getOwningInstitutions()) {
            ReportsInstitutionForm reportsInstitutionForm = getReportInstitutionFormByInstitution(owningInstitution, reportsResponse.getReportsInstitutionFormList());
            reportsInstitutionForm.setInstitution(owningInstitution);
            for (String collectionGroupDesignation : reportsRequest.getCollectionGroupDesignations()) {
                SolrQuery query = solrQueryBuilder.buildSolrQueryForDeaccessionReports(solrFormattedDate, owningInstitution, true, collectionGroupDesignation);
                long numFound = getNumFound(query);
                if (collectionGroupDesignation.equalsIgnoreCase(ScsbCommonConstants.REPORTS_OPEN)) {
                    reportsInstitutionForm.setDeaccessionOpenCount(numFound);
                } else if (collectionGroupDesignation.equalsIgnoreCase(ScsbCommonConstants.REPORTS_SHARED)) {
                    reportsInstitutionForm.setDeaccessionSharedCount(numFound);
                } else if (collectionGroupDesignation.equalsIgnoreCase(ScsbCommonConstants.REPORTS_PRIVATE)) {
                    reportsInstitutionForm.setDeaccessionPrivateCount(numFound);
                }
            }
        }
    }

    /**
     * This mehtod will return the form for matched owning institution or creates new form and returns it.
     * @param owningInstitution
     * @param reportsInstitutionFormList
     * @return
     */
    private ReportsInstitutionForm getReportInstitutionFormByInstitution(String owningInstitution, List<ReportsInstitutionForm> reportsInstitutionFormList) {
        if (!reportsInstitutionFormList.isEmpty()) {
            for (ReportsInstitutionForm reportsOwningInstitutionForm : reportsInstitutionFormList) {
                if (StringUtils.isNotBlank(owningInstitution) && owningInstitution.equalsIgnoreCase(reportsOwningInstitutionForm.getInstitution())) {
                    return reportsOwningInstitutionForm;
                }
            }
        }
        return new ReportsInstitutionForm();
    }

    private String getSolrFormattedDates(String requestedFromDate, String requestedToDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = getSimpleDateFormatForReports();
        Date fromDate = simpleDateFormat.parse(requestedFromDate);
        Date toDate = simpleDateFormat.parse(requestedToDate);
        Date fromDateTime = dateUtil.getFromDateAccession(fromDate);
        Date toDateTime = dateUtil.getToDateAccession(toDate);
        String formattedFromDate = getFormattedDateString(fromDateTime);
        String formattedToDate = getFormattedDateString(toDateTime);
        return formattedFromDate + " TO " + formattedToDate;
    }

    private SimpleDateFormat getSimpleDateFormatForReports() {
        return new SimpleDateFormat(ScsbCommonConstants.SIMPLE_DATE_FORMAT_REPORTS);
    }
    private String convertDateToString(Date date){
        SimpleDateFormat dateFormat = getSimpleDateFormatForReports();
        return dateFormat.format(date);
    }
    private String getFormattedDateString(Date inputDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ScsbCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        String utcStr;
        String dateString = simpleDateFormat.format(inputDate);
        Date date = simpleDateFormat.parse(dateString);
        DateFormat format = new SimpleDateFormat(ScsbCommonConstants.UTC_DATE_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone(ScsbCommonConstants.UTC));
        utcStr = format.format(date);
        return utcStr;
    }
    public TitleMatchedReport titleMatchCount(TitleMatchedReport titleMatchedReport) throws Exception {
        List<TitleMatchCount> titleMatchCountList = new ArrayList<>();
        String solrFormattedDate = getSolrFormattedDates(convertDateToString(titleMatchedReport.getFromDate()),convertDateToString(titleMatchedReport.getToDate()));
                String titleMatch = titleMatchedReport.getTitleMatch();
                String owningInstitution = titleMatchedReport.getOwningInst();
                for (String cgd : titleMatchedReport.getCgd()) {
                    TitleMatchCount titleMatchCount = new TitleMatchCount();
                    String matchingIdentifier = (titleMatch.equals(ScsbConstants.TITLE_MATCHED)) ? "" : "-";
                    SolrQuery query = solrQueryBuilder.buildQueryTitleMatchCount(solrFormattedDate, owningInstitution, cgd, matchingIdentifier);
                    QueryResponse queryResponse = solrTemplate.getSolrClient().query(query);
                    long count = queryResponse.getResults().getNumFound();
                    titleMatchCount.setCount(count);
                    titleMatchCount.setTitleMatched(titleMatch);
                    titleMatchCount.setCgd(cgd);
                    titleMatchCount.setOwningInst(owningInstitution);
                    titleMatchCountList.add(titleMatchCount);
                }
        titleMatchedReport.setTitleMatchCounts(titleMatchCountList);
        return titleMatchedReport;
    }

    public TitleMatchedReport titleMatchReports(TitleMatchedReport titleMatchedReport) throws Exception {
        List<MatchingScoreTranslationEntity> msList = (List<MatchingScoreTranslationEntity>) matchingScoreTranslationRepository.findAll();
        Map<Integer, String> msMap = prepareMSMap(msList);
        List<BibItem> bibItems = new ArrayList<>();
        SolrQuery query = appendSolrQueryForTitle(titleMatchedReport);
        query.setRows(titleMatchedReport.getPageSize());
        query.setStart((titleMatchedReport.getPageNumber() * (titleMatchedReport.getPageSize())));
        if (titleMatchedReport.getTitleMatch().equalsIgnoreCase(ScsbConstants.TITLE_MATCHED))
            query.setSort(ScsbConstants.MATCHING_IDENTIFIER, SolrQuery.ORDER.desc);
        else
            query.setSort(ScsbConstants.BIB_CREATED_DATE, SolrQuery.ORDER.desc);
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(query);
        titleMatchedReport.setTotalRecordsCount(queryResponse.getResults().getNumFound());
        int totalPagesCount = (int) Math.ceil((double) (titleMatchedReport.getTotalRecordsCount()) / (double) (titleMatchedReport.getPageSize()));
        titleMatchedReport.setTotalPageCount(totalPagesCount);
        SolrDocumentList bibSolrDocumentList = queryResponse.getResults();
        setDataForBibItems(bibSolrDocumentList,bibItems);
        return setBibItems(titleMatchedReport, bibItems,msMap);
    }

    private TitleMatchedReport setBibItems(TitleMatchedReport titleMatchedReport, List<BibItem> bibItems, Map<Integer, String> msMap) {
        List<TitleMatchedReports> titleMatchedReportsList = new ArrayList<>();
        for (BibItem bibItem : bibItems) {
            titleMatchedReportsList.addAll(prepareTitleReport(bibItem,null,msMap,false));
        }
        titleMatchedReport.setTitleMatchedReports(titleMatchedReportsList);
        return titleMatchedReport;
    }

    private List<TitleMatchedReports> setDataToTitleMatchReports(BibItem bibItem, Map<Integer, String> msMap) {

        List<TitleMatchedReports> titleMatchedReportsList = new ArrayList<>();
        for (Item item : bibItem.getItems()) {
            titleMatchedReportsList.addAll(prepareTitleReport(bibItem, item, msMap, true));
        }
        return titleMatchedReportsList;
    }

    private List<TitleMatchedReports> prepareTitleReport(BibItem bibItem, Item item, Map<Integer, String> msMap, Boolean isReportExport) {
        List<TitleMatchedReports> titleMatchedReportsList = new ArrayList<>();
        TitleMatchedReports titleMatchedReports = new TitleMatchedReports();
        titleMatchedReports.setBibId(bibItem.getOwningInstitutionBibId());
        if (bibItem.getMatchingIdentifier() != null)
            titleMatchedReports.setDuplicateCode(bibItem.getMatchingIdentifier());
        else
            titleMatchedReports.setDuplicateCode("");
        titleMatchedReports.setScsbId(bibItem.getBibId());
        if (bibItem.getLccn() != null)
            titleMatchedReports.setLccn(bibItem.getLccn());
        else
            titleMatchedReports.setLccn("");
        if (bibItem.getTitle() != null)
            titleMatchedReports.setTitle(bibItem.getTitle());
        if (bibItem.getOclcNumber() != null)
            titleMatchedReports.setOclc(String.join(", ", bibItem.getOclcNumber()));
        else
            titleMatchedReports.setOclc("");
        if (bibItem.getIsbn() != null)
            titleMatchedReports.setIsbn(String.join(", ", bibItem.getIsbn()));
        else
            titleMatchedReports.setIsbn("");
        if (bibItem.getIssn() != null)
            titleMatchedReports.setIssn(String.join(", ", bibItem.getIssn()));
        else
            titleMatchedReports.setIssn("");
        if (bibItem.getMatchScore() != null) {
            titleMatchedReports.setMatchScore(String.valueOf(bibItem.getMatchScore()));
            titleMatchedReports.setMatchScoreTranslated(String.valueOf(msMap.get(bibItem.getMatchScore())));
        } else {
            titleMatchedReports.setMatchScore("");
            titleMatchedReports.setMatchScoreTranslated("");
        }
        if (bibItem.getAnamolyFlag() != null)
            titleMatchedReports.setAnamolyFlag(String.valueOf(bibItem.getAnamolyFlag() ? 1 : 0));
        else
            titleMatchedReports.setAnamolyFlag("");
        if(item !=null) {
            titleMatchedReports.setItemBarcode(item.getBarcode());
            titleMatchedReports.setCgd(item.getCollectionGroupDesignation());
            titleMatchedReports.setChronologyAndEnum((item.getVolumePartYear()!=null)?item.getVolumePartYear(): "");
        }
        titleMatchedReports.setPublisher((bibItem.getPublisher() != null) ? bibItem.getPublisher() : "");
        titleMatchedReports.setPublicationDate((bibItem.getPublicationDate() == null) ? "" : bibItem.getPublicationDate());
        titleMatchedReports.setOwningInstitution(bibItem.getOwningInstitution());
        if (!isReportExport) {
            if (bibItem.getItems().size() == 1) {
                titleMatchedReports.setItemBarcode(bibItem.getItems().get(0).getBarcode());
                titleMatchedReports.setCgd(bibItem.getItems().get(0).getCollectionGroupDesignation());
                titleMatchedReports.setChronologyAndEnum(bibItem.getItems().get(0).getVolumePartYear());
            } else {
                titleMatchedReports.setItemDetails(setItemDetails(bibItem));
            }
        }
        titleMatchedReportsList.add(titleMatchedReports);
        return titleMatchedReportsList;
    }
    private Map<Integer, String> prepareMSMap(List<MatchingScoreTranslationEntity> msList) {
        Map<Integer,String> msListTemp = new HashMap<>();
        msList.stream().forEach(a->{
            msListTemp.put(a.getDecMaScore(), a.getStringMaScore());
        });
        return msListTemp;
    }

    private List<ItemDetails> setItemDetails(BibItem bibItem) {
        List<ItemDetails> itemDetailsList = new ArrayList<>();
        for(Item item : bibItem.getItems()){
            ItemDetails itemDetails = new ItemDetails();
            itemDetails.setCgd(item.getCollectionGroupDesignation());
            itemDetails.setItemBarcode(item.getBarcode());
            itemDetails.setChronologyAndEnum((item.getVolumePartYear() != null) ? item.getVolumePartYear() : "");
            itemDetailsList.add(itemDetails);
        }
        return itemDetailsList;
    }

    private String setCGD(BibItem bibItem) {
        return (bibItem.getItems().size() == 1) ? bibItem.getItems().get(0).getCollectionGroupDesignation() : "";
    }

    public TitleMatchedReport titleMatchReportsExport(TitleMatchedReport titleMatchedReport) throws Exception {
            return getTitleMatchedReportsExport(titleMatchedReport);
    }

    public TitleMatchedReport titleMatchReportsExportS3(TitleMatchedReport titleMatchedReport) throws Exception {
            return titleMatchReportExportService.process(titleMatchedReport);
    }

    public TitleMatchedReport getTitleMatchedReportsExport(TitleMatchedReport titleMatchedReport) throws ParseException, SolrServerException, IOException {
        List<BibItem> bibItems = new ArrayList<>();
        SolrQuery query = appendSolrQueryForTitle(titleMatchedReport);
        query.setRows(Integer.MAX_VALUE);
        if (titleMatchedReport.getTitleMatch().equalsIgnoreCase(ScsbConstants.TITLE_MATCHED))
            query.setSort(ScsbConstants.MATCHING_IDENTIFIER, SolrQuery.ORDER.desc);
        else
            query.setSort(ScsbConstants.BIB_CREATED_DATE, SolrQuery.ORDER.desc);
        QueryResponse queryResponse = solrTemplate.getSolrClient().query(query);
        SolrDocumentList bibSolrDocumentList = queryResponse.getResults();
        setDataForBibItems(bibSolrDocumentList, bibItems);
        return setBibItemsExport(titleMatchedReport, bibItems);
    }

    public TitleMatchedReport getTitleMatchedReportsExportS3(TitleMatchedReport titleMatchedReport) throws ParseException, SolrServerException, IOException {
        List<BibItem> bibItems = new ArrayList<>();
        SolrQuery query = appendSolrQueryForTitle(titleMatchedReport);
        query.setRows(titleReportExportBibsLimitPerFile);
        query.setStart(titleMatchedReport.getPageNumber()*titleReportExportBibsLimitPerFile);
        if (titleMatchedReport.getTitleMatch().equalsIgnoreCase(ScsbConstants.TITLE_MATCHED))
            query.setSort(ScsbConstants.MATCHING_IDENTIFIER, SolrQuery.ORDER.desc);
        else
            query.setSort(ScsbConstants.BIB_CREATED_DATE, SolrQuery.ORDER.desc);

        QueryResponse queryResponse = solrTemplate.getSolrClient().query(query);
        titleMatchedReport.setTotalRecordsCount(queryResponse.getResults().getNumFound());
        int totalPagesCount = (int) Math.ceil((double) (titleMatchedReport.getTotalRecordsCount()) / (double) (titleReportExportBibsLimitPerFile));
        titleMatchedReport.setTotalPageCount(totalPagesCount);
        SolrDocumentList bibSolrDocumentList = queryResponse.getResults();
        setDataForBibItems(bibSolrDocumentList, bibItems);
        return setBibItemsExport(titleMatchedReport, bibItems);
    }

    private TitleMatchedReport setBibItemsExport(TitleMatchedReport titleMatchedReport, List<BibItem> bibItems) {
        List<MatchingScoreTranslationEntity> msList = (List<MatchingScoreTranslationEntity>) matchingScoreTranslationRepository.findAll();
        Map<Integer, String> msMap = prepareMSMap(msList);
        List<TitleMatchedReports> titleMatchedReportsList = new ArrayList<>();
        for (BibItem bibItem : bibItems) {
            titleMatchedReportsList.addAll(setDataToTitleMatchReports(bibItem,msMap));
        }
        titleMatchedReport.setTitleMatchedReports(titleMatchedReportsList);
        return titleMatchedReport;
    }

    private SolrQuery appendSolrQueryForTitle(TitleMatchedReport titleMatchedReport) throws ParseException {
        String solrFormattedDate = getSolrFormattedDates(convertDateToString(titleMatchedReport.getFromDate()), convertDateToString(titleMatchedReport.getToDate()));
        String matchingIdentifier = (titleMatchedReport.getTitleMatch().equals(ScsbConstants.TITLE_MATCHED)) ? "" : "-";
        return solrQueryBuilder.buildQueryTitleMatchedReport(solrFormattedDate, titleMatchedReport.getOwningInst(), titleMatchedReport.getCgd(), matchingIdentifier,titleMatchedReport.getTitleMatch());
    }
    private  void setDataForBibItems( SolrDocumentList bibSolrDocumentList,List<BibItem> bibItems){
        if (CollectionUtils.isNotEmpty(bibSolrDocumentList)) {
            for (SolrDocument bibSolrDocument : bibSolrDocumentList) {
                BibItem bibItem = new BibItem();
                bibSolrDocumentRepository.populateBibItem(bibSolrDocument, bibItem);
                bibSolrDocumentRepository.populateItemHoldingsInfo(bibItem, false, ScsbCommonConstants.COMPLETE_STATUS);
                bibItems.add(bibItem);
            }
        }
    }
}
