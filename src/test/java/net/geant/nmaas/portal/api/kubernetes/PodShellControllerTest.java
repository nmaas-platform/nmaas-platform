package net.geant.nmaas.portal.api.kubernetes;

import net.geant.nmaas.portal.api.domain.K8sShellCommandRequest;
import net.geant.nmaas.portal.service.K8sShellService;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PodShellControllerTest {

    @Test
    public void shouldCallProperMethods() {
        K8sShellService k8sShellService = mock(K8sShellService.class);
        SseEmitter emitter = mock(SseEmitter.class);
        when(k8sShellService.getEmitterForShellSession(anyString())).thenReturn(emitter);

        PodShellController controller = new PodShellController(k8sShellService);

        controller.init(null, 12L, "podName");
        verify(k8sShellService).createNewShellSession(12L, "podName");

        controller.getShell(null, "sessionId");
        verify(k8sShellService).getEmitterForShellSession("sessionId");

        controller.execute(null, "", new K8sShellCommandRequest("",""));
        verify(k8sShellService).executeShellCommand(anyString(), any());

        controller.complete(null, "sessionId");
        verify(k8sShellService).teardownShellSession("sessionId");
    }

}
