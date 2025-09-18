package net.geant.nmaas.portal.service.impl;

import io.fabric8.kubernetes.api.model.PodList;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.kubernetes.KubernetesApiClientService;
import net.geant.nmaas.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.kubernetes.remote.repositories.KClusterRepository;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.api.domain.RejectionReason;
import net.geant.nmaas.portal.api.domain.ResourcesLimitDto;
import net.geant.nmaas.portal.api.domain.ResourcesLimitUpdateDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistent.entity.ResourcesLimitType;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.ResourcesLimitRepository;
import net.geant.nmaas.portal.service.ResourcesLimitService;
import net.geant.nmaas.portal.api.domain.ResourcesLimitValidationResult;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
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
                                                                int requestedContainers) {
        ResourcesLimit limit = resourcesLimitRepository.findByDomain_Codename(domainCodename);
        List<ResourcesLimit> groupsLimits = resourcesLimitRepository.findForGroupsBasedOnDomain(domainCodename);
        if (limit != null) {
            return validateAgainst(false, limit, groupsLimits, domainCodename, requestedInstances, requestedContainers);
        } else {
            limit = resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL);
            if (limit != null) {
                return validateAgainst(true, limit, groupsLimits, domainCodename, requestedInstances, requestedContainers);
            }
        }
        return accepted();
    }

    private ResourcesLimitValidationResult validateAgainst(boolean basedOnGlobal,
                                                           ResourcesLimit limit,
                                                           List<ResourcesLimit> groupsLimits,
                                                           String domainCodename,
                                                           int requestedInstances,
                                                           int requestedContainers) {
        if (limit.getInstancesNo() != null && (appInstanceRepository.countAllActiveInDomain(domainCodename) + requestedInstances > limit.getInstancesNo()+ groupsLimits.stream().mapToInt(ResourcesLimit::getInstancesNo).sum())) {
            return ResourcesLimitValidationResult.builder()
                    .accepted(false)
                    .reason(basedOnGlobal
                            ? RejectionReason.GLOBAL_INSTANCES_LIMIT_REACHED
                            : RejectionReason.DOMAIN_INSTANCES_LIMIT_REACHED)
                    .build();
        }

        if (limit.getContainersNo() != null && (countRunningContainersInDomain(domainCodename) + requestedContainers > limit.getContainersNo()+ groupsLimits.stream().mapToInt(ResourcesLimit::getContainersNo).sum())) {
            return ResourcesLimitValidationResult.builder()
                    .accepted(false)
                    .reason(basedOnGlobal
                            ? RejectionReason.GLOBAL_CONTAINERS_LIMIT_REACHED
                            : RejectionReason.DOMAIN_CONTAINERS_LIMIT_REACHED)
                    .build();
        }

        return accepted();
    }

    private ResourcesLimitValidationResult accepted() {
        return ResourcesLimitValidationResult.builder()
                .accepted(true)
                .build();
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
            PodList pods = kubernetesApiClientService.getPods(cluster, namespace);
            if (pods == null || pods.getItems() == null) {
                return 0;
            }
            return pods.getItems().stream()
                    .mapToInt(pod -> {
                        if (pod.getStatus() == null || pod.getStatus().getContainerStatuses() == null) {
                            return 0;
                        }
                        return (int) pod.getStatus().getContainerStatuses().stream()
                                .filter(cs -> cs.getState() != null && cs.getState().getRunning() != null)
                                .count();
                    })
                    .sum();
        } catch (Exception e) {
            return 0;
        }
    }
}
