package net.geant.nmaas.orchestration.tasks.app;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.DcnDeploymentProvidersManager;
import net.geant.nmaas.dcn.deployment.entities.DcnState;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.orchestration.DefaultAppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyRequestActionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppDcnRequestOrVerificationTaskTest {

    private DefaultAppDeploymentRepositoryManager deployments = mock(DefaultAppDeploymentRepositoryManager.class);
    private DcnDeploymentProvidersManager deploy = mock(DcnDeploymentProvidersManager.class);
    private DcnDeploymentProvider deploymentProvider = mock(DcnDeploymentProvider.class);


    private AppDcnRequestOrVerificationTask task;

    private static final String DOMAIN = "domain";
    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private AppRequestNewOrVerifyExistingDcnEvent event = new AppRequestNewOrVerifyExistingDcnEvent(this, deploymentId);

    @BeforeEach
    public void setup() {
        when(deployments.loadDomain(deploymentId)).thenReturn("domain");
        when(deploy.getDcnDeploymentProvider(any())).thenReturn(deploymentProvider);
        task = new AppDcnRequestOrVerificationTask(deployments, deploy);
    }

    @Test
    public void shouldGenerateNewDcnDeploymentActionIfDcnNotExists() {
        when(deploymentProvider.checkState(DOMAIN)).thenReturn(DcnState.NONE);
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent, is(instanceOf(DcnVerifyRequestActionEvent.class)));
    }

    @Test
    public void shouldGenerateNewDcnDeploymentActionIfDcnRemoved() {
        when(deploymentProvider.checkState(DOMAIN)).thenReturn(DcnState.REMOVED);
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent, is(instanceOf(DcnVerifyRequestActionEvent.class)));
    }

    @Test
    public void shouldNotifyReadyForDeploymentState() {
        when(deploymentProvider.checkState(DOMAIN)).thenReturn(DcnState.DEPLOYED);
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent, is(instanceOf(NmServiceDeploymentStateChangeEvent.class)));
    }

    @Test
    public void shouldDoNothingWhenDcnCurrentlyProcessed() {
        when(deploymentProvider.checkState("domain")).thenReturn(DcnState.PROCESSED);
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent, is(nullValue()));
    }

}
