package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotVerifyNmServiceException;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NmServiceVerificationTest {

    @Mock
    private ContainerOrchestrator orchestrator;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private NmServiceDeploymentCoordinator provider;

    @BeforeEach
    public void setup() {
        provider = new NmServiceDeploymentCoordinator(orchestrator, applicationEventPublisher);
        provider.serviceDeploymentCheckMaxWaitTime = 5;
        provider.serviceDeploymentCheckInternal = 1;
    }

    @Test
    public void shouldVerifyDeploymentSuccessRightAway() {
        assertDoesNotThrow(() -> {
            when(orchestrator.checkService(any())).thenReturn(true);
            provider.verifyNmService(Identifier.newInstance("id"));
        });
    }

    @Test
    public void shouldVerifyDeploymentSuccessAfterThirdAttempt() {
        assertDoesNotThrow(() -> {
            when(orchestrator.checkService(any()))
                    .thenReturn(false)
                    .thenReturn(false)
                    .thenReturn(true);
            provider.verifyNmService(Identifier.newInstance("id"));
        });
    }

    @Test
    public void shouldVerifyDeploymentFailure(){
        assertThrows(CouldNotVerifyNmServiceException.class, () -> {
            when(orchestrator.checkService(any()))
                    .thenReturn(false)
                    .thenReturn(false)
                    .thenReturn(false)
                    .thenReturn(false)
                    .thenReturn(false);
            provider.verifyNmService(Identifier.newInstance("id"));
        });
    }

    @Test
    public void shouldThrowExceptionWhenUnexpectedErrorOccurs(){
        assertThrows(CouldNotVerifyNmServiceException.class, () -> {
            when(orchestrator.checkService(any())).thenThrow(new ContainerCheckFailedException(""));
            provider.verifyNmService(Identifier.newInstance("id"));
        });
    }

}
