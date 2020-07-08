package net.geant.nmaas.portal.api.shell.connectors;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EnableRuleMigrationSupport
public class KubernetesConnectorHelperServerTest {

    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);

    private final KubernetesClientConfigFactory configFactory = mock(KubernetesClientConfigFactory.class);

    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);

    private KubernetesConnectorHelper helper;

    @Rule
    public KubernetesServer server = new KubernetesServer();

    @BeforeEach
    public void setup() {
        helper = new KubernetesConnectorHelper(appDeploymentRepositoryManager, configFactory, applicationInstanceService);

        AppInstance appInstance = mock(AppInstance.class);
        when(applicationInstanceService.find(anyLong())).thenReturn(Optional.of(appInstance));
        Domain domain = mock(Domain.class);
        when(appInstance.getDomain()).thenReturn(domain);
        when(domain.getCodename()).thenReturn("namespace");

        Identifier appInstanceInternalId = mock(Identifier.class);
        when(appInstance.getInternalId()).thenReturn(appInstanceInternalId);

        AppDeployment appDeployment = mock(AppDeployment.class);
        when(appDeploymentRepositoryManager.load(appInstanceInternalId)).thenReturn(appDeployment);
        Identifier appInstanceDescriptiveDeploymentId = mock(Identifier.class);
        when(appDeployment.getDescriptiveDeploymentId()).thenReturn(appInstanceDescriptiveDeploymentId);
        when(appInstanceDescriptiveDeploymentId.getValue()).thenReturn("good-prefix");

        Pod pod0 = new Pod();
        ObjectMeta pod0Meta = new ObjectMeta();
        pod0Meta.setName("good-prefix-name");
        pod0.setMetadata(pod0Meta);

        Pod pod1 = new Pod();
        ObjectMeta pod1Meta = new ObjectMeta();
        pod1Meta.setName("bad-prefix-name");
        pod1.setMetadata(pod0Meta);

        server.expect().withPath("/api/v1/namespaces/namespace/pods").andReturn(200, new PodListBuilder()
                .addNewItemLike(pod0).and()
                .addNewItemLike(pod1).and().build()).once();

        KubernetesClient client = server.getClient();
        when(configFactory.getClient()).thenReturn(client);
    }

    @Test
    public void shouldGetFilteredListOfPods() {
            List<String> result = helper.getPodNamesForAppInstance(1L);
            assertEquals(1, result.size());
            assertTrue(result.get(0).startsWith("good-prefix"));
    }
}
