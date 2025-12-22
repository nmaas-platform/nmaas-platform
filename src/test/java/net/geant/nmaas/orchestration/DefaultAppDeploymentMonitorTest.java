package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.configuration.exceptions.ConfigRepositoryAccessDetailsNotFoundException;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.exceptions.CouldNotRetrieveServiceAccessDetailsException;
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

class DefaultAppDeploymentMonitorTest {

    private final Identifier deploymentId = Identifier.newInstance("deploymentId");

    private final DefaultAppDeploymentRepositoryManager repositoryManager = mock(DefaultAppDeploymentRepositoryManager.class);
    private final NmServiceDeploymentProvider deploy = mock(NmServiceDeploymentProvider.class);
    private final NmServiceConfigurationProvider configure = mock(NmServiceConfigurationProvider.class);

    private DefaultAppDeploymentMonitor monitor;

    @BeforeEach
    void setup() {
        monitor = new DefaultAppDeploymentMonitor(repositoryManager, deploy, configure);
    }

    @Test
    void shouldReturnAllDeployments() {
        when(repositoryManager.loadAll()).thenReturn(Arrays.asList(new AppDeployment(), new AppDeployment()));
        List<AppDeployment> deployments = monitor.allDeployments();
        assertThat(deployments.size(), is(2));
    }

    @Test
    void shouldReturnState() {
        when(repositoryManager.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYED);
        AppLifecycleState state = monitor.state(deploymentId);
        assertThat(state, is(APPLICATION_DEPLOYED.lifecycleState()));
    }

    @Test
    void shouldReturnPreviousState() {
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
    void shouldReturnPreviousStateUnknown() {
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
    void shouldReturnUserAccessDetails() {
        when(repositoryManager.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYMENT_VERIFIED);
        when(deploy.serviceAccessDetails(deploymentId)).thenReturn(new AppUiAccessDetails());
        AppUiAccessDetails accessDetails = monitor.userAccessDetails(deploymentId);
        assertThat(accessDetails, is(notNullValue()));
    }

    @Test
    void shouldNotReturnUserAccessDetailsIfNotExist() {
        assertThrows(InvalidDeploymentIdException.class, () -> {
            when(repositoryManager.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYMENT_VERIFIED);
            when(deploy.serviceAccessDetails(deploymentId)).thenThrow(new CouldNotRetrieveServiceAccessDetailsException(""));
            AppUiAccessDetails accessDetails = monitor.userAccessDetails(deploymentId);
            assertThat(accessDetails, is(notNullValue()));
        });
    }

    @Test
    void shouldNotReturnUserAccessDetailsIfWrongState() {
        when(repositoryManager.loadState(deploymentId)).thenReturn(APPLICATION_DEPLOYED);
        assertThrows(InvalidAppStateException.class, () -> {
            monitor.userAccessDetails(deploymentId);
        });
    }

    @Test
    void shouldRetrieveConfigRepoAccessDetails() {
        when(configure.configRepositoryAccessDetails(deploymentId)).thenReturn(AppConfigRepositoryAccessDetails.of("testCloneURL"));
        AppConfigRepositoryAccessDetails repositoryAccessDetails = monitor.configRepositoryAccessDetails(deploymentId);
        assertEquals("testCloneURL", repositoryAccessDetails.getCloneUrl());
    }

    @Test
    void shouldNotRetrieveConfigRepoAccessDetailsIfNotExist() {
        when(configure.configRepositoryAccessDetails(deploymentId)).thenThrow(ConfigRepositoryAccessDetailsNotFoundException.class);
        assertThrows(InvalidDeploymentIdException.class, () -> {
            monitor.configRepositoryAccessDetails(deploymentId);
        });
    }

    @Test
    void shouldReturnAppDeploymentHistory() {
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
        assertThat(history.stream().map(AppDeploymentHistoryView::getCurrentState).toList(),
                contains(APPLICATION_DEPLOYED.lifecycleState().getUserFriendlyState(),
                        APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS.lifecycleState().getUserFriendlyState()));
    }
}
