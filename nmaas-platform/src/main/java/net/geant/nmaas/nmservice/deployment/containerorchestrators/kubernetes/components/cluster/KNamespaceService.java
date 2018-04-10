package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KNamespaceService {

    private static final String NMAAS_NAMESPACE_PREFIX = "nmaas-ns-";

    @Value("${kubernetes.namespace.use.default}")
    private boolean useDefaultNamespace;

    @Value("${kubernetes.namespace.default}")
    private String defaultNamespace;

    public String namespace(String domain) {
        return (useDefaultNamespace) ? defaultNamespace : NMAAS_NAMESPACE_PREFIX + domain;
    }

}
