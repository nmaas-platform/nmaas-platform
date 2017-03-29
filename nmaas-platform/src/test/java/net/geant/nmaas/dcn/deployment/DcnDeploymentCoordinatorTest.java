package net.geant.nmaas.dcn.deployment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DcnDeploymentCoordinatorTest {

    @Autowired
    private DcnDeploymentCoordinator coordinator;

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
    public void shouldCheckIfDcnStateNotificationIsRequired() {
        assertTrue(coordinator.statusUpdateShouldBeSentToListeners(DcnDeploymentState.DEPLOYED));
        assertTrue(coordinator.statusUpdateShouldBeSentToListeners(DcnDeploymentState.DEPLOYMENT_FAILED));
        assertTrue(coordinator.statusUpdateShouldBeSentToListeners(DcnDeploymentState.REMOVED));
        assertTrue(coordinator.statusUpdateShouldBeSentToListeners(DcnDeploymentState.REMOVAL_FAILED));
        assertFalse(coordinator.statusUpdateShouldBeSentToListeners(DcnDeploymentState.DEPLOYMENT_INITIATED));
    }

}
