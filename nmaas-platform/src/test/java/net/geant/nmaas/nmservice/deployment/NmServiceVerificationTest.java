package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotVerifyNmServiceException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NmServiceVerificationTest {

    @Mock
    private ContainerOrchestrator orchestrator;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private AppDeploymentRepository appDeploymentRepository;
    @Mock
    private DomainRepository domainRepository;

    private NmServiceDeploymentCoordinator provider;

    @Before
    public void setup() {
        provider = new NmServiceDeploymentCoordinator(orchestrator, applicationEventPublisher, appDeploymentRepository, domainRepository);
        provider.serviceDeploymentCheckMaxWaitTime = 5;
        provider.serviceDeploymentCheckInternal = 1;
    }

    @Test
    public void shouldVerifyDeploymentSuccessRightAway() throws Exception {
        doNothing().when(orchestrator).checkService(any());
        provider.verifyNmService(Identifier.newInstance("id"));
    }

    @Test
    public void shouldVerifyDeploymentSuccessAfterThirdAttempt() throws Exception {
        doThrow(new ContainerCheckFailedException(""))
                .doThrow(new ContainerCheckFailedException(""))
                .doNothing()
                .when(orchestrator).checkService(any());
        provider.verifyNmService(Identifier.newInstance("id"));
    }

    @Test(expected = CouldNotVerifyNmServiceException.class)
    public void shouldVerifyDeploymentFailure() throws Exception {
        doThrow(new ContainerCheckFailedException(""))
                .doThrow(new ContainerCheckFailedException(""))
                .doThrow(new ContainerCheckFailedException(""))
                .doThrow(new ContainerCheckFailedException(""))
                .doThrow(new ContainerCheckFailedException(""))
                .doThrow(new ContainerCheckFailedException(""))
                .when(orchestrator).checkService(any());
        provider.verifyNmService(Identifier.newInstance("id"));
    }

}
