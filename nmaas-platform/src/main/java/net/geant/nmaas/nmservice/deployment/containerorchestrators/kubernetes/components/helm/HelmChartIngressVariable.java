package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

public enum HelmChartIngressVariable {
    INGRESS_HOSTS,
    INGRESS_TLS_ENABLED,
    INGRESS_CLASS,
    INGRESS_LETSENCRYPT,
    INGRESS_WILDCARD_OR_ISSUER,
    INGRESS_ENABLED,
    INGRESS_TLS_HOSTS,
    K8S_SERVICE_SUFFIX,
    K8S_SERVICE_PORT
}
