package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes;

import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolume;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.repositories.ServiceAccessMethodRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.repositories.ServiceStorageVolumeRepository;
import net.geant.nmaas.orchestration.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType.MAIN;
import static net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolumeType.SHARED;

@Component
@Profile("env_kubernetes")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class KubernetesRepositoryManager extends NmServiceRepositoryManager<KubernetesNmServiceInfo> {

    @Autowired
    private ServiceStorageVolumeRepository storageVolumeRepository;

    @Autowired
    private ServiceAccessMethodRepository accessMethodRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStorageSpace(Identifier deploymentId, Integer storageSpace) {
        KubernetesNmServiceInfo serviceInfo = loadService(deploymentId);
        List<ServiceStorageVolume> storageVolumes = serviceInfo.getStorageVolumes().stream()
                .filter(v -> Arrays.asList(MAIN, SHARED).contains(v.getType()))
                .collect(Collectors.toList());
        storageVolumes.forEach(v -> {
            v.setSize(storageSpace);
            storageVolumeRepository.save(v);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateKServiceAccessMethods(Set<ServiceAccessMethod> serviceAccessMethods) {
        serviceAccessMethods.forEach(m -> accessMethodRepository.save(m));
    }

}
