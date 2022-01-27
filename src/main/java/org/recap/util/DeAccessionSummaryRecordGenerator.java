package org.recap.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.recap.ScsbCommonConstants;
import org.recap.model.csv.DeAccessionSummaryRecord;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;


import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenchulakshmig on 13/10/16.
 */
@Slf4j
public class DeAccessionSummaryRecordGenerator {

    /**
     * Prepare deaccession summary report record.
     *
     * @param reportEntity the report entity
     * @return the de accession summary record
     */
    public DeAccessionSummaryRecord prepareDeAccessionSummaryReportRecord(ReportEntity reportEntity) {

        DeAccessionSummaryRecord deAccessionSummaryRecord = new DeAccessionSummaryRecord();
        List<ReportDataEntity> reportDataEntities = reportEntity.getReportDataEntities();
        for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
            ReportDataEntity report =  iterator.next();
            String headerValue = report.getHeaderValue();
            String headerName = report.getHeaderName();
            Method setterMethod = getSetterMethod(headerName);
            if(null != setterMethod){
                try {
                    setterMethod.invoke(deAccessionSummaryRecord, headerValue);
                } catch (Exception e) {
                    log.error(ScsbCommonConstants.LOG_ERROR,e);
                }
            }
        }
        return deAccessionSummaryRecord;
    }

    /**
     * Gets setter method for report object property.
     *
     * @param propertyName the property name
     * @return the setter method
     */
    public Method getSetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            return propertyUtilsBean.getWriteMethod(new PropertyDescriptor(propertyName, DeAccessionSummaryRecord.class));
        } catch (IntrospectionException e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return null;
    }

    /**
     * Gets getter method for report object property.
     *
     * @param propertyName the property name
     * @return the getter method
     */
    public Method getGetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            return propertyUtilsBean.getReadMethod(new PropertyDescriptor(propertyName, DeAccessionSummaryRecord.class));
        } catch (IntrospectionException e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return null;
    }
}
