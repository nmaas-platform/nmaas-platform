package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.domains.DomainGroupBaseDto;
import net.geant.nmaas.portal.persistence.entity.DomainGroup;
import org.modelmapper.AbstractConverter;

public class DomainGroupBaseConverter extends AbstractConverter<DomainGroup, DomainGroupBaseDto> {

    @Override
    protected DomainGroupBaseDto convert(DomainGroup source) {
        return new DomainGroupBaseDto(
                source.getId(),
                source.getName(),
                source.getCodename(),
                source.getDomains().size());
    }

}
