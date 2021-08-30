package org.recap.repository.jpa;

import org.recap.model.jpa.InstitutionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by hemalathas on 22/6/16.
 */
public interface InstitutionDetailsRepository extends BaseRepository<InstitutionEntity> {
    /**
     * Finds institution entity by using institution code.
     *
     * @param institutionCode the institution code
     * @return the institution entity
     */
    InstitutionEntity findByInstitutionCode(String institutionCode);

    /**
     * Finds institution entity by using institution name.
     *
     * @param institutionName the institution name
     * @return the institution entity
     */
    InstitutionEntity findByInstitutionName(String institutionName);

    /**
     * Finds institution entity which is not in the given list of institution code.
     *
     * @param institutionCodes list of institution codes
     * @return the list of institution entities
     */
    List<InstitutionEntity> findByInstitutionCodeNotIn(List<String> institutionCodes);

    /**
     * To get the list of institution codes except support institution.
     *
     * @return the institutions
     */
    @Query(value = "select INSTITUTION_CODE from institution_t where INSTITUTION_CODE != :supportInstitution", nativeQuery = true)
    List<String> findAllInstitutionCodesExceptSupportInstitution(@Param("supportInstitution") String supportInstitution);

    /**
     * To get the list of institution codes except support institution.
     *
     * @return the institutions
     */
    @Query(value = "select INSTITUTION_ID from institution_t where INSTITUTION_CODE != :supportInstitution", nativeQuery = true)
    List<Integer> findAllInstitutionIdsExceptSupportInstitution(@Param("supportInstitution") String supportInstitution);

    /**
     * To get the list of institution entities except support institution.
     *
     * @return the institutions
     */
    @Query(value = "select inst from InstitutionEntity inst  where inst.institutionCode not in (:supportInstitution) ORDER BY inst.id")
    List<InstitutionEntity> findAllInstitutionsExceptSupportInstitution(@Param("supportInstitution") String supportInstitution);
}
