package net.geant.nmaas.portal.api.shell.connectors;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.utils.k8sclient.KubernetesClientConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Log4j2
public class KubernetesConnectorHelper {

    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager;

    private final KubernetesClientConfigFactory configFactory;

    private final ApplicationInstanceService applicationInstanceService;

    @Autowired
    public KubernetesConnectorHelper(AppDeploymentRepositoryManager appDeploymentRepositoryManager,
                                     KubernetesClientConfigFactory configFactory,
                                     ApplicationInstanceService applicationInstanceService) {
        this.appDeploymentRepositoryManager = appDeploymentRepositoryManager;
        this.configFactory = configFactory;
        this.applicationInstanceService = applicationInstanceService;
    }

    public List<String> getPodNamesForAppInstance(Long appInstanceId) {
        return this.getPodNamesForAppInstance(
                this.applicationInstanceService.find(appInstanceId).orElseThrow(
                        () -> new RuntimeException("App Instance not found"))
        );
    }

    public List<String> getPodNamesForAppInstance(AppInstance appInstance) {

        final String namespace = appInstance.getDomain().getCodename();

        final String prefix = this.appDeploymentRepositoryManager.load(appInstance.getInternalId())
                .getDescriptiveDeploymentId().getValue();

        KubernetesClient client = configFactory.getClient();
        PodList podList = client.pods().inNamespace(namespace).list();

        return podList.getItems().stream()
                .map(pod -> pod.getMetadata().getName())
                .filter(podName -> podName.startsWith(prefix))
                .collect(Collectors.toList());
    }
}
