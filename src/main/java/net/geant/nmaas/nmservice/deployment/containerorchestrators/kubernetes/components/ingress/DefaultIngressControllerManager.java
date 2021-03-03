package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.ingress;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.IngressControllerManager;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

@Component
public class DefaultIngressControllerManager implements IngressControllerManager {

    @Override
    public void deployIngressControllerIfMissing(String domain) {
        throw new NotImplementedException("Automatic deployment of ingress controllers is not supported yet");
    }

    @Override
    public void deleteIngressController(String domain) {
        throw new NotImplementedException("Removal of ingress controllers is not supported yet");
    }

}
