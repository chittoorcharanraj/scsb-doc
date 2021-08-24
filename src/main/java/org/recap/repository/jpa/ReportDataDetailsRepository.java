package org.recap.repository.jpa;

import org.recap.model.jpa.ReportDataEntity;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by angelind on 9/1/17.
 */
public interface ReportDataDetailsRepository extends BaseRepository<ReportDataEntity> {

    /**
     * Gets a list of report data entities based on the given list of record nums and header name.
     *
     * @param recordNum  the record num
     * @param headerName the header name
     * @return the report data entity by record num in
     */
    @Query(value = "select * from report_data_t where record_num in (?1) and HEADER_NAME=?2", nativeQuery = true)
    List<ReportDataEntity> getReportDataEntityByRecordNumIn(List<Integer> recordNum, String headerName);

    /**
     * Gets a list of report data entities based on the given list of record nums and list of header names.
     *
     * @param recordNumList  the record num list
     * @param headerNameList the header name list
     * @return the records for matching bib info
     */
    @Query(value = "SELECT RDE FROM ReportDataEntity RDE WHERE recordNum IN (?1) AND headerName IN (?2)")
    List<ReportDataEntity> getRecordsForMatchingBibInfo(List<String> recordNumList,List<String> headerNameList);

}

