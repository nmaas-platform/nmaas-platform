package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.portal.persistence.entity.Tag;
import net.geant.nmaas.portal.persistence.repositories.TagRepository;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.AbstractConverter;

public class TagConverter extends AbstractConverter<String, Tag>{

    private TagRepository tagRepo;

    public TagConverter(TagRepository tagRepo) {
            super();
            if(tagRepo == null)
                    throw new IllegalStateException("Tag repo is null");
            this.tagRepo = tagRepo;
    }

    @Override
    protected Tag convert(String source) {
            if(StringUtils.isEmpty(source)) {
                return null;
            }
            return tagRepo.findByName(source).orElse(new Tag(source));
    }

}

