package net.geant.nmaas.utils.ssh;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SshSessionConnectorTest {

    private SshSessionConnector connector;
    private SSHClient mockedClient;
    private Session mockedSession;
    private Session.Shell mockedShell;

    private PipedInputStream inputStream;
    private OutputStream outputStream;

    @BeforeEach
    public void setup() throws NoSuchFieldException, IOException {
        this.connector = new SshSessionConnector();
        this.mockedClient = mock(SSHClient.class);
        this.mockedSession = mock(Session.class);
        this.mockedShell = mock(Session.Shell.class);

        when(this.mockedClient.isConnected()).thenReturn(true);
        when(this.mockedClient.isAuthenticated()).thenReturn(true);
        when(this.mockedSession.isOpen()).thenReturn(true);

        // mocked connected pipes
        this.inputStream = new PipedInputStream();
        this.outputStream = new PipedOutputStream(this.inputStream);

        when(this.mockedShell.getOutputStream()).thenReturn(this.outputStream);
        when(this.mockedShell.getInputStream()).thenReturn(this.inputStream);

        // use reflection to set fields
        ReflectionTestUtils.setField(connector, "client", this.mockedClient);
        ReflectionTestUtils.setField(connector, "session", this.mockedSession);
        ReflectionTestUtils.setField(connector, "shell", this.mockedShell);
    }

    @Test
    public void shouldNotBeConnectedWhenConnectionIsNotSet() {
        SshSessionConnector underTest = new SshSessionConnector();
        assertFalse(underTest.isConnected());
        assertFalse(underTest.isAuthenticated());
        assertFalse(underTest.isSessionOpened());
    }

    @Test
    public void shouldBeConnected() {
        assertTrue(connector.isConnected());
        assertTrue(connector.isAuthenticated());
        assertTrue(connector.isSessionOpened());
    }

    @Test
    public void shouldPassCommandThroughStreams() throws IOException {
        InputStream is = connector.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String result;

        String test1 = "first test";
        connector.executeCommand(test1);

        result = reader.readLine();
        assertTrue(test1.equalsIgnoreCase(result));

        String test2 = "second test";
        String test3 = "third test";
        connector.executeCommand(test2);
        connector.executeCommand(test3);

        result = reader.readLine();
        assertTrue(test2.equalsIgnoreCase(result));

        result = reader.readLine();
        assertTrue(test3.equalsIgnoreCase(result));
    }

    @Test
    public void shouldCloseConnection() throws IOException {
        assertTrue(connector.isConnected());
        assertTrue(connector.isAuthenticated());
        assertTrue(connector.isSessionOpened());

        connector.close();

        verify(mockedSession, times(1)).close();
        verify(mockedClient, times(1)).disconnect();

    }
}
