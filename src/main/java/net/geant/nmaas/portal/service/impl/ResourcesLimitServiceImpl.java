package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.KubernetesApiClientService;
import net.geant.nmaas.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.kubernetes.remote.repositories.KClusterRepository;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.domain.RejectionReason;
import net.geant.nmaas.portal.domain.ResourcesLimitDto;
import net.geant.nmaas.portal.domain.ResourcesLimitUpdateDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.ResourcesLimitRepository;
import net.geant.nmaas.portal.service.ResourcesLimitService;
import net.geant.nmaas.portal.domain.ResourcesLimitValidationResult;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourcesLimitServiceImpl implements ResourcesLimitService {

    private static final String GLOBAL_UNIQUE_RESOURCES_LIMIT = "You can define only one global resources limit";
    private static final String DOMAIN_RESOURCES_LIMIT = "You must define a domain";
    private static final String DOMAIN_UNIQUE_RESOURCES_LIMIT = "You can define only one resources limit per domain";
    private static final String DOMAIN_GROUP_RESOURCES_LIMIT = "You must define a domain group";
    private static final String DOMAIN_GROUP_UNIQUE_RESOURCES_LIMIT = "You can define only one resources limit per domain group";

    private final ResourcesLimitRepository resourcesLimitRepository;
    private final AppInstanceRepository appInstanceRepository;
    private final KubernetesApiClientService kubernetesApiClientService;
    private final KubernetesClusterDeploymentManager clusterDeploymentManager;
    private final DomainRepository domainRepository;
    private final KClusterRepository kClusterRepository;
    private final ModelMapper modelMapper;

    @Override
    public void setGlobalResourcesLimit(ResourcesLimitDto dto) {
        List<ResourcesLimit> limits = resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL);
        if (limits.size() == 1) {
            log.info("Updating existing global limit");
            ResourcesLimit limitFromDb = limits.getFirst();
            limitFromDb.setCpu(dto.getCpu());
            limitFromDb.setMemory(dto.getMemory());
            limitFromDb.setContainersNo(dto.getContainersNo());
            limitFromDb.setInstancesNo(dto.getInstancesNo());
            resourcesLimitRepository.save(limitFromDb);
            return;
        }
        if (limits.isEmpty()) {
            log.info("Adding new global limit");
            ResourcesLimit entity = modelMapper.map(dto, ResourcesLimit.class);
            resourcesLimitRepository.save(entity);
        }
    }

    @Override
    public ResourcesLimitDto getGlobalResourcesLimit() {
        List<ResourcesLimit> limits = resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL);
        if (limits.size() == 1) {
            return modelMapper.map(limits.getFirst(), ResourcesLimitDto.class);
        } else {
            throw new MissingElementException("Global Resources Limit not found or found too many");
        }
    }

    @Override
    public ResourcesLimitDto create(ResourcesLimitDto dto) {

        if (ResourcesLimitType.GLOBAL.equals(dto.getLimitType()) && resourcesLimitRepository.existsByLimitType(ResourcesLimitType.GLOBAL)) {
            throw new IllegalArgumentException(GLOBAL_UNIQUE_RESOURCES_LIMIT);
        } else if (ResourcesLimitType.DOMAIN.equals(dto.getLimitType()) && (dto.getDomain() == null || dto.getDomain().getId() == null)) {
            throw new IllegalArgumentException(DOMAIN_RESOURCES_LIMIT);
        } else if (ResourcesLimitType.DOMAIN.equals(dto.getLimitType()) && dto.getDomain() != null && dto.getDomain().getId() != null && resourcesLimitRepository.existsByDomain_Id(dto.getDomain().getId())) {
            throw new IllegalArgumentException(DOMAIN_UNIQUE_RESOURCES_LIMIT);
        } else if (ResourcesLimitType.DOMAIN_GROUP.equals(dto.getLimitType()) && (dto.getDomainGroup() == null || dto.getDomainGroup().getId() == null)) {
            throw new IllegalArgumentException(DOMAIN_GROUP_RESOURCES_LIMIT);
        } else if (ResourcesLimitType.DOMAIN_GROUP.equals(dto.getLimitType()) && dto.getDomainGroup() != null && dto.getDomainGroup().getId() != null && resourcesLimitRepository.existsByDomainGroup_Id(dto.getDomainGroup().getId())) {
            throw new IllegalArgumentException(DOMAIN_GROUP_UNIQUE_RESOURCES_LIMIT);
        }

        switch (dto.getLimitType()) {
            case GLOBAL:
                dto.setDomain(null);
                dto.setDomainGroup(null);
                break;
            case DOMAIN:
                dto.setDomainGroup(null);
                break;
            case DOMAIN_GROUP:
                dto.setDomain(null);
                break;
        }

        ResourcesLimit entity = modelMapper.map(dto, ResourcesLimit.class);
        entity = resourcesLimitRepository.save(entity);
        return modelMapper.map(entity, ResourcesLimitDto.class);
    }

    @Override
    public void update(ResourcesLimitUpdateDto dto) {
        ResourcesLimit entity = resourcesLimitRepository.findById(dto.getId()).orElseThrow(() -> new MissingElementException("Resources Limit not found"));
        entity.setCpu(dto.getCpu());
        entity.setMemory(dto.getMemory());
        entity.setContainersNo(dto.getContainersNo());
        entity.setInstancesNo(dto.getInstancesNo());
        resourcesLimitRepository.save(entity);
    }

    public void delete(Long id) {
        resourcesLimitRepository.deleteById(id);
    }

    public ResourcesLimitDto getResourcesLimit(Long id) {
        Optional<ResourcesLimit> entity = resourcesLimitRepository.findById(id);
        if (entity.isPresent()) {
            return modelMapper.map(entity.get(), ResourcesLimitDto.class);
        } else {
            throw new MissingElementException("Resources Limit not found");
        }
    }

    public List<ResourcesLimitDto> getAllResourcesLimits() {
        return resourcesLimitRepository.findAll().stream().map(entity -> modelMapper.map(entity, ResourcesLimitDto.class)).toList();
    }

    @Override
    public ResourcesLimitValidationResult validateNewDeployment(String domainCodename,
                                                                Identifier applicationId,
                                                                int requestedInstances,
                                                                AppDeploymentSpec deploymentSpec) {
        ResourcesLimit limit = resourcesLimitRepository.findByDomain_Codename(domainCodename);
        List<ResourcesLimit> groupsLimits = resourcesLimitRepository.findForGroupsBasedOnDomain(domainCodename);
        if (limit != null) {
            return validateAgainst(false, limit, groupsLimits, domainCodename, requestedInstances, deploymentSpec);
        } else {
            List<ResourcesLimit> globalLimits = resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL);
            if (!globalLimits.isEmpty()) {
                return validateAgainst(true, globalLimits.get(0), groupsLimits, domainCodename, requestedInstances, deploymentSpec);
            }
        }
        ResourcesLimitValidationResult validationResult =  new ResourcesLimitValidationResult();
        validationResult.setAccepted(true);
        return validationResult;
    }

    private ResourcesLimitValidationResult validateAgainst(boolean basedOnGlobal,
                                                           ResourcesLimit limit,
                                                           List<ResourcesLimit> groupsLimits,
                                                           String domainCodename,
                                                           int requestedInstances,
                                                           AppDeploymentSpec deploymentSpec) {
        ResourcesLimitValidationResult validationResult =  new ResourcesLimitValidationResult();
        validationResult.setAccepted(true);
        List<AppInstance> runningDeployments = appInstanceRepository.findAllActiveInDomain(domainCodename);

        if (limit.getInstancesNo() != null && (runningDeployments.size() + requestedInstances > limit.getInstancesNo()+ groupsLimits.stream().mapToInt(ResourcesLimit::getInstancesNo).sum())) {
            validationResult.setAccepted(false);
            validationResult.getReasons().add(basedOnGlobal
                            ? RejectionReason.GLOBAL_INSTANCES_LIMIT_REACHED
                            : RejectionReason.DOMAIN_INSTANCES_LIMIT_REACHED);
        }

        if (limit.getContainersNo() != null && (countRunningContainersInDomain(domainCodename) + deploymentSpec.getConsumedPods() > limit.getContainersNo()+ groupsLimits.stream().mapToInt(ResourcesLimit::getContainersNo).sum())) {
            validationResult.setAccepted(false);
            validationResult.getReasons().add(basedOnGlobal
                            ? RejectionReason.GLOBAL_CONTAINERS_LIMIT_REACHED
                            : RejectionReason.DOMAIN_CONTAINERS_LIMIT_REACHED);
        }

        if (limit.getCpu() != null && (runningDeployments.stream().mapToInt(x -> x.getApplication().getAppDeploymentSpec().getConsumedCpu()).sum() + deploymentSpec.getConsumedCpu() > limit.getCpu()+ groupsLimits.stream().mapToInt(ResourcesLimit::getCpu).sum())) {
            validationResult.setAccepted(false);
            validationResult.getReasons().add(basedOnGlobal
                    ? RejectionReason.GLOBAL_CPU_LIMIT_REACHED
                    : RejectionReason.DOMAIN_CPU_LIMIT_REACHED);
        }

        if (limit.getMemory() != null && (runningDeployments.stream().mapToInt(x -> x.getApplication().getAppDeploymentSpec().getConsumedMemory()).sum() + deploymentSpec.getConsumedMemory() > limit.getMemory()+ groupsLimits.stream().mapToInt(ResourcesLimit::getMemory).sum())) {
            validationResult.setAccepted(false);
            validationResult.getReasons().add(basedOnGlobal
                    ? RejectionReason.GLOBAL_MEMORY_LIMIT_REACHED
                    : RejectionReason.DOMAIN_MEMORY_LIMIT_REACHED);
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
