package net.geant.nmaas.nmservice.deployment.limits;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.KubernetesApiClientService;
import net.geant.nmaas.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.kubernetes.remote.repositories.KClusterRepository;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.ResourcesLimitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.geant.nmaas.nmservice.deployment.limits.RejectionReason.DOMAIN_CONTAINERS_LIMIT_REACHED;
import static net.geant.nmaas.nmservice.deployment.limits.RejectionReason.DOMAIN_CPU_LIMIT_REACHED;
import static net.geant.nmaas.nmservice.deployment.limits.RejectionReason.DOMAIN_INSTANCES_LIMIT_REACHED;
import static net.geant.nmaas.nmservice.deployment.limits.RejectionReason.DOMAIN_MEMORY_LIMIT_REACHED;
import static net.geant.nmaas.nmservice.deployment.limits.RejectionReason.GLOBAL_CONTAINERS_LIMIT_REACHED;
import static net.geant.nmaas.nmservice.deployment.limits.RejectionReason.GLOBAL_CPU_LIMIT_REACHED;
import static net.geant.nmaas.nmservice.deployment.limits.RejectionReason.GLOBAL_INSTANCES_LIMIT_REACHED;
import static net.geant.nmaas.nmservice.deployment.limits.RejectionReason.GLOBAL_MEMORY_LIMIT_REACHED;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourcesLimitValidationService {

    private final ResourcesLimitRepository resourcesLimitRepository;
    private final AppInstanceRepository appInstanceRepository;
    private final KubernetesApiClientService kubernetesApiClientService;
    private final KubernetesClusterDeploymentManager clusterDeploymentManager;
    private final DomainRepository domainRepository;
    private final KClusterRepository kClusterRepository;

    public ValidationResult validateNewDeployment(String domainCodename, int requestedInstances, AppDeploymentSpec deploymentSpec) {
        Optional<ResourcesLimit> globalLimit = resourcesLimitRepository.findOneByLimitType(ResourcesLimitType.GLOBAL);
        Optional<ResourcesLimit> domainLimit = resourcesLimitRepository.findByDomain_Codename(domainCodename);
        List<ResourcesLimit> groupLimits = resourcesLimitRepository.findForGroupsBasedOnDomain(domainCodename);
        ResourcesLimit limit = domainLimit.orElseGet(() -> globalLimit.orElse(null));
        if (Objects.nonNull(limit)) {
            return validateAgainst(limit, groupLimits, domainCodename,
                    requestedInstances, deploymentSpec.getConsumedPods(), deploymentSpec.getConsumedCpu(), deploymentSpec.getConsumedMemory());
        }
        return ValidationResult.accepted();
    }

    private ValidationResult validateAgainst(ResourcesLimit limit, List<ResourcesLimit> groupsLimits,
                                             String domainCodename,
                                             int requestedInstances,
                                             int requestedContainers,
                                             int requestedCpu,
                                             int requestedMemory) {
        log.info("Validating against limit {} and {} group limits", limit.toString(), groupsLimits.size());
        ValidationResult validationResult = new ValidationResult();
        validationResult.setAccepted(true);
        List<AppInstance> runningDeployments = appInstanceRepository.findAllActiveInDomain(domainCodename);

        if (limit.getInstancesNo() != null) {
            log.info("Instances: used -> {}, requested -> {}", runningDeployments.size(), requestedInstances);
            if (runningDeployments.size() + requestedInstances > limit.getInstancesNo() + groupsLimits.stream().mapToInt(ResourcesLimit::getInstancesNo).sum()) {
                validationResult.setAccepted(false);
                validationResult.getReasons().add(limit.isGlobal() ? GLOBAL_INSTANCES_LIMIT_REACHED : DOMAIN_INSTANCES_LIMIT_REACHED);
            }
        }

        if (limit.getContainersNo() != null) {
            int runningContainers = countRunningContainersInDomain(domainCodename);
            log.info("Containers: used -> {}, requested -> {}", runningContainers, requestedContainers);
            if (runningContainers + requestedContainers > limit.getContainersNo() + groupsLimits.stream().mapToInt(ResourcesLimit::getContainersNo).sum()) {
                validationResult.setAccepted(false);
                validationResult.getReasons().add(limit.isGlobal() ? GLOBAL_CONTAINERS_LIMIT_REACHED : DOMAIN_CONTAINERS_LIMIT_REACHED);
            }
        }

        if (limit.getCpu() != null) {
            int consumedCpu = runningDeployments.stream().mapToInt(x -> x.getApplication().getAppDeploymentSpec().getConsumedCpu()).sum();
            log.info("CPU: used -> {}, requested -> {}", consumedCpu, requestedCpu);
            if (consumedCpu + requestedCpu > limit.getCpu() + groupsLimits.stream().mapToInt(ResourcesLimit::getCpu).sum()) {
                validationResult.setAccepted(false);
                validationResult.getReasons().add(limit.isGlobal() ? GLOBAL_CPU_LIMIT_REACHED : DOMAIN_CPU_LIMIT_REACHED);
            }
        }

        if (limit.getMemory() != null) {
            int consumedMemory = runningDeployments.stream().mapToInt(x -> x.getApplication().getAppDeploymentSpec().getConsumedMemory()).sum();
            log.info("CPU: used -> {}, requested -> {}", consumedMemory, requestedMemory);
            if (consumedMemory + requestedMemory > limit.getMemory() + groupsLimits.stream().mapToInt(ResourcesLimit::getMemory).sum()) {
                validationResult.setAccepted(false);
                validationResult.getReasons().add(limit.isGlobal() ? GLOBAL_MEMORY_LIMIT_REACHED : DOMAIN_MEMORY_LIMIT_REACHED);
            }
        }

        return validationResult;
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
