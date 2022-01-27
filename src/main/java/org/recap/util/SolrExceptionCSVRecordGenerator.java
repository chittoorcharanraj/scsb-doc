package org.recap.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.recap.ScsbCommonConstants;
import org.recap.model.csv.SolrExceptionReportCSVRecord;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Created by angelind on 22/8/16.
 */
@Slf4j
public class SolrExceptionCSVRecordGenerator {



    /**
     * This method prepares SolrExceptionReportCSVRecord which is used to generate csv report based on the given report entity
     *
     * @param reportEntity                      the report entity
     * @param solrExceptionReportCSVRecord the solr exception report re capcsv record
     * @return the solr exception report re capcsv record
     */
    public SolrExceptionReportCSVRecord prepareSolrExceptionReportCSVRecord(ReportEntity reportEntity, SolrExceptionReportCSVRecord solrExceptionReportCSVRecord) {

        List<ReportDataEntity> reportDataEntities = reportEntity.getReportDataEntities();

        for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
            ReportDataEntity report =  iterator.next();
            String headerName = report.getHeaderName();
            String headerValue = report.getHeaderValue();
            Method setterMethod = getSetterMethod(headerName);
            if(null != setterMethod){
                try {
                    setterMethod.invoke(solrExceptionReportCSVRecord, headerValue);
                } catch (Exception e) {
                    log.error(ScsbCommonConstants.LOG_ERROR,e);
                }
            }
        }
        return solrExceptionReportCSVRecord;
    }

    /**
     * This method is used to get the setter method for the given one of the instance variable name in SolrExceptionReportCSVRecord class.
     *
     * @param propertyName the property name
     * @return the setter method
     */
    public Method getSetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            return propertyUtilsBean.getWriteMethod(new PropertyDescriptor(propertyName, SolrExceptionReportCSVRecord.class));
        } catch (IntrospectionException e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return null;
    }

    /**
     * This method is used to get the getter method for the given one of the instance variable name in SolrExceptionReportCSVRecord class.
     *
     * @param propertyName the property name
     * @return the getter method
     */
    public Method getGetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            return propertyUtilsBean.getReadMethod(new PropertyDescriptor(propertyName, SolrExceptionReportCSVRecord.class));
        } catch (IntrospectionException e) {
            log.error(ScsbCommonConstants.LOG_ERROR,e);
        }
        return null;
    }
}
