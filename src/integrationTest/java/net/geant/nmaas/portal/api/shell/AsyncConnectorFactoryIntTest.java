package net.geant.nmaas.portal.api.shell;

import net.geant.nmaas.portal.api.shell.connectors.AsyncConnector;
import net.geant.nmaas.portal.api.shell.connectors.AsyncConnectorFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AsyncConnectorFactoryIntTest {

    @Autowired
    private AsyncConnectorFactory factory;

    @Disabled
    @Test
    void test() throws IOException {
        AsyncConnector connector = factory.prepareConnection("pllab", "pllab-bastion-3-564bf78fcc-kb6ph");
        connector.executeCommand("ls");
    }

}
