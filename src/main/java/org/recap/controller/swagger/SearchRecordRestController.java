package org.recap.controller.swagger;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.recap.ScsbCommonConstants;
import org.recap.model.search.DataDumpSearchResult;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.model.search.SearchRecordsResponse;
import org.recap.model.search.SearchResultRow;
import org.recap.util.PropertyUtil;
import org.recap.util.SearchRecordsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
@RestController
@RequestMapping("/searchService")
@Api(value="search")
public class SearchRecordRestController {

    private static final Logger logger = LoggerFactory.getLogger(SearchRecordRestController.class);

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
    @PostMapping(value="/search")
    @ApiOperation(value = "search",notes = "Search Books in Storage Location - Using Method Post, Request data is String", nickname = "search")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Search")})
    @ResponseBody
    public SearchRecordsResponse searchRecordsServiceGetParam(@ApiParam(value = "Paramerters for Searching Records" , required = true, name="requestJson") @RequestBody SearchRecordsRequest searchRecordsRequest) {

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
            logger.info(ScsbCommonConstants.LOG_ERROR,e);
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
    @PostMapping(value="/searchRecords")
    @ApiOperation(value = "searchRecords",notes = "Search Books in Storage Location - Using Method Post, Request data is String", nickname = "searchRecords", consumes="application/json")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Search")})
    public Map searchRecords(@ApiParam(value = "Paramerters for Searching Records" , required = true, name="requestJson") @RequestBody SearchRecordsRequest searchRecordsRequest) {
        List<DataDumpSearchResult> dataDumpSearchResults = null;
        Map responseMap = new HashMap();
        try {
            dataDumpSearchResults = getSearchRecordsUtil().searchRecordsForDataDump(searchRecordsRequest);
            responseMap.put("totalPageCount", searchRecordsRequest.getTotalPageCount());
            responseMap.put("totalRecordsCount", searchRecordsRequest.getTotalRecordsCount());
            responseMap.put("dataDumpSearchResults", dataDumpSearchResults);
        } catch (Exception e) {
            logger.error(ScsbCommonConstants.LOG_ERROR,e);
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
    @GetMapping(value="/searchByParam")
    @ApiOperation(value = "searchParam",notes = "Search Books in Storage Location - Using Method GET, Request data as parameter", nickname = "search")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Search")})
    public List<SearchResultRow> searchRecordsServiceGet(
            @RequestParam(name="fieldValue", required = false)  String fieldValue,
            @ApiParam(name="fieldName",required = false,allowableValues = "Author_search,Title_search,TitleStartsWith,Publisher,PublicationPlace,PublicationDate,Subject,ISBN,ISSN,OCLCNumber,Notes,CallNumber_search,Barcode") @RequestParam(name="fieldName", value = "fieldName" , required = false)  String fieldName,
            @ApiParam(name="owningInstitutions", value= "${swagger.values.owningInstitutions}")@RequestParam(name="owningInstitutions",required = false ) String owningInstitutions,
            @ApiParam(name="collectionGroupDesignations", value = "collection Designations : Shared,Private,Open") @RequestParam(name="collectionGroupDesignations", value = "collectionGroupDesignations" , required = false)  String collectionGroupDesignations,
            @ApiParam(name="availability", value = "Availability: Available, NotAvailable") @RequestParam(name="availability", value = "availability" , required = false)  String availability,
            @ApiParam(name="materialTypes", value = "MaterialTypes: Monograph, Serial, Other") @RequestParam(name="materialTypes", value = "materialTypes" , required = false)  String materialTypes,
            @ApiParam(name="useRestrictions", value = "Use Restrictions: NoRestrictions, InLibraryUse, SupervisedUse") @RequestParam(name="useRestrictions", value = "useRestrictions" , required = false)  String useRestrictions,
            @ApiParam(name="pageSize", value = "Page Size in Numbers - 10, 20 30...") @RequestParam(name="pageSize", required = false) Integer pageSize
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
            logger.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return searchResultRows;
    }
}
