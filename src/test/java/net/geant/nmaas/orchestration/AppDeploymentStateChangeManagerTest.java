package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodView;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceDeploymentState;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.events.app.AppDeployServiceActionEvent;
import net.geant.nmaas.orchestration.events.app.AppPrepareEnvironmentActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveDcnIfRequiredEvent;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyServiceActionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashSet;
import java.util.Optional;

import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_UPDATED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_RESTARTED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_UPGRADED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.MANAGEMENT_VPN_CONFIGURED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.REQUEST_VALIDATED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AppDeploymentStateChangeManagerTest {

    private DefaultAppDeploymentRepositoryManager deployments = mock(DefaultAppDeploymentRepositoryManager.class);
    private AppDeploymentMonitor monitor = mock(AppDeploymentMonitor.class);
    private ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

    private AppDeploymentStateChangeManager manager;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private NmServiceDeploymentStateChangeEvent event = mock(NmServiceDeploymentStateChangeEvent.class);

    @BeforeEach
    public void setup() {
        when(event.getDeploymentId()).thenReturn(deploymentId);
        manager = new AppDeploymentStateChangeManager(deployments, monitor, publisher);
    }

    @Test
    public void shouldNotTriggerAnyNewEventInNormalState() {
        when(deployments.loadState(deploymentId)).thenReturn(MANAGEMENT_VPN_CONFIGURED);
        when(event.getState()).thenReturn(NmServiceDeploymentState.CONFIGURATION_INITIATED);
        ApplicationEvent newEvent = manager.notifyStateChange(event);
        assertThat(newEvent, is(nullValue()));
        verify(deployments, times(1)).loadState(deploymentId);
        verify(deployments, times(1)).updateState(deploymentId, APPLICATION_CONFIGURATION_IN_PROGRESS);
    }

    @Test
    public void shouldTriggerNewEventInFailedState() {
        when(deployments.loadState(deploymentId)).thenReturn(APPLICATION_CONFIGURATION_IN_PROGRESS);
        when(deployments.load(deploymentId)).thenReturn(stubAppDeployment());
        when(event.getState()).thenReturn(NmServiceDeploymentState.CONFIGURATION_FAILED);
        when(event.getErrorMessage()).thenReturn("example error message");
        when(deployments.loadDomainName(deploymentId)).thenReturn("domainName");
        ApplicationEvent newEvent = manager.notifyStateChange(event);
        assertThat(newEvent, is(nullValue()));
        verify(deployments, times(1)).updateErrorMessage(any(Identifier.class), any(String.class));
        verify(publisher, times(1)).publishEvent(any(NotificationEvent.class));
    }

    @Test
    public void shouldTriggerNotificationEvent() {
        when(deployments.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS);
        when(deployments.load(deploymentId)).thenReturn(stubAppDeployment());
        when(monitor.userAccessDetails(deploymentId)).thenReturn(new AppUiAccessDetails(new HashSet<ServiceAccessMethodView>() {{
            add(new ServiceAccessMethodView(ServiceAccessMethodType.DEFAULT, "Default", "Web", "url"));
        }}));
        when(deployments.loadDomainName(deploymentId)).thenReturn("domainName");

        when(event.getState()).thenReturn(NmServiceDeploymentState.VERIFIED);
        ApplicationEvent newEvent = manager.notifyStateChange(event);
        assertThat(newEvent, is(nullValue()));
        verify(publisher, times(1)).publishEvent(any(NotificationEvent.class));
    }

    private AppDeployment stubAppDeployment() {
        return AppDeployment.builder()
                .deploymentId(deploymentId)
                .domain("domain")
                .owner("owner")
                .deploymentName("instance")
                .appName("app").build();
    }

    @Test
    public void shouldTriggerActionEventIfRequired() {
        Optional<ApplicationEvent> newEvent = manager.triggerActionEventIfRequired(deploymentId, APPLICATION_CONFIGURATION_IN_PROGRESS);
        assertThat(newEvent.isPresent(), is(false));
        newEvent = manager.triggerActionEventIfRequired(deploymentId, REQUEST_VALIDATED);
        assertThat(newEvent.isPresent(), is(true));
        assertThat(newEvent.get(), instanceOf(AppPrepareEnvironmentActionEvent.class));
        newEvent = manager.triggerActionEventIfRequired(deploymentId, DEPLOYMENT_ENVIRONMENT_PREPARED);
        assertThat(newEvent.isPresent(), is(true));
        assertThat(newEvent.get(), instanceOf(AppRequestNewOrVerifyExistingDcnEvent.class));
        newEvent = manager.triggerActionEventIfRequired(deploymentId, MANAGEMENT_VPN_CONFIGURED);
        assertThat(newEvent.isPresent(), is(true));
        assertThat(newEvent.get(), instanceOf(AppVerifyConfigurationActionEvent.class));
        newEvent = manager.triggerActionEventIfRequired(deploymentId, APPLICATION_CONFIGURED);
        assertThat(newEvent.isPresent(), is(true));
        assertThat(newEvent.get(), instanceOf(AppDeployServiceActionEvent.class));
        newEvent = manager.triggerActionEventIfRequired(deploymentId, APPLICATION_DEPLOYED);
        assertThat(newEvent.isPresent(), is(true));
        assertThat(newEvent.get(), instanceOf(AppVerifyServiceActionEvent.class));
        newEvent = manager.triggerActionEventIfRequired(deploymentId, APPLICATION_RESTARTED);
        assertThat(newEvent.isPresent(), is(true));
        assertThat(newEvent.get(), instanceOf(AppVerifyServiceActionEvent.class));
        newEvent = manager.triggerActionEventIfRequired(deploymentId, APPLICATION_CONFIGURATION_UPDATED);
        assertThat(newEvent.isPresent(), is(true));
        assertThat(newEvent.get(), instanceOf(AppVerifyServiceActionEvent.class));
        newEvent = manager.triggerActionEventIfRequired(deploymentId, APPLICATION_REMOVED);
        assertThat(newEvent.isPresent(), is(true));
        assertThat(newEvent.get(), instanceOf(AppRemoveDcnIfRequiredEvent.class));
        newEvent = manager.triggerActionEventIfRequired(deploymentId, APPLICATION_UPGRADED);
        assertThat(newEvent.isPresent(), is(true));
        assertThat(newEvent.get(), instanceOf(AppVerifyServiceActionEvent.class));
    }

}
