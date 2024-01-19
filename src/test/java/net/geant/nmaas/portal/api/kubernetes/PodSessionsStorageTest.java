package net.geant.nmaas.portal.api.kubernetes;

import net.geant.nmaas.kubernetes.shell.PodSessionsStorage;
import net.geant.nmaas.kubernetes.AsyncConnector;
import net.geant.nmaas.kubernetes.AsyncConnectorFactory;
import net.geant.nmaas.kubernetes.shell.observer.ShellSessionObserver;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.api.domain.K8sShellCommandRequest;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PodSessionsStorageTest {

    private final ApplicationInstanceService instanceService = mock(ApplicationInstanceService.class);
    private final AsyncConnectorFactory connectorFactory = mock(AsyncConnectorFactory.class);
    private final AsyncConnector connector = mock(AsyncConnector.class);

    private PodSessionsStorage storage;

    @BeforeEach
    public void setup() throws IOException {
        when(connectorFactory.preparePodShellConnection(any())).thenReturn(connector);
        storage = new PodSessionsStorage(instanceService, connectorFactory);

        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(inputStream);

        PipedInputStream errorStream = new PipedInputStream();
        PipedOutputStream outputErrorStream = new PipedOutputStream(errorStream);

        when(connector.getInputStream()).thenReturn(inputStream);
        when(connector.getErrorStream()).thenReturn(errorStream);
        doAnswer(invocation -> {
            outputStream.write(invocation.getArgument(0).toString().getBytes());
            outputStream.flush();
            return null;
        }).when(connector).executeCommand(anyString());
        when(connector.executeSingleCommand(anyString())).thenReturn("result\nresult\nresult");
    }

    @Test
    public void shouldThrowExceptionWhenAppInstanceDoesNotExist() {
        when(instanceService.find(anyLong())).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            storage.createSession(1L);
        });
        assertEquals("This application instance does not exists", ex.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenSSHAccessDisabled() {
        AppDeploymentSpec appDeploymentSpec = mock(AppDeploymentSpec.class);
        when(appDeploymentSpec.isAllowSshAccess()).thenReturn(false);
        Application application = mock(Application.class);
        when(application.getAppDeploymentSpec()).thenReturn(appDeploymentSpec);
        AppInstance appInstance = mock(AppInstance.class);
        when(appInstance.getApplication()).thenReturn(application);
        when(instanceService.find(anyLong())).thenReturn(Optional.of(appInstance));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            storage.createSession(1L);
        });

        assertEquals("SSH connection is not allowed", ex.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenRetrievingNotExistingConnection() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            storage.executeCommand("sessionId", new K8sShellCommandRequest("command",""));
        });
        assertEquals("Session with id: sessionId does not exist", ex.getMessage());
    }

    @Test
    public void shouldCreateAndRemoveSession() {
        AppDeploymentSpec appDeploymentSpec = mock(AppDeploymentSpec.class);
        when(appDeploymentSpec.isAllowSshAccess()).thenReturn(true);
        Application application = mock(Application.class);
        when(application.getAppDeploymentSpec()).thenReturn(appDeploymentSpec);
        AppInstance appInstance = mock(AppInstance.class);
        when(appInstance.getApplication()).thenReturn(application);
        when(instanceService.find(anyLong())).thenReturn(Optional.of(appInstance));

        String sessionId = storage.createSession(1L);

        verify(connectorFactory, times(1)).preparePodShellConnection(any());

        storage.executeCommand(sessionId, new K8sShellCommandRequest("command", ""));
        verify(connector, times(1)).executeCommand(anyString());

        ShellSessionObserver observer = storage.getObserver(sessionId);
        assertNotNull(observer);

        storage.completeSession(sessionId);
        verify(connector, times(1)).close();
    }

}
