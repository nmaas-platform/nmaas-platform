package net.geant.nmaas.kubernetes;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.kubernetes.shell.PodShellConnector;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * This object is responsible for creating connectors to instances
 * currently utilizes Kubernetes Connector
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class AsyncConnectorFactory {

    private final KubernetesClientConfigFactory configFactory;

    public AsyncConnector preparePodShellConnection(AppInstance appInstance, String podName) {
        final String namespace = appInstance.getDomain().getCodename();
        return preparePodShellConnection(namespace, podName);
    }

    public AsyncConnector preparePodShellConnection(AppInstance appInstance) {
        return preparePodShellConnection(appInstance, "default");
    }

    public AsyncConnector preparePodShellConnection(String namespace, String podName) {
        log.info("Attempting to connect to Kubernetes pod (namespace: {}, pod: {})", namespace, podName);
        KubernetesClient client = configFactory.getClient();
        log.info("K8s client connected to API version {}", StringUtils.join(client.getVersion().getMajor(), ".", client.getVersion().getMinor()));
        return new PodShellConnector(client, namespace, podName);
    }

}
