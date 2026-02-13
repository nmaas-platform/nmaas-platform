package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.AppTagDto;
import net.geant.nmaas.portal.persistence.entity.Tag;
import org.modelmapper.AbstractConverter;

public class TagInverseConverter extends AbstractConverter<AppTagDto, Tag> {

    @Override
    protected Tag convert(AppTagDto source) {
        return new Tag(source.id(), source.name());
    }

}

