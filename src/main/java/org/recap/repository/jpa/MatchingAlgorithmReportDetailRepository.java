package org.recap.repository.jpa;

import org.recap.model.jpa.MatchingAlgorithmReportEntity;
import org.recap.model.jpa.ReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by rmaheswaran on 12/8/21
 */
public interface MatchingAlgorithmReportDetailRepository extends BaseRepository<MatchingAlgorithmReportEntity> {

    /**
     * Finds a list of report entities based on the given list of record numbers.
     *
     * @param recordNumbers the record numbers
     * @return the list
     */
    List<MatchingAlgorithmReportEntity> findByIdIn(List<Integer> recordNumbers);

    /**
     * Gets the count of record number based on the given list of types.
     *
     * @param typeList the type list
     * @return the count by type
     */
    @Query(value = "SELECT COUNT(id) FROM ReportEntity WHERE TYPE IN (?1)")
    Integer getCountByType(List<String> typeList);

    /**
     * Gets the count of record number based on the given list of type,file name and date range.
     *
     * @param typeList the type list
     * @param fileName the file name
     * @param from     the from
     * @param to       the to
     * @return the count by type and file name and date range
     */
    @Query(value = "SELECT COUNT(id) FROM ReportEntity WHERE type IN (?1) AND fileName = ?2 AND createdDate between ?3 and ?4")
    Integer getCountByTypeAndFileNameAndDateRange(List<String> typeList, String fileName, Date from, Date to);

    /**
     * Gets record number for the given list of types.
     *
     * @param pageable the pageable
     * @param typeList the type list
     * @return the record num by type
     */
    @Query(value = "SELECT id FROM ReportEntity WHERE TYPE IN (?1)")
    Page<Integer> getRecordNumByType(Pageable pageable,List<String> typeList);

    /**
     * Gets record number based on the given type ,file name and date range.
     *
     * @param pageable the pageable
     * @param typeList the type list
     * @param fileName the file name
     * @param from     the from
     * @param to       the to
     * @return the record num by type and file name and date range
     */
    @Query(value = "SELECT id from ReportEntity WHERE type IN (?1) AND fileName = ?2 AND createdDate between ?3 and ?4")
    Page<Integer> getRecordNumByTypeAndFileNameAndDateRange(Pageable pageable, List<String> typeList, String fileName, Date from, Date to);

    /**
     * Finds list of report entities based on the given institution,file name,report type and date range.
     *
     * @param fileName        the file name
     * @param institutionName the institution name
     * @param type            the type
     * @param from            the from
     * @param to              the to
     * @return the list
     */
    @Query(value = "select * from ma_report_t where FILE_NAME like %?1% and INSTITUTION_NAME=?2 and TYPE=?3 and CREATED_DATE >= ?4 and CREATED_DATE <= ?5", nativeQuery = true)
    List<ReportEntity> findByFileLikeAndInstitutionAndTypeAndDateRange(String fileName, String institutionName, String type, Date from, Date to);

    /**
     * Finds a list of report entities based on the given filename ,institution and date range.
     *
     * @param fileName        the file name
     * @param institutionName the institution name
     * @param from            the from
     * @param to              the to
     * @return the list
     */
    @Query(value = "select * from ma_report_t where FILE_NAME like %?1% and INSTITUTION_NAME=?2 and CREATED_DATE >= ?3 and CREATED_DATE <= ?4", nativeQuery = true)
    List<ReportEntity> findByFileNameLikeAndInstitutionAndDateRange(String fileName, String institutionName, Date from, Date to);

}
