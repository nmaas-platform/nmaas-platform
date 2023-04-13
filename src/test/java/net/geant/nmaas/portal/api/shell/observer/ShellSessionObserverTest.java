package net.geant.nmaas.portal.api.shell.observer;

import net.geant.nmaas.kubernetes.shell.observer.ShellSessionObserver;
import net.geant.nmaas.portal.api.domain.K8sShellCommandRequest;
import net.geant.nmaas.kubernetes.shell.observable.EchoShellSessionObservable;
import net.geant.nmaas.kubernetes.shell.observable.GenericShellSessionObservable;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

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

        observable.executeCommand(new K8sShellCommandRequest("some command", ""));

        // one heartbeat and one message
        verify(mockEmitter, timeout(200).times(2)).send(any());

        observer.complete();
        observable.complete();
    }

}
