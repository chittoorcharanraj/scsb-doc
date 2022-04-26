package org.recap.service;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spi.RouteController;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.recap.BaseTestCase;
import org.recap.BaseTestCaseUT;
import org.recap.BaseTestCaseUT4;
import org.recap.ScsbConstants;
import org.recap.exception.CGDRoundTripReportException;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.jpa.ItemChangeLogEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.repository.jpa.ItemChangeLogDetailsRepository;
import org.recap.repository.jpa.ItemDetailsRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Unit Test for OngoingMatchingAlgorithmService class")
public class OngoingMatchingAlgorithmServiceUT extends BaseTestCaseUT {

    @Mock
    ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    @Mock
    ProducerTemplate producerTemplate;

    @Mock
    CamelContext camelContext;

    @Mock
    ItemDetailsRepository itemDetailsRepository;

    @Mock
    RouteController mockedRouteController;

    @InjectMocks
    OngoingMatchingAlgorithmService ongoingMatchingAlgorithmService;

    @BeforeEach
   public void prepareMocks()throws Exception{
        List<ItemChangeLogEntity> itemChangeLogEntityList = getItemChangeLogEntityList();
        ItemEntity itemEntity1 = getItemEntity(1, "ItemId1", "PA", "12345", "PUL");
        ItemEntity itemEntity2 = getItemEntity(2, "ItemId2", "CU", "67890", "CUL");
        ItemEntity itemEntity3 = getItemEntity(3, "ItemId3", "NA", "54321", "NYPL");
        ItemEntity itemEntity4 = getItemEntity(4, "ItemId3", "FL", "34567", "HL");
        when(itemChangeLogDetailsRepository.findByUpdatedDateAndOperationType(any(), anyString())).thenReturn(itemChangeLogEntityList);
        when(itemDetailsRepository.findById(anyInt())).thenReturn(Optional.of(itemEntity1),Optional.of(itemEntity2),Optional.of(itemEntity3));
        when(camelContext.getRouteController()).thenReturn(mockedRouteController);
        doNothing().when(mockedRouteController).startRoute(anyString());
        doNothing().when(producerTemplate).sendBodyAndHeaders(anyString(), any(),Mockito.anyMap());
    }


    @Ignore
    @DisplayName("Test cgd round trip report for Ongoing Matching Algorithm")
    public void testCGDRoundTripReport(){
        String result = ongoingMatchingAlgorithmService.generateCGDRoundTripReport();
        Assertions.assertEquals("CGD Round-Trip report generated successfully",result);
        verify(itemChangeLogDetailsRepository,times(1)).findByUpdatedDateAndOperationType(any(), anyString());
        verify(itemDetailsRepository,times(3)).findById(anyInt());
        verify(producerTemplate,times(3)).sendBodyAndHeaders(anyString(), any(),Mockito.anyMap());
    }

    @Test
    @DisplayName("Test Exception when reported Item is not found to generate for CGD Round Trip")
   public void testWhenReportedItemIsNotFound(){
        List<ItemChangeLogEntity> itemChangeLogEntityList = getItemChangeLogEntityList();
        when(itemChangeLogDetailsRepository.findByUpdatedDateAndOperationType(any(), anyString())).thenReturn(itemChangeLogEntityList);
        when(itemDetailsRepository.findById(anyInt())).thenReturn(Optional.empty());
        CGDRoundTripReportException cgdRoundTripReportException = Assertions.assertThrows(CGDRoundTripReportException.class, () -> ongoingMatchingAlgorithmService.generateCGDRoundTripReport());
        Assertions.assertEquals(ScsbConstants.CGD_ROUND_TRIP_EXCEPTION_MESSAGE,cgdRoundTripReportException.getMessage());
    }


    @Ignore
    @DisplayName("Test NoSuchElementException in CGD Round Trip")
    public void testNoSuchElementFoundException(){
        when(itemDetailsRepository.findById(anyInt())).thenThrow(new NoSuchElementException());
        CGDRoundTripReportException cgdRoundTripReportException = Assertions.assertThrows(CGDRoundTripReportException.class, () -> ongoingMatchingAlgorithmService.generateCGDRoundTripReport());
        Assertions.assertEquals(ScsbConstants.CGD_ROUND_TRIP_EXCEPTION_MESSAGE,cgdRoundTripReportException.getMessage());
    }

    @Test
    @DisplayName("Test when there are no reports to generate CGD Round Trip")
    public void testWhenNoCGDReportsAreGenerated() {
        List<ItemChangeLogEntity> itemChangeLogEntityList=new ArrayList<>();
        when(itemChangeLogDetailsRepository.findByUpdatedDateAndOperationType(anyString(), anyString())).thenReturn(itemChangeLogEntityList);
        String result = ongoingMatchingAlgorithmService.generateCGDRoundTripReport();
        Assertions.assertEquals("No records found to generate CGD Round-Trip report",result);
    }


    @Ignore
    @DisplayName("Test CamelExecutionException in CGD Round Trip")
   public void testForCamelExecutionException(){
        doThrow(CamelExecutionException.class).when(producerTemplate).sendBodyAndHeaders(anyString(), any(),Mockito.anyMap());
        CGDRoundTripReportException cgdRoundTripReportException = Assertions.assertThrows(CGDRoundTripReportException.class, () -> ongoingMatchingAlgorithmService.generateCGDRoundTripReport());
        Assertions.assertEquals("Exception occurred in camel executing while trying to generate CGD Round Trip report",cgdRoundTripReportException.getMessage());
    }


    @Ignore
    @DisplayName("Test for Generic exception in CGD Round Trip")
   public void testForAnyOtherException(){
        doThrow(RuntimeException.class).when(producerTemplate).sendBodyAndHeaders(anyString(), any(),Mockito.anyMap());
        Exception cgdRoundTripReportException = Assertions.assertThrows(Exception.class, () -> ongoingMatchingAlgorithmService.generateCGDRoundTripReport());
        Assertions.assertEquals("Exception occurred while generating CGD Round-Trip report",cgdRoundTripReportException.getMessage());
    }

    private ItemEntity getItemEntity(int id, String itemId, String customerCode, String barcode, String institutionCode) {
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setId(id);
        itemEntity.setOwningInstitutionItemId(itemId);
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setCustomerCode(customerCode);
        itemEntity.setBarcode(barcode);
        InstitutionEntity institutionEntity1 = getInstitutionEntity(id, institutionCode);
        itemEntity.setInstitutionEntity(institutionEntity1);
        return itemEntity;
    }

    private InstitutionEntity getInstitutionEntity(int id, String institutionCode) {
        InstitutionEntity institutionEntity1 = new InstitutionEntity();
        institutionEntity1.setId(id);
        institutionEntity1.setInstitutionName(institutionCode);
        institutionEntity1.setInstitutionCode(institutionCode);
        return institutionEntity1;
    }

    private List<ItemChangeLogEntity> getItemChangeLogEntityList() {
        ItemChangeLogEntity itemChangeLogEntity1 = getItemChangeLogEntity(1, 2);
        ItemChangeLogEntity itemChangeLogEntity2 = getItemChangeLogEntity(2, 5);
        ItemChangeLogEntity itemChangeLogEntity3 = getItemChangeLogEntity(3, 6);
        return Arrays.asList(itemChangeLogEntity1,itemChangeLogEntity2,itemChangeLogEntity3);
    }

    private ItemChangeLogEntity getItemChangeLogEntity(int id, int recordId) {
        ItemChangeLogEntity itemChangeLogEntity1 = new ItemChangeLogEntity();
        itemChangeLogEntity1.setId(id);
        itemChangeLogEntity1.setUpdatedBy("UT");
        itemChangeLogEntity1.setUpdatedDate(new Date());
        itemChangeLogEntity1.setOperationType("OngoingMatchingAlgorithm");
        itemChangeLogEntity1.setRecordId(recordId);
        itemChangeLogEntity1.setNotes("1-2");
        return itemChangeLogEntity1;
    }

}
