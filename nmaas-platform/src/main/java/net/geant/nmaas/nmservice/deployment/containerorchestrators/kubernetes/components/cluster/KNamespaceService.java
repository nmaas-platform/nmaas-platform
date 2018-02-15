package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.cluster;

import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KNamespaceService {

    static final String NMAAS_NAMESPACE_PREFIX = "nmaas-ns-client-";

    @Value("${kubernetes.namespace.use.default}")
    private boolean useDefaultNamespace;

    @Value("${kubernetes.namespace.default}")
    private String defaultNamespace;

    public String namespace(Identifier clientId) {
        return (useDefaultNamespace) ? defaultNamespace : NMAAS_NAMESPACE_PREFIX + clientId;
    }

}
