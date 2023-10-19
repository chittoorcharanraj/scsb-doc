package org.recap;





import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

@RunWith(MockitoJUnitRunner.Silent.class)
@TestPropertySource("classpath:application.properties")
public class BaseTestCaseUT {

    @Test
    public void loadContexts() {
        Assert.isTrue(true);
    }

}
