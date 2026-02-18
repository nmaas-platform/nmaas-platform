package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.CommentDto;
import net.geant.nmaas.api.dto.users.UserBase;
import net.geant.nmaas.portal.persistence.entity.Comment;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;

import java.util.Date;
import java.util.Objects;

public class CommentConverter extends AbstractConverter<Comment, CommentDto> {

    ModelMapper modelMapper = new ModelMapper();

    @Override
    protected CommentDto convert(Comment source) {
        Long parentId = null;
        if (source.getParent() != null) {
            parentId = source.getParent().getId();
        }
        String commentText = source.getComment();
        if (source.isDeleted()) {
            commentText = "<em>@@@\'COMMENTS.REMOVED\'</em>";
        }
        return new CommentDto(
                source.getId(),
                parentId,
                modelMapper.map(source.getOwner(), UserBase.class),
                Objects.nonNull(source.getCreatedAt()) ? new Date(source.getCreatedAt()) : null,
                commentText, source.isDeleted(),
                source.getSubComments().stream().map(this::convert).toList()
        );
    }

}


