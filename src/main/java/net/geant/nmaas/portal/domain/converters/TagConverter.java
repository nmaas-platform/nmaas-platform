package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.TagView;
import net.geant.nmaas.portal.persistence.entity.Tag;
import org.modelmapper.AbstractConverter;

public class TagConverter extends AbstractConverter<Tag, TagView> {

    @Override
    protected TagView convert(Tag source) {
        return new TagView(source.getId(), source.getName());
    }

}

