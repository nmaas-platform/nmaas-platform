package net.geant.nmaas.portal.api.kubernetes.connectors;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import net.geant.nmaas.kubernetes.KubernetesConnectorHelper;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.kubernetes.KubernetesClientConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
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
        Map<String, String> pod0labels = new HashMap<>();
        pod0labels.put("app", "good-prefix-name");
        pod0labels.put("shell-access-enabled", "true");
        when(pod0Meta.getLabels()).thenReturn(pod0labels);

        Pod pod1 = mock(Pod.class);
        ObjectMeta pod1Meta = mock(ObjectMeta.class);
        when(pod1.getMetadata()).thenReturn(pod1Meta);
        when(pod1Meta.getName()).thenReturn("bad-prefix-name-with-hash");

        Pod pod2 = mock(Pod.class);
        ObjectMeta pod2Meta = mock(ObjectMeta.class);
        when(pod2.getMetadata()).thenReturn(pod2Meta);
        when(pod2Meta.getName()).thenReturn("good-prefix-name-2-with-hash");
        Map<String, String> pod2labels = new HashMap<>();
        pod2labels.put("not-app-label", "good-prefix-name");
        pod2labels.put("shell-access-enabled", "true");
        when(pod2Meta.getLabels()).thenReturn(pod2labels);

        Pod pod3 = mock(Pod.class);
        ObjectMeta pod3Meta = mock(ObjectMeta.class);
        when(pod3.getMetadata()).thenReturn(pod3Meta);
        when(pod3Meta.getName()).thenReturn("good-prefix-name-3-with-hash");
        Map<String, String> pod3labels = new HashMap<>();
        pod3labels.put("not-app-label", "good-prefix-name");
        pod3labels.put("shell-access-enabled", "false");
        when(pod3Meta.getLabels()).thenReturn(pod3labels);

        Pod pod4 = mock(Pod.class);
        ObjectMeta pod4Meta = mock(ObjectMeta.class);
        when(pod4.getMetadata()).thenReturn(pod4Meta);
        when(pod4Meta.getName()).thenReturn("good-prefix-name-3-with-hash");
        Map<String, String> pod4labels = new HashMap<>();
        pod4labels.put("not-app-label", "good-prefix-name");
        when(pod4Meta.getLabels()).thenReturn(pod4labels);

        List<Pod> items = Arrays.asList(pod0, pod1, pod2, pod3, pod4);
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
