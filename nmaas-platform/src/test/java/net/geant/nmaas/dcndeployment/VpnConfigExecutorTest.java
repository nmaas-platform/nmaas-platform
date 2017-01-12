package net.geant.nmaas.dcndeployment;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VpnConfigExecutorTest {

    private static final String TEST_PING_COMMAND = "ping 8.8.8.8";

    @Autowired
    private VpnConfigBuilder configBuilder;

    VpnConfigExecutor executor;

    @Before
    public void setupExecutor() {
        executor = new VpnConfigExecutor();
        executor.command(TEST_PING_COMMAND);
    }

    @Ignore
    @Test
    public void shouldTriggerVpnConfigProcessAndNotifyResult() throws InterruptedException {
        executor.execute();
        Thread.sleep(10000);
    }

    @Test
    public void shouldInjectPropertiesFromFile() {
        assertNotNull(configBuilder.varFilePrefix);
        assertNotNull(configBuilder.varFileSuffix);
        assertNotNull(configBuilder.varFileDirectory);
    }

}
