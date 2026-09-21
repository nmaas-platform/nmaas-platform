package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.configuration.exceptions.NmServiceConfigurationFailedException;
import net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotUpdateKubernetesServiceException;
import net.geant.nmaas.nmservice.deployment.limits.ResourcesLimitValidationService;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class NmServiceDeploymentCoordinatorTest {

    private final Identifier deploymentId = Identifier.newInstance("deploymentId");

    private final ContainerOrchestrator orchestrator = mock(ContainerOrchestrator.class);
    private final ResourcesLimitValidationService resourceLimitsValidationService = mock(ResourcesLimitValidationService.class);
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);

    private NmServiceDeploymentCoordinator coordinator;

    @BeforeEach
    void setup() {
        coordinator = new NmServiceDeploymentCoordinator(
                orchestrator,
                resourceLimitsValidationService,
                applicationEventPublisher);
    }

    @Test
    void shouldUpdateKubernetesServiceAndNotifyStateChangeListeners() {
        coordinator.updateKubernetesService(deploymentId, "test-user");

        verify(orchestrator, times(1)).updateKubernetesService(deploymentId);
        ArgumentCaptor<ApplicationEvent> eventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(applicationEventPublisher, times(2)).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues())
                .hasSize(2)
                .allMatch(NmServiceDeploymentStateChangeEvent.class::isInstance);
        NmServiceDeploymentStateChangeEvent firstEvent =
                (NmServiceDeploymentStateChangeEvent) eventCaptor.getAllValues().get(0);
        NmServiceDeploymentStateChangeEvent secondEvent =
                (NmServiceDeploymentStateChangeEvent) eventCaptor.getAllValues().get(1);
        assertThat(firstEvent.getState()).isEqualTo(ServiceDeploymentState.CONFIGURATION_UPDATE_INITIATED);
        assertThat(firstEvent.getUserInitiator()).isEqualTo("test-user");
        assertThat(firstEvent.getErrorMessage()).isEmpty();
        assertThat(secondEvent.getState()).isEqualTo(ServiceDeploymentState.CONFIGURATION_UPDATED);
        assertThat(secondEvent.getUserInitiator()).isEqualTo("test-user");
        assertThat(secondEvent.getErrorMessage()).isEmpty();
    }

    @Test
    void shouldNotifyStateChangeListenersWithFailureWhenServiceUpdateFails() {
        doThrow(new CouldNotUpdateKubernetesServiceException("update failed"))
                .when(orchestrator).updateKubernetesService(deploymentId);

        assertThatThrownBy(() -> coordinator.updateKubernetesService(deploymentId, "test-user"))
                .isInstanceOf(NmServiceConfigurationFailedException.class)
                .hasMessageContaining("update failed");

        ArgumentCaptor<ApplicationEvent> eventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
        verify(applicationEventPublisher, times(2)).publishEvent(eventCaptor.capture());
        NmServiceDeploymentStateChangeEvent initiatedEvent =
                (NmServiceDeploymentStateChangeEvent) eventCaptor.getAllValues().get(0);
        NmServiceDeploymentStateChangeEvent failedEvent =
                (NmServiceDeploymentStateChangeEvent) eventCaptor.getAllValues().get(1);
        assertThat(initiatedEvent.getState()).isEqualTo(ServiceDeploymentState.CONFIGURATION_UPDATE_INITIATED);
        assertThat(failedEvent.getState()).isEqualTo(ServiceDeploymentState.CONFIGURATION_UPDATE_FAILED);
        assertThat(failedEvent.getUserInitiator()).isEqualTo("test-user");
        assertThat(failedEvent.getErrorMessage()).contains("update failed");
    }

    @Test
    void shouldNotifyStateChangeListenersWithFailureWhenDeploymentIdInvalid() {
        doThrow(new CouldNotUpdateKubernetesServiceException("service not found"))
                .when(orchestrator).updateKubernetesService(any());

        assertThatThrownBy(() -> coordinator.updateKubernetesService(deploymentId, "test-user"))
                .isInstanceOf(NmServiceConfigurationFailedException.class);

        verify(applicationEventPublisher, times(2)).publishEvent(any(ApplicationEvent.class));
    }
}
