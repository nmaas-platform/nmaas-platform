package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import static com.google.common.base.Preconditions.checkNotNull;

public enum IngressControllerConfigOption {

    USE_EXISTING {
        @Override
        public void validate(KClusterIngress ingress) {
            checkNotNull(ingress.getSupportedIngressClass(), "Supported ingress class can't be empty.");
        }
    }, DEPLOY_NEW_FROM_REPO {
        @Override
        public void validate(KClusterIngress ingress) {
            checkNotNull(ingress.getControllerChartName(), "Controller chart name can't be empty.");
        }
    }, DEPLOY_NEW_FROM_ARCHIVE {
        @Override
        public void validate(KClusterIngress ingress) {
            checkNotNull(ingress.getControllerChartArchive(), "Controller archive name can't be empty.");
        }
    };

    public abstract void validate(KClusterIngress ingress);

}
