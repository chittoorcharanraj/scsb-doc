package org.recap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.internal.util.Assert;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
@TestPropertySource("classpath:application.properties")
public class BaseTestCaseUT4 {

    @Test
    public void loadContexts() {
        Assert.isTrue(true);
    }

}