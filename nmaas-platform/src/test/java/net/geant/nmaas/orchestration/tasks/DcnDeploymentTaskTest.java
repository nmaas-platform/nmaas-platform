package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.dcn.deployment.DcnDeploymentCoordinator;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.orchestration.events.dcn.DcnDeployActionEvent;
import net.geant.nmaas.orchestration.tasks.dcn.DcnDeploymentTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DcnDeploymentTaskTest {

    @Autowired
    private DcnDeploymentTask task;
    @Autowired
    private ApplicationEventPublisher publisher;
    @MockBean
    private DcnDeploymentCoordinator deploymentCoordinator;

    @Test
    public void shouldNotInvokeDcnDeploymentWhenReadingPropertiesFromFile() throws CouldNotDeployDcnException {
        publisher.publishEvent(new DcnDeployActionEvent(this, null));
        verify(deploymentCoordinator, times(0)).deployDcn(any());
    }

}
