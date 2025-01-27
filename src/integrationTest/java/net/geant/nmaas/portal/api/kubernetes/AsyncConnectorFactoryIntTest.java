package net.geant.nmaas.portal.api.kubernetes;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.AsyncConnectorFactory;
import net.geant.nmaas.kubernetes.KubernetesConnector;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
public class AsyncConnectorFactoryIntTest {

    @Autowired
    private AsyncConnectorFactory factory;

    @Disabled
    @Test
    void test() throws IOException {
        KubernetesConnector connector = (KubernetesConnector) factory.preparePodShellConnection("pllab", "pllab-maddash-32-maddash-5697456d98-45m7b");
        InputStream inputStream = connector.getInputStream();
        connector.executeCommand("ls");
        connector.executeCommand("touch test" + System.currentTimeMillis());
        log.debug(Arrays.toString(inputStream.readAllBytes()));
    }

}
