package org.recap.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.csv.SubmitCollectionReportRecord;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hemalathas on 20/12/16.
 */
@Slf4j
public class SubmitCollectionReportGenerator {

    /**
     * This method prepares submit collection rejection report based on the given report entity.
     *
     * @param reportEntity the report entity
     * @return the SubmitCollectionReportRecord list
     */
    public List<SubmitCollectionReportRecord> prepareSubmitCollectionRejectionRecord(ReportEntity reportEntity) {

        List<SubmitCollectionReportRecord> submitCollectionReportRecordList = new ArrayList<>();
        List<ReportDataEntity> reportDataEntities = reportEntity.getReportDataEntities();
        SubmitCollectionReportRecord submitCollectionReportRecord = null;
        for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
            if(null == submitCollectionReportRecord || isReportRecordFullyUpdated(submitCollectionReportRecord)){
                submitCollectionReportRecord = new SubmitCollectionReportRecord();
                submitCollectionReportRecord.setReportType(getReportType(reportEntity.getType()));
            }
            ReportDataEntity report =  iterator.next();
            String headerValue = report.getHeaderValue();
            String headerName = report.getHeaderName();
            Method setterMethod = getSetterMethod(headerName);
            if(null != setterMethod){
                try {
                    setterMethod.invoke(submitCollectionReportRecord, headerValue);
                } catch (Exception e) {
                    log.error(ScsbCommonConstants.LOG_ERROR,e);
                }
                if(isReportRecordFullyUpdated(submitCollectionReportRecord) ) {
                    submitCollectionReportRecordList.add(submitCollectionReportRecord);
                }
            }
        }
        return submitCollectionReportRecordList;
    }
    /**
     * This method prepares submit collection rejection report based on the given report entity.
     *
     * @param reportEntity the report entity
     * @return the SubmitCollectionReportRecord list
     */
    public List<SubmitCollectionReportRecord> prepareAcessuibExceptionRecord(ReportEntity reportEntity) {
        List<SubmitCollectionReportRecord> submitCollectionReportRecordList = new ArrayList<>();
        List<ReportDataEntity> reportDataEntities = reportEntity.getReportDataEntities();
        SubmitCollectionReportRecord submitCollectionReportRecord = null;
        for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
            if(null == submitCollectionReportRecord || isReportRecordFullyUpdatedAcession(submitCollectionReportRecord)){
                submitCollectionReportRecord = new SubmitCollectionReportRecord();
                submitCollectionReportRecord.setReportType(getReportType(reportEntity.getType()));
            }
            ReportDataEntity report =  iterator.next();
            String headerValue = report.getHeaderValue();
            String headerName = report.getHeaderName();
            Method setterMethod = getSetterMethod(headerName);
            if(null != setterMethod){
                try {
                    setterMethod.invoke(submitCollectionReportRecord, headerValue);
                } catch (Exception e) {
                    log.error(ScsbCommonConstants.LOG_ERROR,e);
                }
                if(isReportRecordFullyUpdatedAcession(submitCollectionReportRecord) ) {
                    submitCollectionReportRecordList.add(submitCollectionReportRecord);
                }
            }
        }
        return submitCollectionReportRecordList;
    }
    private boolean isReportRecordFullyUpdated(SubmitCollectionReportRecord submitCollectionReportRecord){
        boolean newReportObject;
        if (null == submitCollectionReportRecord) {
            return false;
        }
        newReportObject = (null != submitCollectionReportRecord.getCustomerCode());
        newReportObject &= (null != submitCollectionReportRecord.getItemBarcode());
        newReportObject &= (null != submitCollectionReportRecord.getOwningInstitution());
        newReportObject &= (null != submitCollectionReportRecord.getMessage());
        return newReportObject;
    }
    private boolean isReportRecordFullyUpdatedAcession(SubmitCollectionReportRecord submitCollectionReportRecord){
        boolean newReportObject;
        if (null == submitCollectionReportRecord) {
            return false;
        }
        newReportObject = (null != submitCollectionReportRecord.getCustomerCode());
        newReportObject &= (null != submitCollectionReportRecord.getItemBarcode());
        newReportObject &= (null != submitCollectionReportRecord.getMessage());
        return newReportObject;
    }
    private String getReportType(String type) {
        switch (type) {
            case ScsbCommonConstants.SUBMIT_COLLECTION_SUCCESS_REPORT:
                return ScsbCommonConstants.SUCCESS;
            case ScsbCommonConstants.SUBMIT_COLLECTION_FAILURE_REPORT:
                return ScsbCommonConstants.FAIL;
            case ScsbCommonConstants.SUBMIT_COLLECTION_EXCEPTION_REPORT:
                return ScsbConstants.EXCEPTION_TEXT;
            case ScsbCommonConstants.SUBMIT_COLLECTION_REJECTION_REPORT:
                return ScsbConstants.REJECTION;
            default:
                return null;
        }
    }

    /**
     * This method is used to get the setter method for the given one of the instance variable name in SubmitCollectionReportRecord class.
     *
     * @param propertyName the property name
     * @return the setter method
     */
    public Method getSetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            return propertyUtilsBean.getWriteMethod(new PropertyDescriptor(propertyName, SubmitCollectionReportRecord.class));
        } catch (IntrospectionException e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return null;
    }

}
