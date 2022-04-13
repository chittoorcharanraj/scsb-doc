package org.recap.exception;


import org.junit.Test;
import org.recap.BaseTestCaseUT;

import java.sql.Timestamp;


import static org.junit.Assert.assertNotNull;

public class ExceptionResponseUT extends BaseTestCaseUT {

 @Test
 public void ExceptionResponse() throws Exception {
  String message = "test";
  String details = "test";
  Timestamp timestamp = new Timestamp(System.currentTimeMillis());
  System.out.println(timestamp);
  Throwable throwable = new Throwable();
  ExceptionResponse exceptionResponse = new ExceptionResponse(message,details,timestamp,throwable);
  exceptionResponse.setMessage(message);
  exceptionResponse.setDetails(details);
  exceptionResponse.setTimestamp(timestamp);
  exceptionResponse.setThrowable(throwable);
  assertNotNull(exceptionResponse.getTimestamp());
  assertNotNull(exceptionResponse.getMessage());
  assertNotNull(exceptionResponse.getDetails());
  assertNotNull(exceptionResponse.getThrowable());

 }
}
