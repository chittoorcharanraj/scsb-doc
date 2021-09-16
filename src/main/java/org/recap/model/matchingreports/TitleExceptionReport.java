package org.recap.model.matchingreports;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by angelind on 16/6/17.
 */
@Data
public class TitleExceptionReport implements Serializable {
    private String owningInstitution;
    private String bibId;
    private String owningInstitutionBibId;
    private String materialType;
    private String oclc;
    private String isbn;
    private String issn;
    private String lccn;
    private String matchScore;
    private List<String> titleList;
}
