package net.geant.nmaas.nmservice.configuration.api;

import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationProvider;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.entities.ServiceDeploymentState;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class GitLabWebhookControllerIntTest {

    private final KubernetesRepositoryManager repositoryManager = mock(KubernetesRepositoryManager.class);
    private final NmServiceConfigurationProvider configurationProvider = mock(NmServiceConfigurationProvider.class);

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new GitLabWebhookController(repositoryManager, configurationProvider))
                .addPlaceholderValue("nmaas.api.version", "v1")
                .build();
    }

    @Test
    void shouldNotTriggerReloadInIncorrectState() throws Exception {
        KubernetesNmServiceInfo serviceInfo = new KubernetesNmServiceInfo();
        serviceInfo.setDescriptiveDeploymentId(Identifier.newInstance("descriptiveDeploymentId"));
        serviceInfo.setState(ServiceDeploymentState.VERIFICATION_INITIATED);
        when(repositoryManager.loadServiceByGitLabProjectWebhookId("webhookId")).thenReturn(serviceInfo);
        mvc.perform(post("/api/v1/gitlab/webhooks/webhookId")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verifyNoMoreInteractions(configurationProvider);
    }

    @Test
    void shouldTriggerReloadInRunningState() throws Exception {
        KubernetesNmServiceInfo serviceInfo = new KubernetesNmServiceInfo();
        serviceInfo.setDescriptiveDeploymentId(Identifier.newInstance("descriptiveDeploymentId"));
        serviceInfo.setState(ServiceDeploymentState.VERIFIED);
        when(repositoryManager.loadServiceByGitLabProjectWebhookId("webhookId")).thenReturn(serviceInfo);
        mvc.perform(post("/api/v1/gitlab/webhooks/webhookId")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(configurationProvider, times(1)).reloadNmService(any());
    }

    @Test
    void shouldTriggerReloadInVerificationFailedState() throws Exception {
        KubernetesNmServiceInfo serviceInfo = new KubernetesNmServiceInfo();
        serviceInfo.setDescriptiveDeploymentId(Identifier.newInstance("descriptiveDeploymentId"));
        serviceInfo.setState(ServiceDeploymentState.VERIFICATION_FAILED);
        when(repositoryManager.loadServiceByGitLabProjectWebhookId("webhookId")).thenReturn(serviceInfo);
        mvc.perform(post("/api/v1/gitlab/webhooks/webhookId")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(configurationProvider, times(1)).reloadNmService(any());
    }

}
