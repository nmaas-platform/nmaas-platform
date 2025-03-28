package net.geant.nmaas.dcn.deployment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DcnDeploymentProvidersManager {

    private final DcnRepositoryManager dcnRepositoryManager;
    private final List<DcnDeploymentProvider> dcnDeploymentProviders;

    public DcnDeploymentProvider getDcnDeploymentProvider(String domain){
        DcnDeploymentType type = dcnRepositoryManager.loadType(domain);
        return dcnDeploymentProviders.stream()
                .filter(provider -> provider.getDcnDeploymentType().equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("DCN deployment type " + type + " does not exist"));
    }

}
