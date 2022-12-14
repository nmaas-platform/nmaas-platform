package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigRepositoryAccessDetailsNotFoundException;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRetrieveNmServiceAccessDetailsException;
import net.geant.nmaas.orchestration.api.model.AppDeploymentHistoryView;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentHistory;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYED;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYMENT_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS;
import static net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultAppDeploymentMonitorTest {

    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    private DefaultAppDeploymentRepositoryManager repositoryManager = mock(DefaultAppDeploymentRepositoryManager.class);
    private NmServiceDeploymentProvider deploy = mock(NmServiceDeploymentProvider.class);
    private NmServiceConfigurationProvider configure = mock(NmServiceConfigurationProvider.class);

    private DefaultAppDeploymentMonitor monitor;

    @BeforeEach
    public void setup() {
        monitor = new DefaultAppDeploymentMonitor(repositoryManager, deploy, configure);
    }

    @Test
    public void shouldReturnAllDeployments() {
        when(repositoryManager.loadAll()).thenReturn(Arrays.asList(new AppDeployment(), new AppDeployment()));
        List<AppDeployment> deployments = monitor.allDeployments();
        assertThat(deployments.size(), is(2));
    }

    @Test
    public void shouldReturnState() {
        when(repositoryManager.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYED);
        AppLifecycleState state = monitor.state(deploymentId);
        assertThat(state, is(APPLICATION_DEPLOYED.lifecycleState()));
    }

    @Test
    public void shouldReturnPreviousState() {
        List<AppDeploymentHistory> stubHistory = Arrays.asList(
                AppDeploymentHistory.builder()
                        .currentState(APPLICATION_DEPLOYED)
                        .previousState(APPLICATION_DEPLOYMENT_IN_PROGRESS)
                        .timestamp(Date.from(Instant.now().minusSeconds(60)))
                        .build(),
                AppDeploymentHistory.builder()
                        .currentState(APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS)
                        .previousState(APPLICATION_DEPLOYED)
                        .timestamp(Date.from(Instant.now()))
                        .build()
                );
        when(repositoryManager.loadStateHistory(deploymentId)).thenReturn(stubHistory);
        AppLifecycleState state = monitor.previousState(deploymentId);
        assertThat(state, is(AppLifecycleState.APPLICATION_DEPLOYED));
    }

    @Test
    public void shouldReturnPreviousStateUnknown() {
        List<AppDeploymentHistory> stubHistory = Collections.singletonList(
                AppDeploymentHistory.builder()
                        .currentState(APPLICATION_DEPLOYED)
                        .timestamp(Date.from(Instant.now().minusSeconds(60)))
                        .build()
        );
        when(repositoryManager.loadStateHistory(deploymentId)).thenReturn(stubHistory);
        AppLifecycleState state = monitor.previousState(deploymentId);
        assertThat(state, is(AppLifecycleState.UNKNOWN));
    }

    @Test
    public void shouldReturnUserAccessDetails() {
        when(repositoryManager.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYMENT_VERIFIED);
        when(deploy.serviceAccessDetails(deploymentId)).thenReturn(new AppUiAccessDetails());
        AppUiAccessDetails accessDetails = monitor.userAccessDetails(deploymentId);
        assertThat(accessDetails, is(notNullValue()));
    }

    @Test
    public void shouldNotReturnUserAccessDetailsIfNotExist() {
        assertThrows(InvalidDeploymentIdException.class, () -> {
            when(repositoryManager.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYMENT_VERIFIED);
            when(deploy.serviceAccessDetails(deploymentId)).thenThrow(new CouldNotRetrieveNmServiceAccessDetailsException(""));
            AppUiAccessDetails accessDetails = monitor.userAccessDetails(deploymentId);
            assertThat(accessDetails, is(notNullValue()));
        });
    }

    @Test
    public void shouldNotReturnUserAccessDetailsIfWrongState() {
        assertThrows(InvalidAppStateException.class, () -> {
            when(repositoryManager.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYED);
            monitor.userAccessDetails(deploymentId);
        });
    }

    @Test
    public void shouldRetrieveConfigRepoAccessDetails() {
        when(configure.configRepositoryAccessDetails(deploymentId)).thenReturn(AppConfigRepositoryAccessDetails.of("testCloneURL"));
        AppConfigRepositoryAccessDetails repositoryAccessDetails = monitor.configRepositoryAccessDetails(deploymentId);
        assertEquals("testCloneURL", repositoryAccessDetails.getCloneUrl());
    }

    @Test
    public void shouldNotRetrieveConfigRepoAccessDetailsIfNotExist() {
        assertThrows(InvalidDeploymentIdException.class, () -> {
            when(configure.configRepositoryAccessDetails(deploymentId)).thenThrow(ConfigRepositoryAccessDetailsNotFoundException.class);
            monitor.configRepositoryAccessDetails(deploymentId);
        });
    }

    @Test
    public void shouldReturnAppDeploymentHistory() {
        List<AppDeploymentHistory> stubHistory = Arrays.asList(
                AppDeploymentHistory.builder()
                        .currentState(APPLICATION_DEPLOYED)
                        .previousState(APPLICATION_DEPLOYMENT_IN_PROGRESS)
                        .timestamp(Date.from(Instant.now().minusSeconds(60)))
                        .build(),
                AppDeploymentHistory.builder()
                        .currentState(APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS)
                        .previousState(APPLICATION_DEPLOYED)
                        .timestamp(Date.from(Instant.now()))
                        .build()
        );
        when(repositoryManager.loadStateHistory(deploymentId)).thenReturn(stubHistory);
        List<AppDeploymentHistoryView> history = monitor.appDeploymentHistory(deploymentId);
        assertThat(history.size(), is(2));
        assertThat(history.stream().map(AppDeploymentHistoryView::getCurrentState).collect(Collectors.toList()),
                contains(APPLICATION_DEPLOYED.lifecycleState().getUserFriendlyState(),
                        APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS.lifecycleState().getUserFriendlyState()));
    }
}
