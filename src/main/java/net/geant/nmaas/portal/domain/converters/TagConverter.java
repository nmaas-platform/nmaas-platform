package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.AppTagDto;
import net.geant.nmaas.portal.persistence.entity.Tag;
import org.modelmapper.AbstractConverter;

public class TagConverter extends AbstractConverter<Tag, AppTagDto> {

    @Override
    protected AppTagDto convert(Tag source) {
        return new AppTagDto(source.getId(), source.getName());
    }

}

