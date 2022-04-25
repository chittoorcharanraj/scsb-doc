package org.recap.exception;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.recap.BaseTestCaseUT;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SCSBDocExceptionHandlerUT extends BaseTestCaseUT {

    @InjectMocks
    SCSBDocExceptionHandler scsbDocExceptionHandler;

    @Mock
    CGDRoundTripReportException exception;

    @Mock
    WebRequest request;

    @Test
    @DisplayName("Test handle CGD for Round Trip Exceptions")
    public void handleCGDRoundTripExceptions() {
        ResponseEntity<Object> handleCGDRoundTripExceptions= scsbDocExceptionHandler.handleCGDRoundTripExceptions(exception,request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,handleCGDRoundTripExceptions.getStatusCode());
    }
}
