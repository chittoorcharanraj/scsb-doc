package org.recap.model.search;

import lombok.Data;


/**
 * Created by rajesh on 18-Jul-16.
 */
@Data
public class SearchItemResultRow extends AbstractSearchItemResultRow implements Comparable<SearchItemResultRow> {
    private Integer itemId;
    private String owningInstitutionItemId;
    private String owningInstitutionHoldingsId;

    @Override
    public int compareTo(SearchItemResultRow searchItemResultRow) {
        String objChronologyAndEnum=null;
        String searchItemResultRowChronologyAndEnum=null;
        if(this != null && this.getChronologyAndEnum() !=null){
            objChronologyAndEnum=this.getChronologyAndEnum();
        }
        if(searchItemResultRow != null && searchItemResultRow.getChronologyAndEnum() !=null){
            searchItemResultRowChronologyAndEnum=searchItemResultRow.getChronologyAndEnum();
        }
        if(objChronologyAndEnum == null && searchItemResultRowChronologyAndEnum == null){
            return 0;
        }
        else if(objChronologyAndEnum == null){
            return -1;
        }
        else if(searchItemResultRowChronologyAndEnum == null){
            return 1;
        }
        else {
            return objChronologyAndEnum.compareTo(searchItemResultRowChronologyAndEnum);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;

        SearchItemResultRow searchItemResultRow = (SearchItemResultRow) object;

        return getChronologyAndEnum() != null ? getChronologyAndEnum().equals(searchItemResultRow.getChronologyAndEnum()) : searchItemResultRow.getChronologyAndEnum() == null;

    }
    @Override
    public int hashCode() {
        return getChronologyAndEnum() != null ? getChronologyAndEnum().hashCode() : 0;
    }
}

