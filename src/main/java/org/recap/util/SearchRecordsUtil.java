package org.recap.util;

import org.apache.commons.collections.CollectionUtils;
import org.recap.PropertyKeyConstants;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.search.DataDumpSearchResult;
import org.recap.model.search.SearchItemResultRow;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.model.search.SearchResultRow;
import org.recap.model.solr.BibItem;
import org.recap.model.solr.Holdings;
import org.recap.model.solr.Item;
import org.recap.repository.solr.main.BibSolrDocumentRepository;
import org.recap.repository.solr.main.DataDumpSolrDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sudhish on 14/10/16.
 */
@Service
public final class SearchRecordsUtil {

    @Autowired
    private BibSolrDocumentRepository bibSolrDocumentRepository;

    @Autowired
    private DataDumpSolrDocumentRepository dataDumpSolrDocumentRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    PropertyUtil propertyUtil;

    private String fieldValue;
    private String fieldName;

    /**
     * Gets DataDumpSolrDocumentRepository object.
     *
     * @return the DataDumpSolrDocumentRepository object.
     */
    public DataDumpSolrDocumentRepository getDataDumpSolrDocumentRepository() {
        return dataDumpSolrDocumentRepository;
    }

    /**
     * This method searches records in solr based on the given search records request and returns a list of SearchResultRow.
     *
     * @param searchRecordsRequest the SearchResultRow
     * @return the SearchResultRow list
     * @throws Exception the exception
     */
    public List<SearchResultRow> searchRecords(SearchRecordsRequest searchRecordsRequest) throws Exception {
        this.fieldValue = searchRecordsRequest.getFieldValue();
        this.fieldName = searchRecordsRequest.getFieldName();
        if (!isEmptySearch(searchRecordsRequest)) {
            if (CollectionUtils.isEmpty(searchRecordsRequest.getOwningInstitutions())) {
                searchRecordsRequest.setOwningInstitutions(commonUtil.findAllInstitutionCodesExceptSupportInstitution());
            }
            if(searchRecordsRequest.getFieldName().equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISBN) || searchRecordsRequest.getFieldName().equalsIgnoreCase(ScsbCommonConstants.MATCH_POINT_FIELD_ISSN)){
                searchRecordsRequest.setFieldValue(searchRecordsRequest.getFieldValue().replaceAll(ScsbConstants.NUMBER_PATTERN, ""));
            }
            modifySearchRequestForCirculationFreeze(searchRecordsRequest);
            if (CollectionUtils.isEmpty(searchRecordsRequest.getOwningInstitutions())) {
                return new ArrayList<>();
            }
            return searchAndBuildResults(searchRecordsRequest);
        }
        searchRecordsRequest.setErrorMessage(ScsbConstants.EMPTY_FACET_ERROR_MSG);
        return new ArrayList<>();
    }

    /**
     * This method checks if the search request is only for available items, exclude the circulation freeze enabled institution from the institutions list
     * as those institution items will be virtually unavailable.
     * @param searchRecordsRequest ths Search Records Request
     */
    public void modifySearchRequestForCirculationFreeze(SearchRecordsRequest searchRecordsRequest) {
        List<String> availabilitySearch = searchRecordsRequest.getAvailability();
        boolean isOnlyAvailableSearch = availabilitySearch.size() == 1 && availabilitySearch.get(0).equalsIgnoreCase(ScsbCommonConstants.AVAILABLE);
        if (isOnlyAvailableSearch) {
            Map<String, String> propertyMap = propertyUtil.getPropertyByKeyForAllInstitutions(PropertyKeyConstants.ILS.ILS_ENABLE_CIRCULATION_FREEZE);
            searchRecordsRequest.getOwningInstitutions().removeAll(propertyMap.entrySet().stream()
                    .filter(e -> Boolean.parseBoolean(e.getValue()))
                    .map(Map.Entry::getKey).collect(Collectors.toList()));
        }
    }

    /**
     * This method is used to search BibItems from the solr based on the given searchRecordsRequest and sets the reponse to SearchResultRow
     * and returns a list of SearchResultRow.
     *
     * @param searchRecordsRequest the SearchResultRow
     * @return the SearchResultRow list
     * @throws Exception the exception
     */
    public List<SearchResultRow> searchAndBuildResults(SearchRecordsRequest searchRecordsRequest) throws Exception{
        Map<String, Object> searchResponse = bibSolrDocumentRepository.search(searchRecordsRequest);
        String errorResponse = (String) searchResponse.get(ScsbCommonConstants.SEARCH_ERROR_RESPONSE);
        if(errorResponse != null) {
            searchRecordsRequest.setErrorMessage(ScsbConstants.SERVER_ERROR_MSG);
        } else {
            List<BibItem> bibItems = (List<BibItem>) searchResponse.get(ScsbCommonConstants.SEARCH_SUCCESS_RESPONSE);
            return buildGeneralResults(bibItems);
        }

        return new ArrayList<>();
    }

    /**
     * Search records for data dump in solr based on the given search records request and returns a list of DataDumpSearchResult.
     *
     * @param searchRecordsRequest the SearchResultRow
     * @return the DataDumpSearchResult list
     * @throws Exception the exception
     */
    public List<DataDumpSearchResult> searchRecordsForDataDump(SearchRecordsRequest searchRecordsRequest) throws Exception{
        if (!isEmptySearch(searchRecordsRequest)) {
            Map<String, Object> searchResponse = getDataDumpSolrDocumentRepository().search(searchRecordsRequest);
            String errorResponse = (String) searchResponse.get(ScsbCommonConstants.SEARCH_ERROR_RESPONSE);
            if(errorResponse != null) {
                searchRecordsRequest.setErrorMessage(ScsbConstants.SERVER_ERROR_MSG);
            } else {
                List<BibItem> bibItems = (List<BibItem>) searchResponse.get(ScsbCommonConstants.SEARCH_SUCCESS_RESPONSE);
                return buildResultsForDataDump(bibItems);
            }
        }
        searchRecordsRequest.setErrorMessage(ScsbConstants.EMPTY_FACET_ERROR_MSG);
        return new ArrayList<>();
    }

    /**
     * Builds search result rows from the bib and items.
     *
     * @param bibItems
     * @return
     */
    private List<SearchResultRow> buildGeneralResults(List<BibItem> bibItems) {
        List<SearchResultRow> searchResultRows = new ArrayList<>();
        Map<String, String> propertyMap = propertyUtil.getPropertyByKeyForAllInstitutions(PropertyKeyConstants.ILS.ILS_ENABLE_CIRCULATION_FREEZE);
        if (!CollectionUtils.isEmpty(bibItems)) {
            for (BibItem bibItem : bibItems) {
                String institutionCode = bibItem.getOwningInstitution();
                boolean isCirculationFreezeEnabled = Boolean.parseBoolean(propertyMap.get(institutionCode));
                SearchResultRow searchResultRow = new SearchResultRow();
                searchResultRow.setBibId(bibItem.getBibId());
                searchResultRow.setTitle(bibItem.getTitleDisplay());
                searchResultRow.setAuthor(bibItem.getAuthorDisplay());
                searchResultRow.setPublisher(bibItem.getPublisher());
                searchResultRow.setPublisherDate(bibItem.getPublicationDate());
                searchResultRow.setOwningInstitution(bibItem.getOwningInstitution());
                searchResultRow.setLeaderMaterialType(bibItem.getLeaderMaterialType());
                searchResultRow.setBibCreatedDate(bibItem.getBibCreatedDate());
                searchResultRow.setOwningInstitutionBibId(bibItem.getOwningInstitutionBibId());
                searchResultRow.setMatchingIdentifier(bibItem.getMatchingIdentifier());
                String authorSearch = CollectionUtils.isNotEmpty(bibItem.getAuthorSearch()) ? bibItem.getAuthorSearch().get(0) : " ";
                searchResultRow.setAuthorSearch(authorSearch);
                Holdings holdings = CollectionUtils.isEmpty(bibItem.getHoldingsList()) ? new Holdings() : bibItem.getHoldingsList().get(0);
                if (null != bibItem.getItems() && bibItem.getItems().size() == 1 && !ScsbCommonConstants.SERIAL.equals(bibItem.getLeaderMaterialType())) {
                    Item item = bibItem.getItems().get(0);
                    if (null != item) {
                        searchResultRow.setItemId(item.getItemId());
                        searchResultRow.setOwningInstitutionItemId(item.getOwningInstitutionItemId());
                        searchResultRow.setCustomerCode(item.getCustomerCode());
                        searchResultRow.setCollectionGroupDesignation(item.getCollectionGroupDesignation());
                        searchResultRow.setUseRestriction(item.getUseRestrictionDisplay());
                        searchResultRow.setBarcode(item.getBarcode());
                        searchResultRow.setAvailability(isCirculationFreezeEnabled ? ScsbCommonConstants.NOT_AVAILABLE : item.getAvailabilityDisplay());
                        searchResultRow.setSummaryHoldings(holdings.getSummaryHoldings());
                        searchResultRow.setOwningInstitutionHoldingsId(getMatchedOwningInstitutionHoldingsId(bibItem.getHoldingsList(), item.getHoldingsIdList()));
                        searchResultRow.setImsLocation(item.getImsLocation());
                    }
                } else {
                    if (!CollectionUtils.isEmpty(bibItem.getItems())) {
                        List<SearchItemResultRow> searchItemResultRows = new ArrayList<>();
                        Set<String> mixedStatus = new HashSet<>();
                        for (Item item : bibItem.getItems()) {
                            if (null != item) {
                                SearchItemResultRow searchItemResultRow = new SearchItemResultRow();
                                searchItemResultRow.setItemId(item.getItemId());
                                searchItemResultRow.setOwningInstitutionItemId(item.getOwningInstitutionItemId());
                                searchItemResultRow.setCallNumber(item.getCallNumberDisplay());
                                searchItemResultRow.setChronologyAndEnum(item.getVolumePartYear());
                                searchItemResultRow.setCustomerCode(item.getCustomerCode());
                                searchItemResultRow.setBarcode(item.getBarcode());
                                searchItemResultRow.setUseRestriction(item.getUseRestrictionDisplay());
                                searchItemResultRow.setCollectionGroupDesignation(item.getCollectionGroupDesignation());
                                searchItemResultRow.setAvailability(isCirculationFreezeEnabled ? ScsbCommonConstants.NOT_AVAILABLE : item.getAvailabilityDisplay());
                                searchItemResultRow.setOwningInstitutionHoldingsId(getMatchedOwningInstitutionHoldingsId(bibItem.getHoldingsList(), item.getHoldingsIdList()));
                                searchItemResultRow.setImsLocation(item.getImsLocation());

                                searchItemResultRow.setItemMatched(validateItemBarcode(item.getBarcode()));
                                searchItemResultRows.add(searchItemResultRow);
                                mixedStatus.add(isCirculationFreezeEnabled ? ScsbCommonConstants.NOT_AVAILABLE : item.getAvailabilityDisplay());
                                searchResultRow.setAvailability(isCirculationFreezeEnabled ? ScsbCommonConstants.NOT_AVAILABLE : item.getAvailabilityDisplay());
                                searchResultRow.setImsLocation(item.getImsLocation());
                            }
                        }
                        for (String status:mixedStatus){
                            if (ScsbCommonConstants.NOT_AVAILABLE.equals(status) && (mixedStatus.size()==2)){
                                searchResultRow.setAvailability(ScsbConstants.MIXED_STATUS);
                            }
                        }
                        searchResultRow.setSummaryHoldings(holdings.getSummaryHoldings());
                        searchResultRow.setShowItems(true);
                        Collections.sort(searchItemResultRows);
                        searchResultRow.setSearchItemResultRows(searchItemResultRows);
                    }
                }
                searchResultRows.add(searchResultRow);
            }
        }
        return searchResultRows;
    }

    /**
     *
     * @param barcode
     * @return Boolean
     */
    private boolean validateItemBarcode(String barcode) {
        return this.fieldName.equalsIgnoreCase(ScsbCommonConstants.BARCODE) ? this.fieldValue.contains(barcode) : ScsbConstants.BOOLEAN_FALSE;
    }

    /**
     * Returns matching owning institution holdings id for the passed holdings id.
     *
     * @param holdingsList
     * @param holdingsIdList
     * @return
     */
    private String getMatchedOwningInstitutionHoldingsId(List<Holdings> holdingsList, List<Integer> holdingsIdList) {
        String owningInstitutionHoldingsId = "";
        if (CollectionUtils.isNotEmpty(holdingsList)) {
            for (Holdings holdings : holdingsList) {
                if (CollectionUtils.isNotEmpty(holdingsIdList)) {
                    if (holdingsIdList.contains(holdings.getHoldingsId())) {
                        owningInstitutionHoldingsId = holdings.getOwningInstitutionHoldingsId();
                    }
                }
            }
        }
        return owningInstitutionHoldingsId;
    }

    /**
     * This method builds search results for data dump for the given list of bib items and returns a list of DataDumpSearchResult.
     *
     * @param bibItems the bibItems
     * @return the DataDumpSearchResult list
     */
    public List<DataDumpSearchResult> buildResultsForDataDump(List<BibItem> bibItems) {
        List<DataDumpSearchResult> dataDumpSearchResults = new ArrayList<>();
        if (!CollectionUtils.isEmpty(bibItems)) {
            for (BibItem bibItem : bibItems) {
                DataDumpSearchResult dataDumpSearchResult = new DataDumpSearchResult();
                dataDumpSearchResult.setBibId(bibItem.getBibId());
                if (!CollectionUtils.isEmpty(bibItem.getItems())) {
                    List<Integer> itemIds = new ArrayList<>();
                    for (Item item : bibItem.getItems()) {
                        if (null != item) {
                            itemIds.add(item.getItemId());
                        }
                    }
                    dataDumpSearchResult.setItemIds(itemIds);
                }
                dataDumpSearchResults.add(dataDumpSearchResult);
            }
        }
        return dataDumpSearchResults;
    }

    /**
     * Returns true if no field is specified in the search records request.
     *
     * @param searchRecordsRequest
     * @return
     */
    private boolean isEmptySearch(SearchRecordsRequest searchRecordsRequest) {
        boolean emptySearch = false;
        if (searchRecordsRequest.getMaterialTypes().isEmpty() && searchRecordsRequest.getAvailability().isEmpty() &&
                searchRecordsRequest.getCollectionGroupDesignations().isEmpty() && ((searchRecordsRequest.getOwningInstitutions().size() == 0) ? true : false) && searchRecordsRequest.getUseRestrictions().isEmpty() && searchRecordsRequest.getImsDepositoryCodes().isEmpty()) {
            emptySearch = true;
        } else if(!((CollectionUtils.isNotEmpty(searchRecordsRequest.getMaterialTypes()) || ((searchRecordsRequest.getOwningInstitutions().size() == 0)  ? false : true)) &&
                (CollectionUtils.isNotEmpty(searchRecordsRequest.getAvailability()) || CollectionUtils.isNotEmpty(searchRecordsRequest.getCollectionGroupDesignations())
                        || CollectionUtils.isNotEmpty(searchRecordsRequest.getUseRestrictions()) || CollectionUtils.isNotEmpty(searchRecordsRequest.getImsDepositoryCodes())))) {
            emptySearch = true;
        }
        return emptySearch;
    }
}
