package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import static com.google.common.base.Preconditions.checkArgument;

public enum IngressControllerConfigOption {

    USE_EXISTING {
        @Override
        public void validate(KClusterIngress ingress) {
            checkArgument(ingress.getSupportedIngressClass() != null && !ingress.getSupportedIngressClass().isEmpty()
                    , "When using existing ingress controller the supported ingress class can't be empty.");
        }
    }, DEPLOY_NEW_FROM_REPO {
        @Override
        public void validate(KClusterIngress ingress) {
            checkArgument(ingress.getControllerChartName() != null && !ingress.getControllerChartName().isEmpty()
                    , "When deploying ingress controller from chart repository the controller chart name can't be empty.");
        }
    }, DEPLOY_NEW_FROM_ARCHIVE {
        @Override
        public void validate(KClusterIngress ingress) {
            checkArgument(ingress.getControllerChartArchive() != null && !ingress.getControllerChartArchive().isEmpty()
                    , "When deploying ingress controller from local chart file the controller archive name can't be empty.");
        }
    }, USE_EXISTING_PER_NAMESPACE {
        @Override
        public void validate(KClusterIngress ingress){}
    };

    public abstract void validate(KClusterIngress ingress);

}
