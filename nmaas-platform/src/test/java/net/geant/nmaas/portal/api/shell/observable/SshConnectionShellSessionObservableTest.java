package net.geant.nmaas.portal.api.shell.observable;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.shell.ShellCommandRequest;
import net.geant.nmaas.portal.api.shell.connectors.DefaultSSHShellConnectionData;
import net.geant.nmaas.portal.api.shell.observable.SshConnectionShellSessionObservable;
import net.geant.nmaas.portal.api.shell.connectors.SshSessionConnector;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Log4j2
public class SshConnectionShellSessionObservableTest {

    public final String PUB_KEY = DefaultSSHShellConnectionData.SSH_PUB_KEY_X509;
    public final String PRIV_KEY = DefaultSSHShellConnectionData.SSH_PRIV_KEY;

    @Test
    public void testPublicKeyConversion() throws InvalidKeySpecException, NoSuchAlgorithmException {
        PublicKey publicKey = DefaultSSHShellConnectionData.getPublicKey(PUB_KEY);
        System.out.println(publicKey);
    }

    @Test
    public void testPrivateKeyConversion() throws InvalidKeySpecException, NoSuchAlgorithmException {
        PrivateKey privateKey = DefaultSSHShellConnectionData.getPrivateKey(PRIV_KEY);
        System.out.println(privateKey);
    }

    @Getter
    @Log4j2
    private static class TestObserver implements Observer {

        private final List<String> messages = new ArrayList<>();

        @Override
        public void update(Observable observable, Object o) {
            log.debug("Test observer received:\t" + o.toString());
            this.messages.add(o.toString());
        }
    }

    @Test
    public void testSynchronousCommandExecution() throws IOException {
        SshSessionConnector mockConnector = mock(SshSessionConnector.class);

        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(inputStream);

        when(mockConnector.getInputStream()).thenReturn(inputStream);
        when(mockConnector.getErrorStream()).thenReturn(new PipedInputStream()); // do nothing

        when(mockConnector.executeSingleCommand(anyString())).thenReturn("result\nresult\nresult");

        SshConnectionShellSessionObservable underTest = new SshConnectionShellSessionObservable("sessionId", mockConnector);
        TestObserver to = new TestObserver();
        underTest.addObserver(to);

        String line1 = "some result line\r\n";
        underTest.executeCommand(new ShellCommandRequest(line1, ""));
        String line2 = "host@localhost:~/ $ \n";
        underTest.executeCommand(new ShellCommandRequest(line2, ""));
        String line3 = "continuation\r\n";
        underTest.executeCommand(new ShellCommandRequest(line3, ""));
        String line4 = "some another line\r\n";
        underTest.executeCommand(new ShellCommandRequest(line4, ""));

        assertEquals(4*3, to.getMessages().size());

        underTest.complete();
    }

    @Test
    public void testAsynchronousCommandExecution() throws IOException, InterruptedException {
        SshSessionConnector mockConnector = mock(SshSessionConnector.class);

        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(inputStream);

        doAnswer(invocation -> {
            outputStream.write(invocation.getArgument(0).toString().getBytes());
            outputStream.flush();
            return null;
        }).when(mockConnector).executeCommand(anyString());

        when(mockConnector.getInputStream()).thenReturn(inputStream);
        when(mockConnector.getErrorStream()).thenReturn(new PipedInputStream()); // do nothing

        SshConnectionShellSessionObservable underTest = new SshConnectionShellSessionObservable("sessionId", mockConnector);
        TestObserver to = new TestObserver();
        underTest.addObserver(to);


        String line1 = "some result line\r\n";
        underTest.executeCommandAsync(new ShellCommandRequest(line1, ""));
        String line2 = "host@localhost:~/ $ \n";
        underTest.executeCommandAsync(new ShellCommandRequest(line2, ""));
        String line3 = "continuation\r\n";
        underTest.executeCommandAsync(new ShellCommandRequest(line3, ""));
        String line4 = "some another line\r\n";
        underTest.executeCommandAsync(new ShellCommandRequest(line4, ""));

        Thread.sleep(100);

        assertEquals(4, to.getMessages().size());
        assertTrue(to.messages.get(0).equalsIgnoreCase(line1.replace("\r", "<#>NEWLINE<#>").trim()));
        assertEquals(to.messages.get(1).trim(), line2.trim());
        assertTrue(to.messages.get(2).equalsIgnoreCase(line3.replace("\r", "<#>NEWLINE<#>").trim()));
        assertTrue(to.messages.get(3).equalsIgnoreCase(line4.replace("\r", "<#>NEWLINE<#>").trim()));

        underTest.complete();

    }
}
