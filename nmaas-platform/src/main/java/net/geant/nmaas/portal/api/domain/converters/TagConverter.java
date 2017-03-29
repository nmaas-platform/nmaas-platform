package net.geant.nmaas.portal.api.domain.converters;

import org.modelmapper.AbstractConverter;

import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;

public class TagConverter extends AbstractConverter<String, Tag>{

    TagRepository tagRepo;

    public TagConverter(TagRepository tagRepo) {
            super();
            if(tagRepo == null)
                    throw new IllegalStateException("Tag repo is null");
            this.tagRepo = tagRepo;
    }

    @Override
    protected Tag convert(String source) {
            if(source == null) return null;
            Tag tag = tagRepo.findByName(source);
            return (tag != null ? tag : new Tag(source));
    }

}

