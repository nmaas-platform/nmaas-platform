package net.geant.nmaas.portal.api.shell;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.mockito.Mockito.*;

public class ShellClientControllerTest {

    @Test
    public void shouldCallProperMethods() throws InvalidKeySpecException, NoSuchAlgorithmException {
        ShellSessionsStorage storage = mock(ShellSessionsStorage.class);
        ShellSessionObserver observer = mock(ShellSessionObserver.class);
        SseEmitter emitter = mock(SseEmitter.class);
        when(observer.getEmitter()).thenReturn(emitter);
        when(storage.getObserver(anyString())).thenReturn(observer);

        ShellClientController controller = new ShellClientController(storage);

        controller.init(null, 12L);
        verify(storage, times(1)).createSession(anyLong());

        controller.getShell(null, "sessionId");
        verify(storage, times(1)).getObserver(anyString());

        controller.execute(null, "", new ShellCommandRequest("",""));
        verify(storage, times(1)).executeCommand(anyString(), any());

        controller.complete(null, "sessionId");
        verify(storage, times(1)).completeSession(anyString());
    }
}
