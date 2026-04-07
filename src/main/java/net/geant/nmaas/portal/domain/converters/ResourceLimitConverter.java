package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.ResourcesLimitDto;
import net.geant.nmaas.api.dto.ResourcesLimitTypeDto;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.api.dto.domains.DomainGroupBaseDto;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;

import java.util.Objects;

public class ResourceLimitConverter extends AbstractConverter<ResourcesLimit, ResourcesLimitDto> {

    ModelMapper modelMapper = new ModelMapper();

    @Override
    protected ResourcesLimitDto convert(ResourcesLimit source) {
        modelMapper.addConverter(new DomainGroupBaseConverter());
        return new ResourcesLimitDto(
                source.getId(),
                source.getMemory(),
                source.getCpu(),
                source.getInstancesNo(),
                source.getContainersNo(),
                ResourcesLimitTypeDto.valueOf(source.getLimitType().name()),
                Objects.nonNull(source.getDomainGroup()) ? modelMapper.map(source.getDomainGroup(), DomainGroupBaseDto.class) : null,
                Objects.nonNull(source.getDomain()) ? modelMapper.map(source.getDomain(), DomainBaseDto.class) : null);
    }

}

