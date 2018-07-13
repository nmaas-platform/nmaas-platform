package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import static com.google.common.base.Preconditions.checkArgument;

public enum IngressControllerConfigOption {

    USE_EXISTING {
        @Override
        public void validate(KClusterIngress ingress) {
            checkArgument(ingress.getSupportedIngressClass() != null, "Supported ingress class can't be empty.");
        }
    }, DEPLOY_NEW_FROM_REPO {
        @Override
        public void validate(KClusterIngress ingress) {
            checkArgument(ingress.getControllerChartName() != null, "Controller chart name can't be empty.");
        }
    }, DEPLOY_NEW_FROM_ARCHIVE {
        @Override
        public void validate(KClusterIngress ingress) {
            checkArgument(ingress.getControllerChartArchive() != null, "Controller archive name can't be empty.");
        }
    };

    public abstract void validate(KClusterIngress ingress);

}
