package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"env_docker-engine", "dcn_manual", "conf_repo"})
public class ManualDcnDeploymentExecutorWorkflowTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @MockBean
    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;
    @MockBean
    private DcnRepositoryManager dcnRepositoryManager;

    private Identifier deploymentId = Identifier.newInstance("did");
    private Identifier clientId = Identifier.newInstance("cid");

    @Test
    public void shouldCompleteDcnWorkflowWithManualExecutor() throws Exception {
        when(appDeploymentRepositoryManager.loadClientIdByDeploymentId(any())).thenReturn(clientId);
        when(dcnRepositoryManager.loadCurrentState(clientId)).thenThrow(new InvalidClientIdException());
        eventPublisher.publishEvent(new AppRequestNewOrVerifyExistingDcnEvent(this, deploymentId));
        verify(appDeploymentRepositoryManager, timeout(1000)).loadAllWaitingForDcn(clientId);
    }

}
