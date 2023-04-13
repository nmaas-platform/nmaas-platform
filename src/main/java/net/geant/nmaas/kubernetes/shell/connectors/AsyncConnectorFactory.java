package net.geant.nmaas.kubernetes.shell.connectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.kubernetes.client.KubernetesClientConfigFactory;
import net.geant.nmaas.kubernetes.client.KubernetesConnector;
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

    public AsyncConnector prepareConnection(AppInstance appInstance, String podName) {
        final String namespace = appInstance.getDomain().getCodename();
        return prepareConnection(namespace, podName);
    }

    public AsyncConnector prepareConnection(AppInstance appInstance) {
        return this.prepareConnection(appInstance, "default");
    }

    public AsyncConnector prepareConnection(String namespace, String podName) {
        log.info("Attempting to connect to Kubernetes pod (namespace: {}, pod: {}", namespace, podName);
        return new KubernetesConnector(configFactory.getClient(), namespace, podName);
    }

}
