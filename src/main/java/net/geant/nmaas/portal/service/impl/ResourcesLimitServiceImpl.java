package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.domain.ResourcesLimitDto;
import net.geant.nmaas.portal.domain.ResourcesLimitUpdateDto;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;
import net.geant.nmaas.portal.persistence.repositories.ResourcesLimitRepository;
import net.geant.nmaas.portal.service.ResourcesLimitService;
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
        log.info("Creating resources limit of type {}", dto.getLimitType());

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
        ResourcesLimit entity = resourcesLimitRepository.findById(dto.getId())
                .orElseThrow(() -> new MissingElementException("Resources Limit not found"));
        entity.setCpu(dto.getCpu());
        entity.setMemory(dto.getMemory());
        entity.setContainersNo(dto.getContainersNo());
        entity.setInstancesNo(dto.getInstancesNo());
        resourcesLimitRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        resourcesLimitRepository.deleteById(id);
    }

    @Override
    public ResourcesLimitDto getResourcesLimit(Long id) {
        Optional<ResourcesLimit> entity = resourcesLimitRepository.findById(id);
        if (entity.isPresent()) {
            return modelMapper.map(entity.get(), ResourcesLimitDto.class);
        } else {
            throw new MissingElementException("Resources Limit not found");
        }
    }

    @Override
    public List<ResourcesLimitDto> getAllResourcesLimits() {
        return resourcesLimitRepository.findAll().stream()
                .map(entity -> modelMapper.map(entity, ResourcesLimitDto.class))
                .toList();
    }

    @Override
    public ResourcesLimitDto getDomainResourceLimit(Long domainId) {
        Optional<ResourcesLimit> entity = resourcesLimitRepository.findByDomain_Id(domainId);
        if (entity.isPresent()) {
            return modelMapper.map(entity.get(), ResourcesLimitDto.class);
        } else {
            throw new MissingElementException("Resources Limit not found");
        }
    }

}
