package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-engine.properties")
public class DcnDeploymentCoordinatorTest {

    @Autowired
    private DcnDeploymentCoordinator coordinator;
    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;

    @Test
    public void shouldReturnProperErrorState() {
        assertThat(coordinator.deploymentOrRemovalFailureDependingOnLastState(DcnDeploymentState.DEPLOYMENT_INITIATED),
                equalTo(DcnDeploymentState.DEPLOYMENT_FAILED));
        assertThat(coordinator.deploymentOrRemovalFailureDependingOnLastState(DcnDeploymentState.REMOVAL_INITIATED),
                equalTo(DcnDeploymentState.REMOVAL_FAILED));
        assertThat(coordinator.deploymentOrRemovalFailureDependingOnLastState(DcnDeploymentState.DEPLOYED),
                equalTo(DcnDeploymentState.ERROR));
    }

    @Test
    public void shouldReadCorrectAnsibleDockerApiUrlFromProperties() {
        assertThat(coordinator.getAnsibleDockerApiUrl(), equalTo("http://192.168.1.1:2375"));
    }

    @Test
    public void shouldCheckCurrentDcnState() throws InvalidClientIdException {
        Identifier clientId = Identifier.newInstance("id");
        assertThat(coordinator.checkState(clientId), equalTo(DcnState.NONE));
        DcnInfo dcnInfo = new DcnInfo();
        dcnInfo.setClientId(Identifier.newInstance("id2"));
        dcnInfo.setName("name");
        dcnRepositoryManager.storeDcnInfo(dcnInfo);
        assertThat(coordinator.checkState(clientId), equalTo(DcnState.NONE));
        dcnInfo.setClientId(Identifier.newInstance("id"));
        dcnInfo.setName("name2");
        dcnRepositoryManager.storeDcnInfo(dcnInfo);
        assertThat(coordinator.checkState(clientId), equalTo(DcnState.PROCESSED));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, clientId, DcnDeploymentState.VERIFIED));
        assertThat(coordinator.checkState(clientId), equalTo(DcnState.DEPLOYED));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, clientId, DcnDeploymentState.REMOVED));
        assertThat(coordinator.checkState(clientId), equalTo(DcnState.REMOVED));
    }

}
