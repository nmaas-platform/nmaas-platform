package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.api.domain.ResourcesLimitDto;
import net.geant.nmaas.portal.api.domain.ResourcesLimitUpdateDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistent.entity.ResourcesLimitType;
import net.geant.nmaas.portal.persistent.repositories.ResourcesLimitRepository;
import net.geant.nmaas.portal.service.ResourcesLimitService;
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
}
