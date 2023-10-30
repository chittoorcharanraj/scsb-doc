package org.recap.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
