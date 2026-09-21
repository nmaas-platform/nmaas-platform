package net.geant.nmaas.kubernetes.remote.entities;

import net.geant.nmaas.api.dto.kubernetes.KClusterDto.KClusterDeploymentDto;
import org.apache.commons.lang3.Validate;

public enum NamespaceConfigOption {
    USE_DEFAULT_NAMESPACE {
        @Override
        public void validate(KClusterDeploymentDto deployment) {
            Validate.isTrue(deployment.getDefaultNamespace() != null && !deployment.getDefaultNamespace().isEmpty(),
                    "When using default namespace the default namespace field can't be empty.");
        }
    },
    USE_DOMAIN_NAMESPACE {
        @Override
        public void validate(KClusterDeploymentDto deployment) {
            //no need to check anything
        }
    },
    CREATE_NAMESPACE {
        @Override
        public void validate(KClusterDeploymentDto deployment) {
            //no need to check anything
        }
    };

    public abstract void validate(KClusterDeploymentDto deployment);
}
