package net.geant.nmaas.externalservices.kubernetes.model;

import static com.google.common.base.Preconditions.checkArgument;

public enum IngressCertificateConfigOption {

    USE_WILDCARD {
        @Override
        public void validate(KClusterView.KClusterIngressView ingress) {
            checkArgument(ingress.getCertificateConfigOption() != null && !ingress.getIssuerOrWildcardName().isEmpty()
                    , "When using existing wildcard certificate, it's secret's name cannot be empty.");
        }
    }, USE_LETSENCRYPT {
        @Override
        public void validate(KClusterView.KClusterIngressView ingress) {
            checkArgument(ingress.getCertificateConfigOption() != null && !ingress.getIssuerOrWildcardName().isEmpty()
                    , "When using letsencrypt certificates, acme issuer name cannot be empty.");
        }
    };

    public abstract void validate(KClusterView.KClusterIngressView ingress);

}
