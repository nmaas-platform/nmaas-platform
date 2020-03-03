package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Profile("env_kubernetes")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class KubernetesRepositoryManager extends NmServiceRepositoryManager<KubernetesNmServiceInfo> {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateKServiceAccessMethods(Identifier deploymentId, Set<ServiceAccessMethod> serviceAccessMethods) {
        KubernetesNmServiceInfo serviceInfo = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        serviceInfo.getAccessMethods().clear();
        serviceInfo.getAccessMethods().addAll(serviceAccessMethods);
        repository.save(serviceInfo);
    }

}
