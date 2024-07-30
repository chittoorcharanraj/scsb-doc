package org.recap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.recap.model.queueinfo.QueueSizeInfoJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ActiveMqQueuesInfoTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<String> responseEntity;

    @InjectMocks
    private ActiveMqQueuesInfo activeMqQueuesInfo;

    private final String activeMqApiUrl = "/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=";
    private final String searchAttribute = "/QueueSize";
    private final String serviceUrl = "http://localhost:8161, http://localhost:8162";
    private final String activemqCredentials = "admin:admin";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set the @Value fields
        ReflectionTestUtils.setField(activeMqQueuesInfo, "activeMqApiUrl", activeMqApiUrl);
        ReflectionTestUtils.setField(activeMqQueuesInfo, "searchAttribute", searchAttribute);
        ReflectionTestUtils.setField(activeMqQueuesInfo, "serviceUrl", serviceUrl);
        ReflectionTestUtils.setField(activeMqQueuesInfo, "activemqCredentials", activemqCredentials);
    }

    @Test
    public void testGetActivemqQueuesInfo() throws Exception {

        String queueName = "testQueue";
        String queueSize = "5";

        QueueSizeInfoJson queueSizeInfoJson = new QueueSizeInfoJson();
        queueSizeInfoJson.setValue(queueSize);

//        when(responseEntity.getBody()).thenReturn(new ObjectMapper().writeValueAsString(queueSizeInfoJson));
//        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
//                .thenReturn(responseEntity);

        int queueSizeCount = activeMqQueuesInfo.getActivemqQueuesInfo(queueName);

//        assertEquals(Optional.of(Integer.valueOf(queueSize)), queueSizeCount);

//        verify(restTemplate, times(1)).exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testGetActivemqQueuesInfoWithResourceAccessException() {
        String queueName = "testQueue";

//        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
//                .thenThrow(new ResourceAccessException(""));

        int queueSizeCount = activeMqQueuesInfo.getActivemqQueuesInfo(queueName);

//        assertEquals(Optional.ofNullable(Integer.valueOf(0)), queueSizeCount);

//        verify(restTemplate, times(2)).exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }
}
