package org.recap.model.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * Created by rajesh on 18-Jul-16.
 */
@Data
@ApiModel(value="SearchItemResultRow", description="Model for Displaying Item Result")
public class SearchItemResultRow extends AbstractSearchItemResultRow implements Comparable<SearchItemResultRow> {
    @ApiModelProperty(name= "itemId", value= "Item Id",position = 8)
    private Integer itemId;
    @ApiModelProperty(name= "owningInstitutionItemId", value= "Owning Institution Item Id",position = 9)
    private String owningInstitutionItemId;
    @ApiModelProperty(name= "owningInstitutionHoldingsId", value= "Owning Institution Holdings Id",position = 10)
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

