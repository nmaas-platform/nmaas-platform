package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.domains.ResourcesLimitDto;
import net.geant.nmaas.api.dto.domains.ResourcesLimitUpdateDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.repositories.DomainGroupRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.ResourcesLimitRepository;
import net.geant.nmaas.portal.service.ResourcesLimitService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static net.geant.nmaas.api.dto.domains.ResourcesLimitTypeDto.DOMAIN;
import static net.geant.nmaas.api.dto.domains.ResourcesLimitTypeDto.DOMAIN_GROUP;
import static net.geant.nmaas.api.dto.domains.ResourcesLimitTypeDto.GLOBAL;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourcesLimitServiceImpl implements ResourcesLimitService {

    private static final String GLOBAL_UNIQUE_RESOURCES_LIMIT = "You can define only one global resources limit";
    private static final String DOMAIN_RESOURCES_LIMIT = "You must define a domain";
    private static final String DOMAIN_UNIQUE_RESOURCES_LIMIT = "You can define only one resources limit per domain";
    private static final String DOMAIN_GROUP_RESOURCES_LIMIT = "You must define a domain group";
    private static final String DOMAIN_GROUP_UNIQUE_RESOURCES_LIMIT = "You can define only one resources limit per domain group";
    private static final Integer DEFAULT_MEMORY = 2000;
    private static final Integer DEFAULT_CPU = 1000;
    private static final Integer DEFAULT_INSTANCES_NO = 10;
    private static final Integer DEFAULT_CONTAINERS_NO = 20;

    private final ResourcesLimitRepository resourcesLimitRepository;
    private final DomainRepository domainRepository;
    private final DomainGroupRepository domainGroupRepository;
    private final ModelMapper modelMapper;

    @Override
    public void setGlobalResourcesLimit(ResourcesLimitDto dto) {
        dto = withDefaultValues(dto);
        List<ResourcesLimit> limits = resourcesLimitRepository.findByLimitType(
                net.geant.nmaas.portal.persistence.entity.ResourcesLimitType.GLOBAL);
        if (limits.size() == 1) {
            log.info("Updating existing global limit");
            ResourcesLimit limitFromDb = limits.getFirst();
            limitFromDb.setCpu(dto.cpu());
            limitFromDb.setMemory(dto.memory());
            limitFromDb.setContainersNo(dto.containersNo());
            limitFromDb.setInstancesNo(dto.instancesNo());
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
        List<ResourcesLimit> limits = resourcesLimitRepository.findByLimitType(
                net.geant.nmaas.portal.persistence.entity.ResourcesLimitType.GLOBAL);
        if (limits.size() == 1) {
            return modelMapper.map(limits.getFirst(), ResourcesLimitDto.class);
        } else {
            throw new MissingElementException("Global Resources Limit not found or found too many");
        }
    }

    @Override
    public ResourcesLimitDto create(ResourcesLimitDto dto) {
        dto = withDefaultValues(dto);
        log.info("Creating resources limit of type {}", dto.limitType());

        if (GLOBAL.equals(dto.limitType()) && resourcesLimitRepository.existsByLimitType(
                net.geant.nmaas.portal.persistence.entity.ResourcesLimitType.GLOBAL)) {
            throw new IllegalArgumentException(GLOBAL_UNIQUE_RESOURCES_LIMIT);
        } else if (DOMAIN.equals(dto.limitType()) && (dto.domain() == null || dto.domain().getId() == null)) {
            throw new IllegalArgumentException(DOMAIN_RESOURCES_LIMIT);
        } else if (DOMAIN.equals(dto.limitType()) && dto.domain() != null && dto.domain().getId() != null && resourcesLimitRepository.existsByDomain_Id(dto.domain().getId())) {
            throw new IllegalArgumentException(DOMAIN_UNIQUE_RESOURCES_LIMIT);
        } else if (DOMAIN_GROUP.equals(dto.limitType()) && (dto.domainGroup() == null || dto.domainGroup().id() == null)) {
            throw new IllegalArgumentException(DOMAIN_GROUP_RESOURCES_LIMIT);
        } else if (DOMAIN_GROUP.equals(dto.limitType()) && dto.domainGroup() != null && dto.domainGroup().id() != null && resourcesLimitRepository.existsByDomainGroup_Id(dto.domainGroup().id())) {
            throw new IllegalArgumentException(DOMAIN_GROUP_UNIQUE_RESOURCES_LIMIT);
        }

        ResourcesLimit entity = modelMapper.map(dto, ResourcesLimit.class);
        if (dto.limitType().equals(DOMAIN_GROUP)) {
            entity.setDomainGroup(domainGroupRepository.getReferenceById(dto.domainGroup().id()));
        } else if (dto.limitType().equals(DOMAIN)) {
            entity.setDomain(domainRepository.getReferenceById(dto.domain().getId()));
        }
        ResourcesLimit result = resourcesLimitRepository.save(entity);
        return new ResourcesLimitDto(
                result.getId(),
                result.getMemory(),
                result.getCpu(),
                result.getInstancesNo(),
                result.getContainersNo(),
                dto.limitType(),
                dto.domainGroup(),
                dto.domain());
    }

    @Override
    public void update(ResourcesLimitUpdateDto dto) {
        dto = withDefaultValues(dto);
        ResourcesLimit entity = resourcesLimitRepository.findById(dto.id())
                .orElseThrow(() -> new MissingElementException("Resources Limit not found"));
        entity.setCpu(dto.cpu());
        entity.setMemory(dto.memory());
        entity.setContainersNo(dto.containersNo());
        entity.setInstancesNo(dto.instancesNo());
        resourcesLimitRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ResourcesLimit entity = resourcesLimitRepository.findById(id)
                .orElseThrow(() -> new MissingElementException("Resources Limit not found"));
        if (entity.getDomainGroup() != null) {
            entity.getDomainGroup().setResourcesLimit(null);
            entity.setDomainGroup(null);
        }
        if (entity.getDomain() != null) {
            entity.getDomain().setResourcesLimit(null);
            entity.setDomain(null);
        }
        resourcesLimitRepository.delete(entity);
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

    @Override
    public ResourcesLimitDto getGroupResourceLimit(Long groupId) {
        Optional<ResourcesLimit> entity = resourcesLimitRepository.findByDomainGroup_Id(groupId);
        if (entity.isPresent()) {
            return modelMapper.map(entity.get(), ResourcesLimitDto.class);
        } else {
            throw new MissingElementException("Resources Limit not found");
        }
    }

    private ResourcesLimitDto withDefaultValues(ResourcesLimitDto dto) {
        return new ResourcesLimitDto(
                dto.id(),
                valueOrDefault(dto.memory(), DEFAULT_MEMORY),
                valueOrDefault(dto.cpu(), DEFAULT_CPU),
                valueOrDefault(dto.instancesNo(), DEFAULT_INSTANCES_NO),
                valueOrDefault(dto.containersNo(), DEFAULT_CONTAINERS_NO),
                dto.limitType(),
                dto.domainGroup(),
                dto.domain());
    }

    private ResourcesLimitUpdateDto withDefaultValues(ResourcesLimitUpdateDto dto) {
        return new ResourcesLimitUpdateDto(
                dto.id(),
                valueOrDefault(dto.memory(), DEFAULT_MEMORY),
                valueOrDefault(dto.cpu(), DEFAULT_CPU),
                valueOrDefault(dto.instancesNo(), DEFAULT_INSTANCES_NO),
                valueOrDefault(dto.containersNo(), DEFAULT_CONTAINERS_NO));
    }

    private Integer valueOrDefault(Integer value, Integer defaultValue) {
        return value == null || value == 0 ? defaultValue : value;
    }
}
