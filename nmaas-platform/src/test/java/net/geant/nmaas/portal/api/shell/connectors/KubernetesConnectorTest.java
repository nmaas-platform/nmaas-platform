package net.geant.nmaas.portal.api.shell.connectors;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KubernetesConnectorTest {

    private KubernetesConnector connector;

    private final String namespace = "namespace";
    private final String podName = "turtle";


    private KubernetesClient client = mock(KubernetesClient.class);
    private ExecWatch watch = mock(ExecWatch.class);

    /**
     * extend class to be able to mock inner components
     */
    private static class TestableKubernetesConnector extends KubernetesConnector {

        public TestableKubernetesConnector(KubernetesClient client, ExecWatch watch, String namespace, String podName) {
            super();
            this.client = client;
            this.watch = watch;
            this.namespace = namespace;
            this.podName = podName;
        }
    }

    @BeforeEach
    public void setup() throws IOException {
        connector = new TestableKubernetesConnector(client, watch, namespace, podName);

        PipedInputStream is = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(is);

        when(watch.getInput()).thenReturn(out);
        when(watch.getOutput()).thenReturn(is);
    }

    @Test
    public void shouldReturnNotImplementedAfterExecutingSingleCommand() {
        assertEquals("NOT IMPLEMENTED", connector.executeSingleCommand("command"));
    }

    @Test
    public void shouldReturnInputStreams() {
        connector.getInputStream();
        verify(watch, times(1)).getOutput();
        connector.getErrorStream();
        verify(watch, times(1)).getError();
        connector.close();
        verify(watch, times(1)).close();
    }

    @Test
    public void shouldWriteCommandToStreamWhenExecuted() throws IOException {
        final String command = "command";
        connector.executeCommand(command);
        verify(watch, times(2)).getInput();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connector.getInputStream()));
        assertEquals(command, reader.readLine());
    }
}
