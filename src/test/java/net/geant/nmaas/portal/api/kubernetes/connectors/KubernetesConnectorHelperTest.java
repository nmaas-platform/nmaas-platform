package net.geant.nmaas.portal.api.kubernetes.connectors;

import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import net.geant.nmaas.kubernetes.DummyKubernetesApiClientService;
import net.geant.nmaas.kubernetes.remote.repositories.KClusterRepository;
import net.geant.nmaas.kubernetes.shell.KubernetesConnectorHelper;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KubernetesConnectorHelperTest {

    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);
    private final KClusterRepository kClusterRepository = mock(KClusterRepository.class);
    private final DummyKubernetesApiClientService kubernetesApiClientService = new DummyKubernetesApiClientService();

    private KubernetesConnectorHelper helper;

    @BeforeEach
    void setup() {
        kubernetesApiClientService.reset();
        helper = new KubernetesConnectorHelper(applicationInstanceService, appDeploymentRepositoryManager,
                kClusterRepository, kubernetesApiClientService);

        AppInstance appInstance = mock(AppInstance.class);
        when(applicationInstanceService.find(anyLong())).thenReturn(Optional.of(appInstance));
        Domain domain = mock(Domain.class);
        when(appInstance.getDomain()).thenReturn(domain);
        when(domain.getCodename()).thenReturn("namespace");
        Application application = mock(Application.class);
        when(appInstance.getApplication()).thenReturn(application);
        when(application.getAppDeploymentSpec()).thenReturn(AppDeploymentSpec.builder().allowSshAccess(true).build());

        Identifier appInstanceInternalId = mock(Identifier.class);
        when(appInstance.getInternalId()).thenReturn(appInstanceInternalId);

        AppDeployment appDeployment = mock(AppDeployment.class);
        when(appDeploymentRepositoryManager.load(appInstanceInternalId)).thenReturn(appDeployment);
        Identifier appInstanceDescriptiveDeploymentId = mock(Identifier.class);
        when(appDeployment.getDescriptiveDeploymentId()).thenReturn(appInstanceDescriptiveDeploymentId);
        when(appInstanceDescriptiveDeploymentId.getValue()).thenReturn("good-prefix");

        kubernetesApiClientService.setPods(null, "namespace", new PodListBuilder()
                .withItems(Arrays.asList(
                        new PodBuilder()
                                .withNewMetadata()
                                .withName("good-prefix-name-with-hash")
                                .addToLabels("app", "good-prefix-name")
                                .addToLabels("shell-access-enabled", "true")
                                .endMetadata()
                                .build(),
                        new PodBuilder()
                                .withNewMetadata()
                                .withName("bad-prefix-name-with-hash")
                                .endMetadata()
                                .build(),
                        new PodBuilder()
                                .withNewMetadata()
                                .withName("good-prefix-name-2-with-hash")
                                .addToLabels("not-app-label", "good-prefix-name")
                                .addToLabels("shell-access-enabled", "true")
                                .endMetadata()
                                .build(),
                        new PodBuilder()
                                .withNewMetadata()
                                .withName("good-prefix-name-3-with-hash")
                                .addToLabels("not-app-label", "good-prefix-name")
                                .addToLabels("shell-access-enabled", "false")
                                .endMetadata()
                                .build(),
                        new PodBuilder()
                                .withNewMetadata()
                                .withName("good-prefix-name-3-with-hash")
                                .addToLabels("not-app-label", "good-prefix-name")
                                .endMetadata()
                                .build()))
                .build());
    }

    //TODO: Missing DoneablePod on new version. Rewrite the setup stage

    @Test
    void shouldReturnPodNamesWithPrefix() {
        Map<String, String> result = helper.getPodNamesForAppInstance(1L);
        assertEquals(2, result.size());
        assertEquals("good-prefix-name", result.get("good-prefix-name-with-hash"));
        assertEquals("good-prefix-name-2-with-hash", result.get("good-prefix-name-2-with-hash"));
    }

}
