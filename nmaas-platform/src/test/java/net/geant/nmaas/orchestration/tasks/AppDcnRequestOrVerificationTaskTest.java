package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.entities.DcnState;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.orchestration.tasks.app.AppDcnRequestOrVerificationTask;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEvent;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppDcnRequestOrVerificationTaskTest {

    private AppDeploymentRepositoryManager deployments = mock(AppDeploymentRepositoryManager.class);
    private DcnDeploymentProvider deploy = mock(DcnDeploymentProvider.class);

    private AppDcnRequestOrVerificationTask task;

    private static final String DOMAIN = "domain";
    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private AppRequestNewOrVerifyExistingDcnEvent event = new AppRequestNewOrVerifyExistingDcnEvent(this, deploymentId);

    @Before
    public void setup() {
        when(deployments.loadDomainByDeploymentId(deploymentId)).thenReturn("domain");
        task = new AppDcnRequestOrVerificationTask(deployments, deploy);
    }

    @Test
    public void shouldGenerateNewDcnDeploymentActionIfDcnNotExists() {
        when(deploy.checkState(DOMAIN)).thenReturn(DcnState.NONE);
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent, is(instanceOf(DcnVerifyRequestActionEvent.class)));
    }

    @Test
    public void shouldGenerateNewDcnDeploymentActionIfDcnRemoved() {
        when(deploy.checkState(DOMAIN)).thenReturn(DcnState.REMOVED);
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent, is(instanceOf(DcnVerifyRequestActionEvent.class)));
    }

    @Test
    public void shouldNotifyReadyForDeploymentState() {
        when(deploy.checkState(DOMAIN)).thenReturn(DcnState.DEPLOYED);
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent, is(instanceOf(NmServiceDeploymentStateChangeEvent.class)));
    }

    @Test
    public void shouldDoNothingWhenDcnCurrentlyProcessed() {
        when(deploy.checkState("domain")).thenReturn(DcnState.PROCESSED);
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent, is(nullValue()));
    }

}
