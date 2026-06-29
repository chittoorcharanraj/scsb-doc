package org.recap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@TestPropertySource("classpath:application.properties")
@MockitoSettings(strictness = Strictness.LENIENT)
public class BaseTestCaseUT {

    @Test
    public void loadContexts() {
        Assert.isTrue(true, "test");
    }

}
