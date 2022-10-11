package org.recap.exception;

import org.recap.controller.OngoingMatchingAlgorithmJobRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice(assignableTypes = OngoingMatchingAlgorithmJobRestController.class)
@RestController
public class SCSBDocExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CGDRoundTripReportException.class)
    public static final ResponseEntity<Object> handleCGDRoundTripExceptions(CGDRoundTripReportException exception, WebRequest request){
        ExceptionResponse exceptionResponse = new ExceptionResponse(exception.getMessage(), request.getDescription(false),new Date(),exception.getCause());
        return new ResponseEntity<>(exceptionResponse,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
