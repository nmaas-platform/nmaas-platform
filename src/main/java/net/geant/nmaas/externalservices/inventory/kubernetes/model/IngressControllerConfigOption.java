package net.geant.nmaas.externalservices.inventory.kubernetes.model;

import static com.google.common.base.Preconditions.checkArgument;

public enum IngressControllerConfigOption {

    USE_EXISTING {
        @Override
        public void validate(KClusterView.KClusterIngressView ingress) {
            checkArgument(ingress.getSupportedIngressClass() != null && !ingress.getSupportedIngressClass().isEmpty()
                    , "When using existing ingress controller the supported ingress class can't be empty.");
        }
    }, DEPLOY_NEW_FROM_REPO {
        @Override
        public void validate(KClusterView.KClusterIngressView ingress) {
            checkArgument(ingress.getControllerChartName() != null && !ingress.getControllerChartName().isEmpty()
                    , "When deploying ingress controller from chart repository the controller chart name can't be empty.");
        }
    }, DEPLOY_NEW_FROM_ARCHIVE {
        @Override
        public void validate(KClusterView.KClusterIngressView ingress) {
            checkArgument(ingress.getControllerChartArchive() != null && !ingress.getControllerChartArchive().isEmpty()
                    , "When deploying ingress controller from local chart file the controller archive name can't be empty.");
        }
    };

    public abstract void validate(KClusterView.KClusterIngressView ingress);

}
