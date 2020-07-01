package net.geant.nmaas.utils.ssh;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.reflection.FieldSetter;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        FieldSetter.setField(connector, this.connector.getClass().getDeclaredField("client"), this.mockedClient);
        FieldSetter.setField(connector, this.connector.getClass().getDeclaredField("session"), this.mockedSession);
        FieldSetter.setField(connector, this.connector.getClass().getDeclaredField("shell"), this.mockedShell);
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
        connector.executeCommandInSession(test1);

        result = reader.readLine();
        assertTrue(test1.equalsIgnoreCase(result));

        String test2 = "second test";
        String test3 = "third test";
        connector.executeCommandInSession(test2);
        connector.executeCommandInSession(test3);

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
