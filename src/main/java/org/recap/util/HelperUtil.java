package org.recap.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.jpa.ItemChangeLogEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.jpa.ItemChangeLogDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sheiks on 19/07/17.
 */
@Slf4j
@Service
public class HelperUtil {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    private ObjectMapper objectMapper;

    /**
     * This method is used to save the ReportEntity in the database.
     * @param owningInstitution
     * @param fileName
     * @param reportType
     * @param reportDataEntityList
     */
    @Transactional
    public void saveReportEntity(String owningInstitution, String fileName, String reportType, List<ReportDataEntity> reportDataEntityList) {
        ReportEntity reportEntity;
        reportEntity = getReportEntity(owningInstitution!=null ? owningInstitution : ScsbConstants.UNKNOWN_INSTITUTION, fileName, reportType);
        reportEntity.setReportDataEntities(reportDataEntityList);
        producerTemplate.sendBody(ScsbCommonConstants.REPORT_Q, reportEntity);
    }


    private ReportEntity getReportEntity(String owningInstitution, String fileName, String reportType){
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setFileName(fileName);
        reportEntity.setType(reportType);
        reportEntity.setInstitutionName(owningInstitution);
        reportEntity.setCreatedDate(new Date());
        return reportEntity;
    }



    public void saveItemChangeLogEntity(String operationType, String message, List<ItemEntity> itemEntityList) {
        List<ItemChangeLogEntity> itemChangeLogEntityList = new ArrayList<>();
        for (ItemEntity itemEntity:itemEntityList) {
            ItemChangeLogEntity itemChangeLogEntity = new ItemChangeLogEntity();
            itemChangeLogEntity.setOperationType(operationType);
            itemChangeLogEntity.setUpdatedBy(operationType);
            itemChangeLogEntity.setUpdatedDate(new Date());
            itemChangeLogEntity.setRecordId(itemEntity.getId());
            itemChangeLogEntity.setNotes(message);
            itemChangeLogEntityList.add(itemChangeLogEntity);
        }
        itemChangeLogDetailsRepository.saveAll(itemChangeLogEntityList);
    }

    public String getJsonString(Object object) {
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
            return e.getMessage();
        }
    }

    public ObjectMapper getObjectMapper() {
        if(null == objectMapper) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }

}
