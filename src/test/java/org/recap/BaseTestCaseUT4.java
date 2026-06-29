package org.recap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.internal.util.Assert;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
@TestPropertySource("classpath:application.properties")
@MockitoSettings(strictness = Strictness.LENIENT)
public class BaseTestCaseUT4 {

    @Test
    public void loadContexts() {
        Assert.isTrue(true);
    }

}