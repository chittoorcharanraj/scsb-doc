package org.recap.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CGDRoundTripReportExceptionTest {
    @Test
    public void testConstructorWithMessageAndThrowable() {
        String errorMessage = "Test error message";
        Throwable cause = new RuntimeException("Cause exception");
        CGDRoundTripReportException exception = new CGDRoundTripReportException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals(cause, exception.getCause());
    }
}
