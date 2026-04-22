package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.portal.persistence.entity.Tag;
import net.geant.nmaas.portal.persistence.repositories.TagRepository;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.AbstractConverter;

public class TagStringConverter extends AbstractConverter<String, Tag> {

    private final TagRepository tagRepository;

    public TagStringConverter(TagRepository tagRepository) {
        super();
        if (tagRepository == null) {
            throw new IllegalStateException("Tag repository is null");
        }
        this.tagRepository = tagRepository;
    }

    @Override
    protected Tag convert(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }
        return tagRepository.findByName(source).orElse(new Tag(source));
    }

}
