package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import static com.google.common.base.Preconditions.checkArgument;

public enum NamespaceConfigOption {
    USE_DEFAULT_NAMESPACE{
      @Override
      public void validate(KClusterDeployment deployment){
        checkArgument(!deployment.getDefaultNamespace().isEmpty() && deployment.getDefaultNamespace() != null,
                "When using default namespace the default namespace field can't be empty.");
      }
    },
    USE_DOMAIN_NAMESPACE{
        @Override
        public void validate(KClusterDeployment deployment){
            //no need to check anything
        }
    },
    CREATE_NAMESPACE{
        @Override
        public void validate(KClusterDeployment deployment){
            //no need to check anything
        }
    };

    public abstract void validate(KClusterDeployment deployment);
}
