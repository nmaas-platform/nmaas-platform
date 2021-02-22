package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import static com.google.common.base.Preconditions.checkArgument;

public enum IngressCertificateConfigOption {
    USE_WILDCARD {
        @Override
        public void validate(KClusterIngress ingress) {
            checkArgument(ingress.getCertificateConfigOption() != null && !ingress.getIssuerOrWildcardName().isEmpty()
                    , "When using existing wildcard certificate, it's secret's name cannot be empty.");
        }
    }, USE_LETSENCRYPT {
        @Override
        public void validate(KClusterIngress ingress) {
            checkArgument(ingress.getCertificateConfigOption() != null && !ingress.getIssuerOrWildcardName().isEmpty()
                    , "When using letsencrypt certificates, acme issuer name cannot be empty.");
        }
    };
    public abstract void validate(KClusterIngress ingress);
}
