package net.geant.nmaas.portal.api.shell.connectors;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This object is responsible for creating connectors to instances
 * currently utilizes Kubernetes Connector (still to be provided)
 */
@Component
@Log4j2
public class AsyncConnectorFactory {

    private final KubernetesClientConfigFactory configFactory;

    @Autowired
    public AsyncConnectorFactory(KubernetesClientConfigFactory configFactory) {
        this.configFactory = configFactory;
    }

    public AsyncConnector prepareConnection(AppInstance appInstance, String podName) {

        final String namespace = appInstance.getDomain().getCodename();

        log.info("Attempting to connect to Kubernetes pod via nmaas-shell:\tnamespace: " + namespace + "\tpodname: " + podName);
        return new KubernetesConnector(configFactory.getClient(), namespace, podName);

    }

    public AsyncConnector prepareConnection(AppInstance appInstance) {
        // TODO retrieve podName if possible
        return this.prepareConnection(appInstance, "default");
    }
}
