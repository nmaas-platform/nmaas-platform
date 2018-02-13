package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.orchestration.entities.Identifier;

public interface KNamespaceAware {

    static final String NMAAS_NAMESPACE_PREFIX = "nmaas-ns-";

    default String clientNamespace(Identifier clientId) {
        return NMAAS_NAMESPACE_PREFIX + clientId;
    }

}
