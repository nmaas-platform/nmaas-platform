package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.DcnDeploymentCoordinator;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployActionEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {"dcn.deployment.mode = none"})
public class DcnDeploymentInNoneModeTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @MockBean
    private DcnDeploymentCoordinator deploymentCoordinator;

    @Test
    public void shouldNotInvokeDcnDeploymentInNoneMode() throws CouldNotDeployDcnException, InterruptedException {
        publisher.publishEvent(new DcnDeployActionEvent(this, null));
        Thread.sleep(300);
        verifyNoMoreInteractions(deploymentCoordinator);
    }

}
