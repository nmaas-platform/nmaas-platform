package net.geant.nmaas.orchestration;

import net.geant.nmaas.api.dto.applications.ServiceAccessMethodDto;
import net.geant.nmaas.api.dto.applications.ServiceAccessMethodTypeDto;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent.EventDetailType;
import net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentHistory;
import net.geant.nmaas.orchestration.events.app.AppDeployServiceActionEvent;
import net.geant.nmaas.orchestration.events.app.AppPrepareEnvironmentActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveDcnIfRequiredEvent;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.events.app.AppUpgradeCompleteEvent;
import net.geant.nmaas.orchestration.events.app.AppUpgradeFailedEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyServiceActionEvent;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.events.ApplicationDeployedEvent;
import net.geant.nmaas.portal.events.ApplicationRemovedEvent;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.persistence.repositories.WebhookEventRepository;
import net.geant.nmaas.portal.service.UserService;
import net.geant.nmaas.scheduling.ScheduleManager;
import net.geant.nmaas.webhooks.WebhooksEventListener;
import net.geant.nmaas.webhooks.jobs.AppDeploymentJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_UPDATED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVAL_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_RESTARTED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_UPGRADED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_UPGRADE_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.MANAGEMENT_VPN_CONFIGURED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.REQUEST_VALIDATED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppDeploymentStateChangeManagerTest {

    private final Identifier deploymentId = Identifier.newInstance("deploymentId");
    private final NmServiceDeploymentStateChangeEvent event = mock(NmServiceDeploymentStateChangeEvent.class);
    private final DefaultAppDeploymentRepositoryManager deployments = mock(DefaultAppDeploymentRepositoryManager.class);
    private final UserService userService = mock(UserService.class);
    private final AppDeploymentMonitor monitor = mock(AppDeploymentMonitor.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final WebhookEventRepository webhookEventRepository = mock(WebhookEventRepository.class);
    private final AppDeploymentRepository appDeploymentRepository = mock(AppDeploymentRepository.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final ScheduleManager scheduleManager = mock(ScheduleManager.class);

    private AppDeploymentStateChangeManager manager;
    private WebhooksEventListener listener;

    @BeforeEach
    void setup() {
        when(event.getDeploymentId()).thenReturn(deploymentId);
        manager = new AppDeploymentStateChangeManager(deployments, monitor, publisher, userService);
        listener = new WebhooksEventListener(webhookEventRepository, appDeploymentRepository, scheduleManager, modelMapper);
    }

    @Test
    void shouldNotTriggerAnyNewEventInNormalState() {
        when(deployments.loadState(deploymentId)).thenReturn(MANAGEMENT_VPN_CONFIGURED);
        when(event.getState()).thenReturn(ServiceDeploymentState.CONFIGURATION_INITIATED);
        ApplicationEvent newEvent = manager.notifyStateChange(event);
        assertThat(newEvent, is(nullValue()));
        verify(deployments, times(1)).loadState(deploymentId);
        verify(deployments, times(1)).updateState(deploymentId, APPLICATION_CONFIGURATION_IN_PROGRESS);
    }

    @Test
    void shouldTriggerNewEventInFailedState() {
        when(deployments.loadState(deploymentId)).thenReturn(APPLICATION_CONFIGURATION_IN_PROGRESS);
        when(deployments.load(deploymentId)).thenReturn(stubAppDeployment());
        when(event.getState()).thenReturn(ServiceDeploymentState.CONFIGURATION_FAILED);
        when(event.getErrorMessage()).thenReturn("example error message");
        when(deployments.loadDomainName(deploymentId)).thenReturn("domainName");
        ApplicationEvent newEvent = manager.notifyStateChange(event);
        assertThat(newEvent, is(nullValue()));
        verify(deployments, times(1)).updateErrorMessage(any(Identifier.class), any(String.class));
        verify(publisher, times(1)).publishEvent(any(NotificationEvent.class));
    }

    @Test
    void shouldTriggerNewEventInDeployedVerifiedState() {
        when(deployments.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS);
        when(deployments.load(deploymentId)).thenReturn(stubAppDeployment());
        when(deployments.isFirstTimeDeployment(deploymentId)).thenReturn(true);
        when(event.getState()).thenReturn(ServiceDeploymentState.VERIFIED);

        when(monitor.userAccessDetails(deploymentId)).thenReturn(
                new AppUiAccessDetails(
                        new HashSet<>() {{
                            add(new ServiceAccessMethodDto(ServiceAccessMethodTypeDto.DEFAULT, "Default", "Web", "url"));
                        }}
                )
        );
        when(deployments.loadDomainName(deploymentId)).thenReturn("domainName");

        ApplicationEvent newEvent = manager.notifyStateChange(event);

        assertThat(newEvent, is(nullValue()));
        verify(publisher, times(1)).publishEvent(any(NotificationEvent.class));
        verify(publisher, times(1)).publishEvent(any(ApplicationDeployedEvent.class));
    }

    @Test
    void shouldTriggerAppDeploymentJobWhenWebhookMatchesDomain() {
        when(appDeploymentRepository.findByDeploymentId(deploymentId)).thenReturn(Optional.ofNullable(stubAppDeployment()));
        ApplicationDeployedEvent appDeployedEvent = new ApplicationDeployedEvent(this, deploymentId.value());
        when(webhookEventRepository.findIdByEventTypeAndDomain(WebhookEventType.APPLICATION_DEPLOYMENT, "domain"))
                .thenReturn(java.util.List.of(1L));

        listener.trigger(appDeployedEvent);
        verify(scheduleManager, times(1)).createOneTimeJob(
                eq(AppDeploymentJob.class),
                anyString(),
                anyMap()
        );
    }

    @Test
    void shouldNotTriggerAppDeploymentJobWhenWebhookForDifferentDomain() {
        when(appDeploymentRepository.findByDeploymentId(deploymentId)).thenReturn(Optional.ofNullable(stubAppDeployment()));
        ApplicationDeployedEvent appDeployedEvent = new ApplicationDeployedEvent(this, deploymentId.value());
        when(webhookEventRepository.findIdByEventTypeAndDomain(WebhookEventType.APPLICATION_DEPLOYMENT, "domain1"))
                .thenReturn(java.util.List.of());

        listener.trigger(appDeployedEvent);
        verify(scheduleManager, never()).createOneTimeJob(
                eq(AppDeploymentJob.class),
                anyString(),
                anyMap()
        );
    }

    @Test
    void shouldTriggerNotificationEvent() {
        when(deployments.isFirstTimeDeployment(deploymentId)).thenReturn(true);
        when(deployments.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS);
        when(deployments.load(deploymentId)).thenReturn(stubAppDeployment());
        when(monitor.userAccessDetails(deploymentId)).thenReturn(
                new AppUiAccessDetails(
                        new HashSet<>() {{
                            add(new ServiceAccessMethodDto(ServiceAccessMethodTypeDto.DEFAULT, "Default", "Web", "url"));
                        }}
                )
        );
        when(deployments.loadDomainName(deploymentId)).thenReturn("domainName");
        when(event.getState()).thenReturn(ServiceDeploymentState.VERIFIED);

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
    void shouldTriggerActionEventIfRequired() {
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

    @Test
    void shouldProcessApplicationUpgradedState() {
        when(deployments.loadState(deploymentId)).thenReturn(APPLICATION_UPGRADE_IN_PROGRESS);
        when(deployments.load(deploymentId)).thenReturn(stubAppDeployment());
        when(event.getState()).thenReturn(ServiceDeploymentState.UPGRADED);
        when(event.getDetail(EventDetailType.NEW_APPLICATION_ID)).thenReturn("10");
        when(event.getDetail(EventDetailType.UPGRADE_TRIGGER_TYPE)).thenReturn(AppUpgradeMode.MANUAL.toString());
        manager.notifyStateChange(event);
        verify(deployments).updateApplicationId(deploymentId, Identifier.newInstance(10L));
        verify(publisher).publishEvent(any(AppUpgradeCompleteEvent.class));
    }

    @Test
    void shouldProcessApplicationUpgradeFailedState() {
        when(deployments.loadState(deploymentId)).thenReturn(APPLICATION_UPGRADE_IN_PROGRESS);
        when(deployments.loadDomainName(deploymentId)).thenReturn("domainName");
        when(deployments.load(deploymentId)).thenReturn(stubAppDeployment());
        when(event.getState()).thenReturn(ServiceDeploymentState.UPGRADE_FAILED);
        when(event.getDetail(EventDetailType.NEW_APPLICATION_ID)).thenReturn("10");
        when(event.getDetail(EventDetailType.UPGRADE_TRIGGER_TYPE)).thenReturn(AppUpgradeMode.MANUAL.toString());
        when(event.getErrorMessage()).thenReturn("example error message");
        manager.notifyStateChange(event);
        verify(publisher).publishEvent(any(AppUpgradeFailedEvent.class));
    }

    @Test
    void shouldSendEmailWithoutHistoryRunning() {
        when(deployments.isFirstTimeDeployment(deploymentId)).thenReturn(true);
        when(deployments.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS);
        when(deployments.loadDomainName(deploymentId)).thenReturn("domainName");
        when(deployments.load(deploymentId)).thenReturn(stubAppDeployment());
        when(monitor.userAccessDetails(deploymentId)).thenReturn(new AppUiAccessDetails(new HashSet<>() {{
            add(new ServiceAccessMethodDto(ServiceAccessMethodTypeDto.DEFAULT, "Default", "Web", "url"));
        }}));
        when(event.getDetail(EventDetailType.NEW_APPLICATION_ID)).thenReturn("10");
        when(event.getState()).thenReturn(ServiceDeploymentState.VERIFIED);

        manager.notifyStateChange(event);

        verify(publisher).publishEvent(any(NotificationEvent.class));
    }

    @Test
    void shouldNotSendEmailWithoutHistoryRunning() {
        when(deployments.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS);
        when(deployments.loadDomainName(deploymentId)).thenReturn("domainName");
        when(deployments.load(deploymentId)).thenReturn(stubAppDeployment());
        when(monitor.userAccessDetails(deploymentId)).thenReturn(new AppUiAccessDetails(new HashSet<ServiceAccessMethodDto>() {{
            add(new ServiceAccessMethodDto(ServiceAccessMethodTypeDto.DEFAULT, "Default", "Web", "url"));
        }}));
        AppDeploymentHistory history = new AppDeploymentHistory(1L, stubAppDeployment(), new Date(), APPLICATION_DEPLOYED, APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS, new User("user1"));
        AppDeploymentHistory history2 = new AppDeploymentHistory(2L, stubAppDeployment(), new Date(), APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS, APPLICATION_DEPLOYMENT_VERIFIED, new User("user1"));
        // added second time as "current" re-deployment, because current state is not added by `update state` function
        AppDeploymentHistory history3 = new AppDeploymentHistory(3L, stubAppDeployment(), new Date(), APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS, APPLICATION_DEPLOYMENT_VERIFIED, new User("user1"));

        when(deployments.loadStateHistory(deploymentId)).thenReturn(java.util.List.of(history, history2, history3));
        when(event.getDetail(EventDetailType.NEW_APPLICATION_ID)).thenReturn("10");
        when(event.getState()).thenReturn(ServiceDeploymentState.VERIFIED);
        manager.notifyStateChange(event);
        verify(publisher, never()).publishEvent(any(NotificationEvent.class));
    }

    @Test
    void shouldTriggerNewEventInRemovedState() {
        when(deployments.loadState(deploymentId)).thenReturn(APPLICATION_REMOVAL_IN_PROGRESS);
        when(deployments.load(deploymentId)).thenReturn(stubAppDeployment());
        when(deployments.isFirstTimeDeployment(deploymentId)).thenReturn(true);
        when(event.getState()).thenReturn(ServiceDeploymentState.REMOVED);

        manager.notifyStateChange(event);

        verify(publisher, times(1)).publishEvent(any(ApplicationRemovedEvent.class));
    }
}