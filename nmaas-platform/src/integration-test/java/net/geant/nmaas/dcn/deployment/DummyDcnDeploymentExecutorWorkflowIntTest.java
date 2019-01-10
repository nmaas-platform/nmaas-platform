package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"env_kubernetes", "dcn_none"})
public class DummyDcnDeploymentExecutorWorkflowIntTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @MockBean
    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;
    @MockBean
    private DcnRepositoryManager dcnRepositoryManager;

    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("did");
    private static final String DOMAIN = "domain";

    @Test
    public void shouldCompleteDcnWorkflowWithDummyExecutor() {
        when(appDeploymentRepositoryManager.loadDomainByDeploymentId(any())).thenReturn(DOMAIN);
        when(dcnRepositoryManager.loadCurrentState(DOMAIN)).thenThrow(new InvalidDomainException());
        eventPublisher.publishEvent(new AppRequestNewOrVerifyExistingDcnEvent(this, DEPLOYMENT_ID));
        verify(appDeploymentRepositoryManager, timeout(1000)).loadAllWaitingForDcn(DOMAIN);
    }

}
