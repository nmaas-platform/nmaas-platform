package net.geant.nmaas.externalservices.kubernetes.model;

import static com.google.common.base.Preconditions.checkArgument;

public enum IngressResourceConfigOption {

    /** Don't deploy or configure ingress resource at all */
    NOT_USED {
        @Override
        public void validate(KClusterView.KClusterIngressView ingress) {
            // no need to check anything
        }
    },
    /** Use ingress resource definition from the application helm chart */
    DEPLOY_FROM_CHART {
        @Override
        public void validate(KClusterView.KClusterIngressView ingress) {
            checkArgument(ingress.getExternalServiceDomain() != null && !ingress.getExternalServiceDomain().isEmpty()
                    , "When deploying ingress resource the external service domain can't be empty.");
            checkArgument(ingress.getTlsSupported() != null
                    , "When deploying ingress resource the TLS support flag must be set.");
        }
    };

    public abstract void validate(KClusterView.KClusterIngressView ingress);

}
