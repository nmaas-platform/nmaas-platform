package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class NmServiceConfigurationExecutorTest {

    private NmServiceConfigurationFilePreparer filePreparer = mock(NmServiceConfigurationFilePreparer.class);
    private GitConfigHandler configHandler = mock(GitConfigHandler.class);
    private JanitorService janitorService = mock(JanitorService.class);
    private ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private NmServiceConfigurationExecutor executor;

    @BeforeEach
    public void init() {
        executor = new NmServiceConfigurationExecutor(filePreparer, configHandler, janitorService, eventPublisher);
    }

    @Test
    public void shouldCreateRepositoryAndCommitFiles() {
        NmServiceDeployment nsd = NmServiceDeployment.builder()
                .configFileRepositoryRequired(true)
                .configUpdateEnabled(false)
                .build();
        when(filePreparer.generateAndStoreConfigFiles(any(), any(), any())).thenReturn(Arrays.asList("1", "2"));
        executor.configureNmService(nsd);
        verify(configHandler, times(1)).createUser(any(), any(), any(), any());
        verify(configHandler, times(1)).createRepository(any(), any());
        verify(configHandler, times(1)).commitConfigFiles(any(), any());
    }

    @Test
    public void shouldCreateRepositoryAndCommitFilesSinceUpdateEnabled() {
        NmServiceDeployment nsd = NmServiceDeployment.builder()
                .configFileRepositoryRequired(true)
                .configUpdateEnabled(true)
                .build();
        executor.configureNmService(nsd);
        verify(configHandler, times(1)).createUser(any(), any(), any(), any());
        verify(configHandler, times(1)).createRepository(any(), any());
        verify(configHandler, times(1)).commitConfigFiles(any(), any());
    }

    @Test
    public void shouldNotInteractWithGitLab() {
        NmServiceDeployment nsd = NmServiceDeployment.builder().configFileRepositoryRequired(false).build();
        executor.configureNmService(nsd);
        verifyZeroInteractions(configHandler);
    }

    @Test
    public void shouldCreateRepositoryButDoNotCommitSinceNotFileAndUpdateNotEnabled() {
        NmServiceDeployment nsd = NmServiceDeployment.builder().configFileRepositoryRequired(true).build();
        executor.configureNmService(nsd);
        verify(configHandler, times(1)).createUser(any(), any(), any(), any());
        verify(configHandler, times(1)).createRepository(any(), any());
        verifyNoMoreInteractions(configHandler);
    }

}
