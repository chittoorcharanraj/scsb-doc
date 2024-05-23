package org.recap.config;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.recap.BaseTestCaseUT;

public class SamplerConfigTest extends BaseTestCaseUT {
   @InjectMocks
    private SamplerConfig samplerConfig;

    /**
     * Method under test: {@link SamplerConfig#defaultSampler()}
     */
    @Test
    public void testDefaultSampler() {
        samplerConfig.defaultSampler();
    }
}
