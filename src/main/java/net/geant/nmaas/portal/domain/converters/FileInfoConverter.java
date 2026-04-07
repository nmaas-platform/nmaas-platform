package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.users.UserFileDto;
import net.geant.nmaas.portal.persistence.entity.FileInfo;
import org.modelmapper.AbstractConverter;

public class FileInfoConverter extends AbstractConverter<FileInfo, UserFileDto> {

    @Override
    protected UserFileDto convert(FileInfo source) {
        return new UserFileDto(
                source.getId(),
                source.getFilename(),
                source.getContentType());
    }

}
