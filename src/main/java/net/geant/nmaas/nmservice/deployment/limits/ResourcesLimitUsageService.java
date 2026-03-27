package net.geant.nmaas.nmservice.deployment.limits;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.KubernetesApiClientService;
import net.geant.nmaas.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.kubernetes.remote.repositories.KClusterRepository;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.ResourcesLimitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourcesLimitUsageService {

    private final ResourcesLimitRepository resourcesLimitRepository;
    private final AppInstanceRepository appInstanceRepository;
    private final KubernetesApiClientService kubernetesApiClientService;
    private final KubernetesClusterDeploymentManager clusterDeploymentManager;
    private final DomainRepository domainRepository;
    private final KClusterRepository kClusterRepository;

    public ResourceLimitUsage calculateDomainLimitUsage(String domainCodename) {
        Optional<ResourcesLimit> globalLimit = resourcesLimitRepository.findOneByLimitType(ResourcesLimitType.GLOBAL);
        Optional<ResourcesLimit> domainLimit = resourcesLimitRepository.findByDomain_Codename(domainCodename);
        List<ResourcesLimit> groupLimits = resourcesLimitRepository.findForGroupsBasedOnDomain(domainCodename);
        ResourceLimitUsage usage = populateLimits(globalLimit, domainLimit, groupLimits);
        populateUsage(domainCodename, usage);
        return usage;
    }

    private ResourceLimitUsage populateLimits(Optional<ResourcesLimit> globalLimit, Optional<ResourcesLimit> domainLimit, List<ResourcesLimit> groupLimits) {
        ResourceLimitUsage usage = new ResourceLimitUsage();
        ResourcesLimit limit = domainLimit.orElseGet(() -> globalLimit.orElse(null));
        if (limit != null) {
            usage.setMemoryLimit(limit.getMemory());
            usage.setCpuLimit(limit.getCpu());
            usage.setInstancesNoLimit(limit.getInstancesNo());
            usage.setContainersNoLimit(limit.getContainersNo());
            usage.setGlobalLimit(domainLimit.isEmpty());
        }
        return usage;
    }

    private void populateUsage(String domainCodename, ResourceLimitUsage usage) {
        List<AppInstance> runningInstances = appInstanceRepository.findAllActiveInDomain(domainCodename);
        usage.setInstancesNoUsed(runningInstances.size());
        usage.setContainersNoUsed(countRunningContainersInDomain(domainCodename));
        usage.setCpuUsed(runningInstances.stream()
                    .mapToInt(x -> x.getApplication().getAppDeploymentSpec().getConsumedCpu())
                    .sum());
        usage.setMemoryUsed(runningInstances.stream()
                    .mapToInt(x -> x.getApplication().getAppDeploymentSpec().getConsumedMemory())
                    .sum());
    }

    private int countRunningContainersInDomain(String domainCodename) {
        String namespace = clusterDeploymentManager.namespace(domainCodename);
        final int localClusterCount = countRunningContainersInNamespace(null, namespace);
        int remoteClustersCount = 0;
        Optional<Domain> domainOpt = domainRepository.findByCodename(domainCodename);
        if (domainOpt.isPresent()) {
            for (KCluster cluster : kClusterRepository.findByDomains_Id(domainOpt.get().getId())) {
                remoteClustersCount += countRunningContainersInNamespace(cluster, namespace);
            }
        }
        return localClusterCount + remoteClustersCount;
    }

    private int countRunningContainersInNamespace(KCluster cluster, String namespace) {
        try {
            return kubernetesApiClientService.getPods(cluster, namespace).getItems().size();
        } catch (Exception e) {
            return 0;
        }
    }

}
