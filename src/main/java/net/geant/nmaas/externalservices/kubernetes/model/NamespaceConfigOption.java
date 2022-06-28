package net.geant.nmaas.externalservices.kubernetes.model;

import static com.google.common.base.Preconditions.checkArgument;

public enum NamespaceConfigOption {
    USE_DEFAULT_NAMESPACE{
      @Override
      public void validate(KClusterView.KClusterDeploymentView deployment){
        checkArgument(deployment.getDefaultNamespace() != null && !deployment.getDefaultNamespace().isEmpty(),
                "When using default namespace the default namespace field can't be empty.");
      }
    },
    USE_DOMAIN_NAMESPACE{
        @Override
        public void validate(KClusterView.KClusterDeploymentView deployment){
            //no need to check anything
        }
    },
    CREATE_NAMESPACE{
        @Override
        public void validate(KClusterView.KClusterDeploymentView deployment){
            //no need to check anything
        }
    };

    public abstract void validate(KClusterView.KClusterDeploymentView deployment);
}
