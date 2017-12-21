package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.ContainerOrchestrator;
import net.geant.nmaas.nmservice.deployment.exceptions.ContainerCheckFailedException;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("kubernetes")
public class KubernetesManagerCheckServiceTest {

    @Autowired
    private ContainerOrchestrator manager;

    @MockBean
    private HelmCommandExecutor commandExecutor;

    @Test
    public void shouldVerifyThatServiceIsDeployed() throws Exception {
        when(commandExecutor.executeHelmStatusCommand(any(Identifier.class))).thenReturn(HelmPackageStatus.DEPLOYED);
        manager.checkService(Identifier.newInstance("deploymentId"));
    }

    @Test(expected = ContainerCheckFailedException.class)
    public void shouldThrowExceptionSinceServiceNotDeployed() throws Exception {
        when(commandExecutor.executeHelmStatusCommand(any(Identifier.class))).thenReturn(HelmPackageStatus.UNKNOWN);
        manager.checkService(Identifier.newInstance("deploymentId"));
    }

}
