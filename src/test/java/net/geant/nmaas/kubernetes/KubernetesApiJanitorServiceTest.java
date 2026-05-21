package net.geant.nmaas.kubernetes;

import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.nmservice.configuration.ConfigFile;
import net.geant.nmaas.orchestration.AppComponentDetails;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KubernetesApiJanitorServiceTest {

    private static final String DOMAIN = "domain";
    private static final String LOCAL_NAMESPACE = "local-namespace";
    private static final String REMOTE_NAMESPACE = "remote-namespace";

    private final KubernetesClusterNamespaceService namespaceService = mock(KubernetesClusterNamespaceService.class);
    private final KubernetesApiClientService kubernetesApiClientService = mock(KubernetesApiClientService.class);

    private KubernetesApiJanitorService janitorService;

    @BeforeEach
    void setup() {
        janitorService = new KubernetesApiJanitorService(namespaceService, kubernetesApiClientService);
        when(namespaceService.namespace(DOMAIN)).thenReturn(LOCAL_NAMESPACE);
    }

    @Test
    void shouldUseLocalNamespaceForLocalClusterOperations() {
        Identifier deploymentId = Identifier.newInstance("deploymentId");
        when(kubernetesApiClientService.getDeployments(null, LOCAL_NAMESPACE, deploymentId.value())).thenReturn(List.of());
        when(kubernetesApiClientService.getStatefulSets(null, LOCAL_NAMESPACE, deploymentId.value())).thenReturn(List.of());

        boolean ready = janitorService.checkIfReady(null, deploymentId, DOMAIN);

        assertThat(ready).isTrue();
        verify(namespaceService, times(1)).namespace(DOMAIN);
        verify(kubernetesApiClientService, times(1)).getDeployments(null, LOCAL_NAMESPACE, deploymentId.value());
        verify(kubernetesApiClientService, times(1)).getStatefulSets(null, LOCAL_NAMESPACE, deploymentId.value());
    }

    @Test
    void shouldUseRemoteNamespaceForRemoteClusterOperations() {
        KCluster remoteCluster = KCluster.builder().id(1L).build();
        Identifier deploymentId = Identifier.newInstance("deploymentId");
        when(namespaceService.namespace(remoteCluster, DOMAIN)).thenReturn(REMOTE_NAMESPACE);
        when(kubernetesApiClientService.getDeployments(remoteCluster, REMOTE_NAMESPACE, deploymentId.value()))
                .thenReturn(List.of(new DeploymentBuilder()
                        .withNewSpec().withReplicas(1).endSpec()
                        .withNewStatus().withReadyReplicas(1).endStatus()
                        .build()));
        when(kubernetesApiClientService.getStatefulSets(remoteCluster, REMOTE_NAMESPACE, deploymentId.value()))
                .thenReturn(List.of(new StatefulSetBuilder()
                        .withNewSpec().withReplicas(1).endSpec()
                        .withNewStatus().withReadyReplicas(1).endStatus()
                        .build()));
        Service service = new ServiceBuilder()
                .withNewStatus()
                .withNewLoadBalancer()
                .addNewIngress()
                .withIp("192.0.2.10")
                .endIngress()
                .endLoadBalancer()
                .endStatus()
                .build();
        when(kubernetesApiClientService.getService(remoteCluster, REMOTE_NAMESPACE, deploymentId.value()))
                .thenReturn(service);
        when(kubernetesApiClientService.getPods(remoteCluster, REMOTE_NAMESPACE))
                .thenReturn(new PodListBuilder()
                        .addNewItem()
                        .withNewMetadata()
                        .withName("deploymentId-abc")
                        .endMetadata()
                        .withNewSpec()
                        .addNewContainer()
                        .withName("app")
                        .endContainer()
                        .endSpec()
                        .endItem()
                        .build());
        when(kubernetesApiClientService.getLogs(remoteCluster, REMOTE_NAMESPACE, "deploymentId-abc", "app", 100))
                .thenReturn("log-line");
        ConfigFile configFile = ConfigFile.builder()
                .fileName("app.yml")
                .filePath("/etc/app.yml")
                .fileContent("value: test")
                .build();

        boolean ready = janitorService.checkIfReady(remoteCluster, deploymentId, DOMAIN);
        String serviceIp = janitorService.retrieveServiceIp(remoteCluster, deploymentId, DOMAIN);
        boolean serviceExists = janitorService.checkServiceExists(remoteCluster, deploymentId, DOMAIN);
        List<AppComponentDetails> podNames = janitorService.getPodNames(remoteCluster, deploymentId, DOMAIN);
        List<String> podLogs = janitorService.getPodLogs(remoteCluster, "deploymentId-abc", "app", DOMAIN, 100);
        janitorService.createOrReplaceConfigMaps(remoteCluster, deploymentId, DOMAIN, List.of(configFile));
        janitorService.deleteConfigMapIfExists(remoteCluster, deploymentId, DOMAIN);
        janitorService.createOrReplaceBasicAuth(remoteCluster, deploymentId, DOMAIN, "user", "password");
        janitorService.deleteBasicAuthIfExists(remoteCluster, deploymentId, DOMAIN);
        janitorService.deleteTlsIfExists(remoteCluster, deploymentId, DOMAIN);

        assertThat(ready).isTrue();
        assertThat(serviceIp).isEqualTo("192.0.2.10");
        assertThat(serviceExists).isTrue();
        assertThat(podNames).extracting(AppComponentDetails::getName).containsExactly("deploymentId-abc");
        assertThat(podLogs).containsExactly("log-line");
        verify(namespaceService, times(10)).namespace(remoteCluster, DOMAIN);
        verify(namespaceService, never()).namespace(DOMAIN);
        verify(kubernetesApiClientService, times(1)).getDeployments(remoteCluster, REMOTE_NAMESPACE, deploymentId.value());
        verify(kubernetesApiClientService, times(1)).getStatefulSets(remoteCluster, REMOTE_NAMESPACE, deploymentId.value());
        verify(kubernetesApiClientService, times(2)).getService(remoteCluster, REMOTE_NAMESPACE, deploymentId.value());
        verify(kubernetesApiClientService, times(1)).getPods(remoteCluster, REMOTE_NAMESPACE);
        verify(kubernetesApiClientService, times(1)).getLogs(remoteCluster, REMOTE_NAMESPACE, "deploymentId-abc", "app", 100);
        verify(kubernetesApiClientService, times(1)).createOrReplaceConfigMap(remoteCluster, REMOTE_NAMESPACE, "deploymentId-etc", List.of(configFile));
        verify(kubernetesApiClientService, times(1)).deleteConfigMapIfExists(remoteCluster, REMOTE_NAMESPACE, deploymentId.value());
        verify(kubernetesApiClientService, times(1)).createOrReplaceBasicAuth(remoteCluster, REMOTE_NAMESPACE, "deploymentId-auth", "dXNlcjpwYXNzd29yZA==");
        verify(kubernetesApiClientService, times(1)).deleteSecretIfExists(remoteCluster, REMOTE_NAMESPACE, "deploymentId-auth");
        verify(kubernetesApiClientService, times(1)).deleteSecretIfExists(remoteCluster, REMOTE_NAMESPACE, "deploymentId-tls");
    }

}
