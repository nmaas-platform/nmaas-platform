package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.TagView;
import net.geant.nmaas.portal.persistence.entity.Tag;
import org.modelmapper.AbstractConverter;

public class TagInverseConverter extends AbstractConverter<TagView, Tag> {

    @Override
    protected Tag convert(TagView source) {
        return new Tag(source.id(), source.name());
    }

}

