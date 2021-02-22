package net.geant.nmaas.dcn.deployment;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DcnDeploymentProvidersManager {

    private DcnRepositoryManager dcnRepositoryManager;

    private List<DcnDeploymentProvider> dcnDeploymentProviders;

    @Autowired
    public DcnDeploymentProvidersManager(DcnRepositoryManager dcnRepositoryManager, List<DcnDeploymentProvider> dcnDeploymentProviders){
        this.dcnRepositoryManager = dcnRepositoryManager;
        this.dcnDeploymentProviders = dcnDeploymentProviders;
    }

    public DcnDeploymentProvider getDcnDeploymentProvider(String domain){
        DcnDeploymentType type = dcnRepositoryManager.loadType(domain);
        return dcnDeploymentProviders.stream()
                .filter(provider -> provider.getDcnDeploymentType().equals(type))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Dcn deployment type " + type + " does not exist"));
    }
}
