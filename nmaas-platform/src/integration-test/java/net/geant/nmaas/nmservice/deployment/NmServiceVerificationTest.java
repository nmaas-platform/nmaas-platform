package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotVerifyNmServiceException;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

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
        doNothing().when(orchestrator).checkService(any());
        provider.verifyNmService(Identifier.newInstance("id"));
    }

    @Test
    public void shouldVerifyDeploymentSuccessAfterThirdAttempt() {
        doThrow(new ContainerCheckFailedException(""))
                .doThrow(new ContainerCheckFailedException(""))
                .doNothing()
                .when(orchestrator).checkService(any());
        provider.verifyNmService(Identifier.newInstance("id"));
    }

    @Test
    public void shouldVerifyDeploymentFailure(){
        assertThrows(CouldNotVerifyNmServiceException.class, () -> {
            doThrow(new ContainerCheckFailedException(""))
                    .doThrow(new ContainerCheckFailedException(""))
                    .doThrow(new ContainerCheckFailedException(""))
                    .doThrow(new ContainerCheckFailedException(""))
                    .doThrow(new ContainerCheckFailedException(""))
                    .doThrow(new ContainerCheckFailedException(""))
                    .when(orchestrator).checkService(any());
            provider.verifyNmService(Identifier.newInstance("id"));
        });
    }

}
