package net.geant.nmaas.portal.api.configuration;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.InitScriptsStateService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class InitScriptsControllerTest {

    private final InitScriptsStateService service = mock(InitScriptsStateService.class);

    private final InitScriptsController controller = new InitScriptsController(service);

    @Test
    void shouldEnableFlagOnStart() {
        assertThat(controller.isInitInProgress()).isFalse();
        controller.startInitScripts();
        assertThat(controller.isInitInProgress()).isTrue();
    }

    @Test
    void shouldDisableFlagOnEnd() {
        assertThat(controller.isInitInProgress()).isFalse();
        controller.endInitScripts();
        assertThat(controller.isInitInProgress()).isFalse();
        verify(service, times(1)).executeHelmRepoUpdate();
    }

}
