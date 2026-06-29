package org.recap.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.recap.BaseTestCaseUT;
import org.springframework.test.util.ReflectionTestUtils;

//@ExtendWith(MockitoExtension.class)
public class AWSClientConfigTest extends BaseTestCaseUT {

    @InjectMocks
    private AWSClientConfig awsClientConfig;

    @Mock
    private AmazonS3ClientBuilder amazonS3ClientBuilder;

    @Mock
    private AWSStaticCredentialsProvider awsStaticCredentialsProvider;

    @Mock
    private AmazonS3 amazonS3;

    @BeforeEach
    void setUp() {
        // Set the properties manually
        ReflectionTestUtils.setField(awsClientConfig, "awsAccessKey", "testAccessKey");
        ReflectionTestUtils.setField(awsClientConfig, "awsAccessSecretKey", "testSecretKey");
    }

    @Test
    public void testGetAwsClient() {
        // Call the method under test
        AmazonS3 client = awsClientConfig.getAwsClient();

        // Verify the result (note: Due to JUnit Jupiter/Mockito limitations with static builders,
        // we cannot easily mock AmazonS3ClientBuilder.standard() calls.
        // This test verifies that getAwsClient() completes successfully)
        assertNotNull(client);
    }
}
