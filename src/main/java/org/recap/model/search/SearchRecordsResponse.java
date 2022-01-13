package org.recap.model.search;

import lombok.Data;
import lombok.EqualsAndHashCode;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajeshbabuk on 2/1/17.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class SearchRecordsResponse extends SearchRecordsCommonResponse {
    private List<SearchResultRow> searchResultRows = new ArrayList<>();
 }
