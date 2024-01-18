package net.geant.nmaas.utils.k8sclient;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import net.geant.nmaas.kubernetes.KubernetesConnector;
import net.geant.nmaas.kubernetes.shell.PodShellConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KubernetesConnectorTest {

    private KubernetesConnector connector;

    private final String namespace = "namespace";
    private final String podName = "turtle";

    private final KubernetesClient client = mock(KubernetesClient.class);
    private final ExecWatch watch = mock(ExecWatch.class);

    /**
     * extend class to be able to mock inner components
     */
    private static class TestableKubernetesConnector extends PodShellConnector {

        public TestableKubernetesConnector(KubernetesClient client, ExecWatch watch, String namespace, String podName) {
            super(client, namespace, podName, watch);
        }

    }

    @BeforeEach
    void setup() throws IOException {
        connector = new TestableKubernetesConnector(client, watch, namespace, podName);

        PipedInputStream is = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(is);

        when(watch.getInput()).thenReturn(out);
        when(watch.getOutput()).thenReturn(is);
    }

    @Test
    void shouldReturnNotImplementedAfterExecutingSingleCommand() {
        assertEquals("NOT IMPLEMENTED", connector.executeSingleCommand("command"));
    }

    @Test
    void shouldReturnInputStreams() {
        connector.getInputStream();
        verify(watch, times(1)).getOutput();
        connector.getErrorStream();
        verify(watch, times(1)).getError();
        connector.close();
        verify(watch, times(1)).close();
    }

    @Test
    void shouldWriteCommandToStreamWhenExecuted() throws IOException {
        final String command = "command";
        connector.executeCommand(command);
        verify(watch, times(2)).getInput();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connector.getInputStream()));
        assertEquals(command, reader.readLine());
    }

}
