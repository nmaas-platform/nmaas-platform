package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.nmservice.deployment.limits.ResourcesLimitUsageService;
import net.geant.nmaas.portal.persistence.entity.Domain;
import org.modelmapper.AbstractConverter;

public class DomainBaseConverter extends AbstractConverter<Domain, DomainBaseDto> {

    private final ResourcesLimitUsageService limitUsageService;

    public DomainBaseConverter(ResourcesLimitUsageService limitUsageService) {
        super();
        if (limitUsageService == null) {
            throw new IllegalStateException("Tag repo is null");
        }
        this.limitUsageService = limitUsageService;
    }

    @Override
    protected DomainBaseDto convert(Domain source) {
        DomainBaseDto dto = new DomainBaseDto();
        dto.setId(source.getId());
        dto.setName(source.getName());
        dto.setCodename(source.getCodename());
        dto.setActive(source.isActive());
        dto.setDeleted(source.isDeleted());
        dto.setLimitUsage(limitUsageService.calculateDomainLimitUsageValue(source.getCodename()));
        return dto;
    }

}