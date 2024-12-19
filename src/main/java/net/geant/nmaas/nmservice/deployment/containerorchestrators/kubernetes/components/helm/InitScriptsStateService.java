package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InitScriptsStateService {

    private final HelmCommandExecutor helmCommandExecutor;

    public void executeHelmRepoUpdate() {
        helmCommandExecutor.executeHelmRepoUpdateCommand();
    }

}
