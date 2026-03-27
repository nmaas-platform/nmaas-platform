package net.geant.nmaas.nmservice.deployment.limits;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import org.springframework.stereotype.Service;

import java.util.Objects;

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

    private final ResourcesLimitUsageService resourcesLimitUsageService;

    public ValidationResult validateNewDeployment(String domainCodename, int requestedInstances, AppDeploymentSpec deploymentSpec) {
        ResourceLimitUsage usage = resourcesLimitUsageService.calculateDomainLimitUsage(domainCodename);
        if (Objects.nonNull(usage)) {
            return validateAgainst(usage, requestedInstances, deploymentSpec.getConsumedPods(), deploymentSpec.getConsumedCpu(), deploymentSpec.getConsumedMemory());
        }
        return ValidationResult.accepted();
    }

    private ValidationResult validateAgainst(ResourceLimitUsage usage,
                                             int requestedInstances, int requestedContainers,
                                             int requestedCpu, int requestedMemory) {
        log.info("Validating against limits");
        ValidationResult validationResult = new ValidationResult();
        validationResult.setAccepted(true);

        if (usage.getInstancesNoLimit() != null) {
            log.info("Instances: used -> {}, requested -> {}", usage.getInstancesNoUsed(), requestedInstances);
            if (usage.getInstancesNoUsed() + requestedInstances > usage.getInstancesNoLimit()) {
                validationResult.setAccepted(false);
                validationResult.getReasons().add(usage.isGlobalLimit() ? GLOBAL_INSTANCES_LIMIT_REACHED : DOMAIN_INSTANCES_LIMIT_REACHED);
            }
        }
        if (usage.getContainersNoLimit() != null) {
            log.info("Containers: used -> {}, requested -> {}", usage.getContainersNoUsed(), requestedContainers);
            if (usage.getContainersNoUsed() + requestedContainers > usage.getContainersNoLimit()) {
                validationResult.setAccepted(false);
                validationResult.getReasons().add(usage.isGlobalLimit() ? GLOBAL_CONTAINERS_LIMIT_REACHED : DOMAIN_CONTAINERS_LIMIT_REACHED);
            }
        }
        if (usage.getCpuLimit() != null) {
            log.info("CPU: used -> {}, requested -> {}", usage.getCpuUsed(), requestedCpu);
            if (usage.getCpuUsed() + requestedCpu > usage.getCpuLimit()) {
                validationResult.setAccepted(false);
                validationResult.getReasons().add(usage.isGlobalLimit() ? GLOBAL_CPU_LIMIT_REACHED : DOMAIN_CPU_LIMIT_REACHED);
            }
        }
        if (usage.getMemoryLimit() != null) {
            log.info("Memory: used -> {}, requested -> {}", usage.getMemoryUsed(), requestedMemory);
            if (usage.getMemoryUsed() + requestedMemory > usage.getMemoryLimit()) {
                validationResult.setAccepted(false);
                validationResult.getReasons().add(usage.isGlobalLimit() ? GLOBAL_MEMORY_LIMIT_REACHED : DOMAIN_MEMORY_LIMIT_REACHED);
            }
        }
        return validationResult;
    }

}