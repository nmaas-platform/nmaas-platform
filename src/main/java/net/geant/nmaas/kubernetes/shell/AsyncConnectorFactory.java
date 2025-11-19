package net.geant.nmaas.kubernetes.shell;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import org.springframework.stereotype.Component;

/**
 * This object is responsible for creating connectors to instances
 * currently utilizes Kubernetes Connector
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncConnectorFactory {

    private final KubernetesConnectorHelper helper;

    public AsyncConnector preparePodShellConnection(AppInstance appInstance, String podName) {
        final KubernetesClient client = helper.getKubernetesClient(appInstance);
        final String namespace = appInstance.getDomain().getCodename();
        return new PodShellConnector(client, namespace, podName);
    }

}
