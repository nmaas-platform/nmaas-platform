package net.geant.nmaas.portal.api.shell;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class ShellSessionObserverTest {

    /**
     * extend object under test to provide custom parameters
     */
    private static class TestableShellSessionObserver extends ShellSessionObserver {

        public TestableShellSessionObserver(SseEmitter emitter) {
            this.emitter = emitter;
            this.startExecutor();
        }
    }


    @Test
    public void testObserver() throws IOException {
        GenericShellSessionObservable observable = new EchoShellSessionObservable("someId");
        SseEmitter mockEmitter = mock(SseEmitter.class);
        ShellSessionObserver observer = new TestableShellSessionObserver(mockEmitter);

        observable.addObserver(observer);

        observable.executeCommand(new ShellCommandRequest("some command", ""));

        // one heartbeat and one message
        verify(mockEmitter, times(2)).send(any());

        observer.complete();
        observable.complete();


    }
}
