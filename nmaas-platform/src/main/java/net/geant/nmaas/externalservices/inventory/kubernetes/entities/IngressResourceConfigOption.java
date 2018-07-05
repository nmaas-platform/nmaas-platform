package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import static com.google.common.base.Preconditions.checkArgument;

public enum IngressResourceConfigOption {

    /** Don't deploy or configure ingress resource at all */
    NOT_USED {
        @Override
        public void validate(KClusterIngress ingress) {
            // no need to check anything
        }
    },
    /** Deploy or update ingress resource using Kubernetes API */
    DEPLOY_USING_API {
        @Override
        public void validate(KClusterIngress ingress) {
            checkArgument(ingress.getExternalServiceDomain() != null, "External service domain can't be empty.");
            checkArgument(ingress.getTlsSupported() != null, "TLS support flag must be set.");
        }
    },
    /** Use ingress resource definition from the application helm chart */
    DEPLOY_FROM_CHART {
        @Override
        public void validate(KClusterIngress ingress) {
            checkArgument(ingress.getExternalServiceDomain() != null, "External service domain can't be empty.");
            checkArgument(ingress.getTlsSupported() != null, "TLS support flag must be set.");
        }
    };

    public abstract void validate(KClusterIngress ingress);

}
