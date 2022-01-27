package org.recap.activemq;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.junit.jupiter.api.Test;
import org.recap.camel.activemq.JmxHelper;

import javax.management.MBeanServerConnection;

import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
public class JmxHelperUT {


    @Test
    public void testGetBeanForQueueName() {

        JmxHelper JmxHelper = new JmxHelper();
        DestinationViewMBean DestinationViewMBean = null;
        try {
            DestinationViewMBean = JmxHelper.getBeanForQueueName("test");
        } catch (Exception exception) {
            log.info("Exception" + exception);
        }
        assertNull(DestinationViewMBean);
    }

    @Test
    public void testGetConnection() {
        JmxHelper JmxHelper = new JmxHelper();
        MBeanServerConnection MBeanServerConnection = null;
        try {
            MBeanServerConnection = JmxHelper.getConnection();
        } catch (Exception e) {
            log.info("Exception" + e);
        }
        assertNull(MBeanServerConnection);
    }
}
