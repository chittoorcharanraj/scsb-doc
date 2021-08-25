package org.recap.repository.jpa;

import org.recap.model.jpa.MatchingAlgorithmReportDataEntity;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by rmaheswaran on 12/8/21.
 */
public interface MatchingAlgorithmReportDataDetailsRepository extends BaseRepository<MatchingAlgorithmReportDataEntity> {

    /**
     * Gets count of record num for matching monograph based on the given header name.
     *
     * @param headerName the header name
     * @return the count of record num for matching monograph
     */
    @Query(value = "select count(*) from ma_report_data_t where " +
            "record_num in (select distinct RECORD_NUM from ma_report_data_t " +
            "where HEADER_NAME = 'MaterialType' and HEADER_VALUE like 'Monograph,%' " +
            "and RECORD_NUM in (select record_num from ma_report_t where type in ('SingleMatch','MultiMatch') and file_name not in ('PendingBibMatches'))) and header_name=?1", nativeQuery = true)
    long getCountOfRecordNumForMatchingMonograph(String headerName);

    /**
     * Gets count of record num for matching serials.
     *
     * @param headerName the header name
     * @return the count of record num for matching serials
     */
    @Query(value = "select count(*) from ma_report_data_t where " +
            "record_num in (select distinct RECORD_NUM from ma_report_data_t " +
            "where HEADER_NAME = 'MaterialType' and HEADER_VALUE like 'Serial,%' " +
            "and RECORD_NUM in (select record_num from ma_report_t where type in ('SingleMatch','MultiMatch'))) and header_name=?1", nativeQuery = true)
    long getCountOfRecordNumForMatchingSerials(String headerName);

    /**
     * Gets count of record num for matching mv ms.
     *
     * @param headerName the header name
     * @return the count of record num for matching mv ms
     */
    @Query(value = "select count(*) from ma_report_data_t where " +
            "record_num in (select distinct RECORD_NUM from ma_report_data_t " +
            "where HEADER_NAME = 'MaterialType' and HEADER_VALUE like 'MonographicSet,%' " +
            "and RECORD_NUM in (select record_num from ma_report_t where type in ('SingleMatch','MultiMatch'))) and header_name=?1", nativeQuery = true)
    long getCountOfRecordNumForMatchingMVMs(String headerName);

    /**
     * Gets count of record num for matching pending monograph.
     *
     * @param headerName the header name
     * @return the count of record num for matching pending monograph
     */
    @Query(value = "select count(*) from ma_report_data_t where " +
            "record_num in (select distinct RECORD_NUM from ma_report_data_t " +
            "where HEADER_NAME = 'MaterialType' and HEADER_VALUE like 'Monograph,%' " +
            "and RECORD_NUM in (select record_num from ma_report_t where type in ('SingleMatch','MultiMatch') and file_name in ('PendingBibMatches'))) and header_name=?1", nativeQuery = true)
    long getCountOfRecordNumForMatchingPendingMonograph(String headerName);

    /**
     * Gets a list of report data entities for matching monographs based on the given header name and limit values.
     *
     * @param headerName the header name
     * @param from       the from
     * @param batchsize  the batchsize
     * @return the report data entity for matching monographs
     */
    @Query(value = "select * from ma_report_data_t where record_num in (select distinct RECORD_NUM from ma_report_data_t " +
            "where HEADER_NAME = 'MaterialType' and HEADER_VALUE like 'Monograph,%' " +
            "and RECORD_NUM in (select record_num from ma_report_t where type in ('SingleMatch','MultiMatch') and file_name not in ('PendingBibMatches'))) " +
            "and header_name=?1 order by record_num limit ?2,?3", nativeQuery = true)
    List<MatchingAlgorithmReportDataEntity> getReportDataEntityForMatchingMonographs(String headerName, long from, long batchsize);

    /**
     * Gets a list of report data entities for matching monographs based on the given header names and limit values.
     *
     * @param headerNames the header names
     * @param from       the from
     * @param batchsize  the batchsize
     * @return the report data entity for matching monographs
     */
    @Query(value = "select * from ma_report_data_t where record_num in (select distinct RECORD_NUM from ma_report_data_t " +
            "where HEADER_NAME = 'MaterialType' and HEADER_VALUE like 'Monograph,%' " +
            "and RECORD_NUM in (select record_num from ma_report_t where type in ('SingleMatch','MultiMatch') and file_name not in ('PendingBibMatches'))) " +
            "and header_name in ?1 order by record_num limit ?2,?3", nativeQuery = true)
    List<MatchingAlgorithmReportDataEntity> getReportDataEntityForMatchingMonographs(List<String> headerNames, long from, long batchsize);


    /**
     * Gets report data entity for pending matching monographs.
     *
     * @param headerName the header name
     * @param from       the from
     * @param batchsize  the batchsize
     * @return the report data entity for pending matching monographs
     */
    @Query(value = "select * from ma_report_data_t where record_num in (select distinct RECORD_NUM from ma_report_data_t " +
            "where HEADER_NAME = 'MaterialType' and HEADER_VALUE like 'Monograph,%' " +
            "and RECORD_NUM in (select record_num from ma_report_t where type in ('SingleMatch','MultiMatch') and file_name in ('PendingBibMatches'))) " +
            "and header_name=?1 order by record_num limit ?2,?3", nativeQuery = true)
    List<MatchingAlgorithmReportDataEntity> getReportDataEntityForPendingMatchingMonographs(String headerName, long from, long batchsize);

    /**
     * Gets report data entity for pending matching monographs.
     *
     * @param headerNames the header names
     * @param from       the from
     * @param batchsize  the batchsize
     * @return the report data entity for pending matching monographs
     */
    @Query(value = "select * from ma_report_data_t where record_num in (select distinct RECORD_NUM from ma_report_data_t " +
            "where HEADER_NAME = 'MaterialType' and HEADER_VALUE like 'Monograph,%' " +
            "and RECORD_NUM in (select record_num from ma_report_t where type in ('SingleMatch','MultiMatch') and file_name in ('PendingBibMatches'))) " +
            "and header_name in ?1 order by record_num limit ?2,?3", nativeQuery = true)
    List<MatchingAlgorithmReportDataEntity> getReportDataEntityForPendingMatchingMonographs(List<String> headerNames, long from, long batchsize);


    /**
     * Gets a list of report data entities based on the given list of record nums and header name.
     *
     * @param recordNum  the record num
     * @param headerName the header name
     * @return the report data entity by record num in
     */
    @Query(value = "select * from ma_report_data_t where record_num in (?1) and HEADER_NAME=?2", nativeQuery = true)
    List<MatchingAlgorithmReportDataEntity> getReportDataEntityByRecordNumIn(List<Integer> recordNum, String headerName);

    /**
     * Gets a list of report data entities based on the given list of record nums and list of header names.
     *
     * @param recordNumList  the record num list
     * @param headerNameList the header name list
     * @return the records for matching bib info
     */
    @Query(value = "SELECT RDE FROM ReportDataEntity RDE WHERE recordNum IN (?1) AND headerName IN (?2)")
    List<MatchingAlgorithmReportDataEntity> getRecordsForMatchingBibInfo(List<String> recordNumList,List<String> headerNameList);

    /**
     * Gets a list of report data entities for matching serials based on the given header name and limit values.
     *
     * @param headerName the header name
     * @param from       the from
     * @param batchsize  the batchsize
     * @return the report data entity for matching serials
     */
    @Query(value = "select * from ma_report_data_t where record_num in (select distinct RECORD_NUM from ma_report_data_t " +
            "where HEADER_NAME = 'MaterialType' and HEADER_VALUE like 'Serial,%' " +
            "and RECORD_NUM in (select record_num from ma_report_t where type in ('SingleMatch','MultiMatch'))) " +
            "and header_name=?1 order by record_num limit ?2,?3", nativeQuery = true)
    List<MatchingAlgorithmReportDataEntity> getReportDataEntityForMatchingSerials(String headerName, long from, long batchsize);

    /**
     * Gets a list of report data entities for matching serials based on the given header name and limit values.
     *
     * @param headerNames the header names
     * @param from       the from
     * @param batchsize  the batchsize
     * @return the report data entity for matching serials
     */
    @Query(value = "select * from ma_report_data_t where record_num in (select distinct RECORD_NUM from ma_report_data_t " +
            "where HEADER_NAME = 'MaterialType' and HEADER_VALUE like 'Serial,%' " +
            "and RECORD_NUM in (select record_num from ma_report_t where type in ('SingleMatch','MultiMatch'))) " +
            "and header_name in ?1 order by record_num limit ?2,?3", nativeQuery = true)
    List<MatchingAlgorithmReportDataEntity> getReportDataEntityForMatchingSerials(List<String> headerNames, long from, long batchsize);

    /**
     * Gets a list of report data entities for matching monographicSet based on the given header name and limit values.
     *
     * @param headerName the header name
     * @param from       the from
     * @param batchsize  the batchsize
     * @return the report data entity for matching monographicSet
     */
    @Query(value = "select * from ma_report_data_t where record_num in (select distinct RECORD_NUM from ma_report_data_t " +
            "where HEADER_NAME = 'MaterialType' and HEADER_VALUE like 'MonographicSet,%'" +
            "and RECORD_NUM in (select record_num from ma_report_t where type in ('SingleMatch','MultiMatch'))) " +
            "and header_name=?1 order by record_num limit ?2,?3", nativeQuery = true)
    List<MatchingAlgorithmReportDataEntity> getReportDataEntityForMatchingMVMs(String headerName, long from, long batchsize);

    /**
     * Gets a list of report data entities for matching monographicSet based on the given header names and limit values.
     *
     * @param headerNames the header names
     * @param from       the from
     * @param batchsize  the batchsize
     * @return the report data entity for matching monographicSet
     */
    @Query(value = "select * from ma_report_data_t where record_num in (select distinct RECORD_NUM from ma_report_data_t " +
            "where HEADER_NAME = 'MaterialType' and HEADER_VALUE like 'MonographicSet,%'" +
            "and RECORD_NUM in (select record_num from ma_report_t where type in ('SingleMatch','MultiMatch'))) " +
            "and header_name in ?1 order by record_num limit ?2,?3", nativeQuery = true)
    List<MatchingAlgorithmReportDataEntity> getReportDataEntityForMatchingMVMs(List<String> headerNames, long from, long batchsize);

    @Query(value = "select * from ma_report_data_t\n" +
            "where RECORD_NUM in \n" +
            "(select RECORD_NUM from ma_report_data_t where record_num in \n" +
            "(select record_num from ma_report_t where date(CREATED_DATE)=?1 and type like %?2%) and HEADER_VALUE like %?3%) and HEADER_NAME in ('BibId','MatchScore') and HEADER_VALUE like %?4% ;", nativeQuery = true)
    List<MatchingAlgorithmReportDataEntity> getReportDataEntityForSingleMatch(String date,String type,String matchTypeValue,String bibIds);
}

