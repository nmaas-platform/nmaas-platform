package net.geant.nmaas.kubernetes.remote.entities;

import net.geant.nmaas.api.dto.kubernetes.KClusterDto.KClusterIngressDto;
import org.apache.commons.lang3.Validate;

public enum IngressCertificateConfigOption {

    USE_WILDCARD {
        @Override
        public void validate(KClusterIngressDto ingress) {
            Validate.isTrue(ingress.getCertificateConfigOption() != null && !ingress.getIssuerOrWildcardName().isEmpty()
                    , "When using existing wildcard certificate, it's secret's name cannot be empty.");
        }
    }, USE_LETSENCRYPT {
        @Override
        public void validate(KClusterIngressDto ingress) {
            Validate.isTrue(ingress.getCertificateConfigOption() != null && !ingress.getIssuerOrWildcardName().isEmpty()
                    , "When using letsencrypt certificates, acme issuer name cannot be empty.");
        }
    };

    public abstract void validate(KClusterIngressDto ingress);

}
