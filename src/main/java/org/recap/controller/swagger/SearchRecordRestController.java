package org.recap.controller.swagger;


import lombok.extern.slf4j.Slf4j;
import org.recap.ScsbCommonConstants;
import org.recap.model.search.DataDumpSearchResult;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.model.search.SearchRecordsResponse;
import org.recap.model.search.SearchResultRow;
import org.recap.util.PropertyUtil;
import org.recap.util.SearchRecordsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sudhish on 13/10/16.
 */
@Slf4j
@RestController
@RequestMapping("/searchService")
public class SearchRecordRestController {



    @Autowired
    private SearchRecordsUtil searchRecordsUtil=new SearchRecordsUtil();

    @Autowired
    PropertyUtil propertyUtil;

    /**
     * Gets SearchRecordsUtil object.
     *
     * @return the SearchRecordsUtil object.
     */
    public SearchRecordsUtil getSearchRecordsUtil() {
        return searchRecordsUtil;
    }

    /**
     * This method searches books based on the given search records request parameter and returns a list of search result row.
     *
     * @param searchRecordsRequest the search records request
     * @return the SearchRecordsResponse.
     */

    @GetMapping("/search")
    @ResponseBody
    public SearchRecordsResponse searchRecordsServiceGetParam(@RequestBody SearchRecordsRequest searchRecordsRequest) {

        SearchRecordsResponse searchRecordsResponse = new SearchRecordsResponse();
        if(ScsbCommonConstants.CUSTOMER_CODE.equalsIgnoreCase(searchRecordsRequest.getFieldName())){
            searchRecordsRequest.setFieldValue(searchRecordsRequest.getFieldValue().toUpperCase());
        }
        try {
            List<SearchResultRow> searchResultRows = searchRecordsUtil.searchRecords(searchRecordsRequest);
            searchRecordsResponse.setSearchResultRows(searchResultRows);
            searchRecordsResponse.setTotalBibRecordsCount(searchRecordsRequest.getTotalBibRecordsCount());
            searchRecordsResponse.setTotalItemRecordsCount(searchRecordsRequest.getTotalItemRecordsCount());
            searchRecordsResponse.setTotalRecordsCount(searchRecordsRequest.getTotalRecordsCount());
            searchRecordsResponse.setTotalPageCount(searchRecordsRequest.getTotalPageCount());
            searchRecordsResponse.setShowTotalCount(searchRecordsRequest.isShowTotalCount());
            searchRecordsResponse.setErrorMessage(searchRecordsRequest.getErrorMessage());
            searchRecordsResponse.setPageNumber(searchRecordsRequest.getPageNumber());
        } catch (Exception e) {
            log.info(ScsbCommonConstants.LOG_ERROR,e);
            searchRecordsResponse.setErrorMessage(e.getMessage());
        }
        return searchRecordsResponse;
    }

    /**
     * This method searches books based on the given search records request parameter and returns a list of DataDumpSearchResult which contains only bib ids and their corresponding item ids.
     *
     * @param searchRecordsRequest the search records request
     * @return the responseMap.
     */
    @GetMapping("/searchRecords")
    public Map searchRecords(
            @RequestBody SearchRecordsRequest searchRecordsRequest) {
        List<DataDumpSearchResult> dataDumpSearchResults = null;
        Map responseMap = new HashMap();
        try {
            dataDumpSearchResults = getSearchRecordsUtil().searchRecordsForDataDump(searchRecordsRequest);
            responseMap.put("totalPageCount", searchRecordsRequest.getTotalPageCount());
            responseMap.put("totalRecordsCount", searchRecordsRequest.getTotalRecordsCount());
            responseMap.put("dataDumpSearchResults", dataDumpSearchResults);
        } catch (Exception e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return responseMap;
    }


    /**
     * This method searches books based on the given search parameters and returns a list of search result row.
     *
     * @param fieldValue                  the field value
     * @param fieldName                   the field name
     * @param owningInstitutions          the owning institutions
     * @param collectionGroupDesignations the collection group designations
     * @param availability                the availability
     * @param materialTypes               the material types
     * @param useRestrictions             the use restrictions
     * @param pageSize                    the page size
     * @return the SearchResultRow list.
     */

    @GetMapping("/searchByParam")
    public List<SearchResultRow> searchRecordsServiceGet(
            @RequestParam(name="fieldValue", required = false)  String fieldValue,
            @RequestParam(name="fieldName", value = "fieldName" , required = false)  String fieldName,
            @RequestParam(name="owningInstitutions",required = false ) String owningInstitutions,
            @RequestParam(name="collectionGroupDesignations", value = "collectionGroupDesignations" , required = false)  String collectionGroupDesignations,
            @RequestParam(name="availability", value = "availability" , required = false)  String availability,
            @RequestParam(name="materialTypes", value = "materialTypes" , required = false)  String materialTypes,
            @RequestParam(name="useRestrictions", value = "useRestrictions" , required = false)  String useRestrictions,
            @RequestParam(name="pageSize", required = false) Integer pageSize
    ) {

        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest(propertyUtil.getAllInstitutions());
        if (fieldValue !=null) {
            searchRecordsRequest.setFieldValue(fieldValue);
        }
        if (fieldName !=null) {
            searchRecordsRequest.setFieldName(fieldName);
        }
        if(owningInstitutions !=null && owningInstitutions.trim().length()>0) {
            searchRecordsRequest.setOwningInstitutions(Arrays.asList(owningInstitutions.split(",")));
        }
        if(collectionGroupDesignations !=null && collectionGroupDesignations.trim().length()>0) {
            searchRecordsRequest.setCollectionGroupDesignations(Arrays.asList(collectionGroupDesignations.split(",")));
        }
        if(availability !=null && availability.trim().length()>0) {
            searchRecordsRequest.setAvailability(Arrays.asList(availability.split(",")));
        }
        if(materialTypes !=null && materialTypes.trim().length()>0) {
            searchRecordsRequest.setMaterialTypes(Arrays.asList(materialTypes.split(",")));
        }
        if(pageSize !=null) {
            searchRecordsRequest.setPageSize(pageSize);
        }
        List<SearchResultRow> searchResultRows = null;
        try {
            searchResultRows = searchRecordsUtil.searchRecords(searchRecordsRequest);
        } catch (Exception e) {
            searchResultRows = new ArrayList<>();
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return searchResultRows;
    }
}
