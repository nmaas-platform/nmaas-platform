package net.geant.nmaas.portal.api.shell.connectors;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.utils.k8sclient.KubernetesClientConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KubernetesConnectorHelperTest {

    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);

    private final KubernetesClientConfigFactory configFactory = mock(KubernetesClientConfigFactory.class);

    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);

    private KubernetesConnectorHelper helper;

    @BeforeEach
    public void setup() {
        helper = new KubernetesConnectorHelper(appDeploymentRepositoryManager, configFactory, applicationInstanceService);

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

        KubernetesClient client = mock(KubernetesClient.class);
        PodList podList = mock(PodList.class);
        when(configFactory.getClient()).thenReturn(client);
        MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> pods = (MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>>)mock(MixedOperation.class);
        NonNamespaceOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> nsPods = (NonNamespaceOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>>)mock(NonNamespaceOperation.class);
        when(client.pods()).thenReturn(pods);
        when(pods.inNamespace("namespace")).thenReturn(nsPods);
        when(nsPods.list()).thenReturn(podList);

        Pod pod0 = mock(Pod.class);
        ObjectMeta pod0Meta = mock(ObjectMeta.class);
        when(pod0.getMetadata()).thenReturn(pod0Meta);
        when(pod0Meta.getName()).thenReturn("good-prefix-name-with-hash");
        when(pod0Meta.getLabels()).thenReturn(Collections.singletonMap("app", "good-prefix-name"));

        Pod pod1 = mock(Pod.class);
        ObjectMeta pod1Meta = mock(ObjectMeta.class);
        when(pod1.getMetadata()).thenReturn(pod1Meta);
        when(pod1Meta.getName()).thenReturn("bad-prefix-name-with-hash");

        Pod pod2 = mock(Pod.class);
        ObjectMeta pod2Meta = mock(ObjectMeta.class);
        when(pod2.getMetadata()).thenReturn(pod2Meta);
        when(pod2Meta.getName()).thenReturn("good-prefix-name-2-with-hash");
        when(pod2Meta.getLabels()).thenReturn(Collections.singletonMap("not-app-label", "good-prefix-name"));

        List<Pod> items = Arrays.asList(pod0, pod1, pod2);
        when(podList.getItems()).thenReturn(items);
    }

    @Test
    public void shouldReturnPodNamesWithPrefix() {
        Map<String, String> result = helper.getPodNamesForAppInstance(1L);
        assertEquals(2, result.size());
        assertEquals("good-prefix-name", result.get("good-prefix-name-with-hash"));
        assertEquals("good-prefix-name-2-with-hash", result.get("good-prefix-name-2-with-hash"));
    }

}
