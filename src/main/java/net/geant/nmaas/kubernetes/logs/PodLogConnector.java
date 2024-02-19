package net.geant.nmaas.kubernetes.logs;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.kubernetes.KubernetesConnector;

import java.io.InputStream;
import java.util.Objects;

@Log4j2
public class PodLogConnector extends KubernetesConnector {

    private LogWatch logWatch;

    public PodLogConnector(KubernetesClient client, String namespace, String podName) {
        this.namespace = namespace;
        this.podName = podName;
        this.client = client;
        this.initLogWatch();
    }

    private void initLogWatch() {
        log.debug("Initializing log watch");
        logWatch = client.pods()
                .inNamespace(namespace)
                .withName(podName)
                .tailingLines(10)
                .watchLog();
    }

    @Override
    public InputStream getInputStream() {
        assert Objects.nonNull(logWatch);
        return logWatch.getOutput();
    }

}
