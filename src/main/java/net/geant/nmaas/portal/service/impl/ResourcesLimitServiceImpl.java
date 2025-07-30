package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.api.domain.ResourcesLimitDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.DomainGroup;
import net.geant.nmaas.portal.persistent.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistent.repositories.ResourcesLimitRepository;
import net.geant.nmaas.portal.service.ResourcesLimitService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourcesLimitServiceImpl implements ResourcesLimitService {

    private static final String GLOBAL_UNIQUE_RESOURCES_LIMIT ="You could define only one global resources limit";
    private static final String DOMAIN_UNIQUE_RESOURCES_LIMIT ="You could define only one resources limit per domain";
    private static final String DOMAIN_GROUP_UNIQUE_RESOURCES_LIMIT ="You could define only one resources limit per domain group";
    private static final String RESOURCES_LIMIT_FOR_ONE_TYPE ="You could define resources limit globally or per domain or per domain group";

    private final ResourcesLimitRepository resourcesLimitRepository;
    private final ModelMapper modelMapper;

    @Override
    public ResourcesLimitDto create(ResourcesLimitDto dto) {

        int count = 0;
        if (dto.isGlobal() && ++count == 2) { }
        else if (dto.getDomain() != null && dto.getDomain().getId() != null && ++count == 2) { }
        else if (dto.getDomainGroup() != null && dto.getDomainGroup().getId() != null  && ++count == 2) {  }

        if (count >= 2) {
            throw new IllegalArgumentException(RESOURCES_LIMIT_FOR_ONE_TYPE);
        } else if (dto.isGlobal() && resourcesLimitRepository.existsByIsGlobalTrue()) {
            throw new IllegalArgumentException(GLOBAL_UNIQUE_RESOURCES_LIMIT);
        } else if (dto.getDomain() != null && dto.getDomain().getId() != null && resourcesLimitRepository.existsByDomain_Id(dto.getDomain().getId())) {
            throw new IllegalArgumentException(DOMAIN_UNIQUE_RESOURCES_LIMIT);
        } else if (dto.getDomainGroup() != null && dto.getDomainGroup().getId() != null && resourcesLimitRepository.existsByDomainGroup_Id(dto.getDomainGroup().getId())) {
            throw new IllegalArgumentException(DOMAIN_GROUP_UNIQUE_RESOURCES_LIMIT);
        }

        ResourcesLimit entity = modelMapper.map(dto, ResourcesLimit.class);
        entity = resourcesLimitRepository.save(entity);
        return modelMapper.map(entity, ResourcesLimitDto.class);
    }

    @Override
    public void update(ResourcesLimitDto dto){
        ResourcesLimit entity = resourcesLimitRepository.findById(dto.getId()).orElseThrow(() -> new MissingElementException("Resources Limit not found"));

        int count = 0;
        if (dto.isGlobal() && ++count == 2) { }
        else if (dto.getDomain() != null && dto.getDomain().getId() != null && ++count == 2) { }
        else if (dto.getDomainGroup() != null && dto.getDomainGroup().getId() != null  && ++count == 2) {  }

        if (count >= 2) {
            throw new IllegalArgumentException(RESOURCES_LIMIT_FOR_ONE_TYPE);
        } else if (dto.isGlobal() && resourcesLimitRepository.existsByIsGlobalTrueAndIdNot(dto.getId())) {
            throw new IllegalArgumentException(GLOBAL_UNIQUE_RESOURCES_LIMIT);
        } else if (dto.getDomain() != null && dto.getDomain().getId() != null && resourcesLimitRepository.existsByDomain_IdAndIdNot(dto.getDomain().getId(), dto.getId())) {
            throw new IllegalArgumentException(DOMAIN_UNIQUE_RESOURCES_LIMIT);
        } else if (dto.getDomainGroup() != null && dto.getDomainGroup().getId() != null && resourcesLimitRepository.existsByDomainGroup_IdAndIdNot(dto.getDomainGroup().getId(), dto.getId())) {
            throw new IllegalArgumentException(DOMAIN_GROUP_UNIQUE_RESOURCES_LIMIT);
        }

        entity.setGlobal(dto.isGlobal());
        entity.setCpu(dto.getCpu());
        entity.setMemory(dto.getMemory());
        entity.setContainersNo(dto.getContainersNo());
        entity.setInstancesNo(dto.getInstancesNo());
        entity.setDomain(dto.getDomain() != null && dto.getDomain().getId() != null ? new Domain(dto.getDomain().getId()) : null);
        entity.setDomainGroup(dto.getDomainGroup() != null && dto.getDomainGroup().getId() != null ? new DomainGroup(dto.getDomainGroup().getId()) : null);
        resourcesLimitRepository.save(entity);

    }

    public void delete(Long id){
        resourcesLimitRepository.deleteById(id);
    }

    public ResourcesLimitDto getResourcesLimit(Long id){
        Optional<ResourcesLimit> entity = resourcesLimitRepository.findById(id);
        if (entity.isPresent()) {
            return modelMapper.map(entity.get(), ResourcesLimitDto.class);
        } else {
            throw new MissingElementException("Resources Limit not found");
        }
    }

    public List<ResourcesLimitDto> getAllResourcesLimits(){
        return resourcesLimitRepository.findAll().stream().map(entity -> modelMapper.map(entity, ResourcesLimitDto.class)).toList();
    }
}
