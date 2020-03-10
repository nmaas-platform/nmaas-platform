package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.repositories.ServiceAccessMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ServiceAccessMethodRepository accessMethodRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateKServiceAccessMethods(Set<ServiceAccessMethod> serviceAccessMethods) {
        serviceAccessMethods.forEach(m -> accessMethodRepository.save(m));
    }

}
