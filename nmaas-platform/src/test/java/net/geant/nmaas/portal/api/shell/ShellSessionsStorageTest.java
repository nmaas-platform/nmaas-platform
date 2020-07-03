package net.geant.nmaas.portal.api.shell;

import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.api.shell.connectors.AsyncConnector;
import net.geant.nmaas.portal.api.shell.connectors.AsyncConnectorFactory;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShellSessionsStorageTest {

    private ApplicationInstanceService instanceService = mock(ApplicationInstanceService.class);
    private AsyncConnectorFactory connectorFactory = mock(AsyncConnectorFactory.class);
    private AsyncConnector connector = mock(AsyncConnector.class);

    private ShellSessionsStorage storage;

    @BeforeEach
    public void setup() {
        when(connectorFactory.prepareConnection(any())).thenReturn(connector);
        storage = new ShellSessionsStorage(instanceService, connectorFactory);
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
            storage.executeCommand("sessionId", new ShellCommandRequest("command",""));
        });

        assertEquals("Session with id: sessionId does not exist", ex.getMessage());
    }

    @Test
    @Disabled
    public void shouldCreateAndRemoveSession() {
        // TODO find a way to mock connector initialization
        // currently only default connector is available and it cannot be mocked
        // implement this test after connection is produced out of app instance data

    }
}
