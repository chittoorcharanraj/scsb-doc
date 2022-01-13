package org.recap.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.recap.ScsbCommonConstants;
import org.recap.ScsbConstants;
import org.recap.model.csv.OngoingAccessionReportRecord;
import org.recap.model.csv.SubmitCollectionReportRecord;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;


import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Created by premkb on 07/02/17.
 */
@Slf4j
public class OngoingAccessionReportGenerator {

    /**
     * This method prepares ongoing accession report record.
     *
     * @param reportEntity the report entity
     * @return the list
     */
    public OngoingAccessionReportRecord prepareOngoingAccessionReportRecord(ReportEntity reportEntity) {

        List<ReportDataEntity> reportDataEntities = reportEntity.getReportDataEntities();
        OngoingAccessionReportRecord ongoingAccessionReportRecord = new OngoingAccessionReportRecord();
        for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
            ReportDataEntity report =  iterator.next();
            String headerName = report.getHeaderName();
            if (!(StringUtils.equalsIgnoreCase(headerName, ScsbConstants.ACCESSION_SUMMARY) || StringUtils.equalsIgnoreCase(headerName, ScsbConstants.BULK_ACCESSION_SUMMARY))) {
                String headerValue = report.getHeaderValue();
                Method setterMethod = getSetterMethod(headerName);
                if(null != setterMethod){
                    try {
                        setterMethod.invoke(ongoingAccessionReportRecord, headerValue);
                    } catch (Exception e) {
                        log.error(ScsbConstants.EXCEPTION,e);
                    }
                }
            }
        }
        return ongoingAccessionReportRecord;
    }

    /**
     * This method gets setter method.
     *
     * @param propertyName the property name
     * @return the setter method
     */
    public Method getSetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            return propertyUtilsBean.getWriteMethod(new PropertyDescriptor(propertyName, OngoingAccessionReportRecord.class));
        } catch (IntrospectionException e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return null;
    }

    /**
     * This method gets getter method.
     *
     * @param propertyName the property name
     * @return the getter method
     */
    public Method getGetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            return propertyUtilsBean.getReadMethod(new PropertyDescriptor(propertyName, SubmitCollectionReportRecord.class));
        } catch (IntrospectionException e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return null;
    }
}
