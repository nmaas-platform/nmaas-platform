package net.geant.nmaas.kubernetes.remote.entities;

import net.geant.nmaas.api.dto.kubernetes.KClusterDto.KClusterIngressView;
import org.apache.commons.lang3.Validate;

public enum IngressResourceConfigOption {

    /**
     * Don't deploy or configure ingress resource at all
     */
    NOT_USED {
        @Override
        public void validate(KClusterIngressView ingress) {
            // no need to check anything
        }
    },
    /**
     * Use ingress resource definition from the application helm chart
     */
    DEPLOY_FROM_CHART {
        @Override
        public void validate(KClusterIngressView ingress) {
            Validate.isTrue(ingress.getExternalServiceDomain() != null && !ingress.getExternalServiceDomain().isEmpty()
                    , "When deploying ingress resource the external service domain can't be empty.");
            Validate.isTrue(ingress.getTlsSupported() != null
                    , "When deploying ingress resource the TLS support flag must be set.");
        }
    };

    public abstract void validate(KClusterIngressView ingress);

}
