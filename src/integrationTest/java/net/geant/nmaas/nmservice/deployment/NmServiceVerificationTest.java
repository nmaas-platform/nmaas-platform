package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotVerifyServiceException;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NmServiceVerificationTest {

    private final ContainerOrchestrator orchestrator = mock(ContainerOrchestrator.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private NmServiceDeploymentCoordinator provider;

    @BeforeEach
    void setup() {
        provider = new NmServiceDeploymentCoordinator(orchestrator, eventPublisher);
        provider.serviceDeploymentCheckMaxWaitTime = 5;
        provider.serviceDeploymentCheckInternal = 1;
    }

    @Test
    void shouldVerifyDeploymentSuccessRightAway() {
        assertDoesNotThrow(() -> {
            when(orchestrator.checkService(any())).thenReturn(true);
            provider.verifyService(Identifier.newInstance("id"));
        });
    }

    @Test
    void shouldVerifyDeploymentSuccessAfterThirdAttempt() {
        assertDoesNotThrow(() -> {
            when(orchestrator.checkService(any()))
                    .thenReturn(false)
                    .thenReturn(false)
                    .thenReturn(true);
            provider.verifyService(Identifier.newInstance("id"));
        });
    }

    @Test
    void shouldVerifyDeploymentFailure() {
        assertThrows(CouldNotVerifyServiceException.class, () -> {
            when(orchestrator.checkService(any()))
                    .thenReturn(false)
                    .thenReturn(false)
                    .thenReturn(false)
                    .thenReturn(false)
                    .thenReturn(false);
            provider.verifyService(Identifier.newInstance("id"));
        });
    }

    @Test
    void shouldThrowExceptionWhenUnexpectedErrorOccurs() {
        assertThrows(CouldNotVerifyServiceException.class, () -> {
            when(orchestrator.checkService(any())).thenThrow(new ContainerCheckFailedException(""));
            provider.verifyService(Identifier.newInstance("id"));
        });
    }

}
