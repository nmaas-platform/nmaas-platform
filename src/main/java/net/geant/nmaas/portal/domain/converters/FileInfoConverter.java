package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.users.UserFile;
import net.geant.nmaas.portal.persistence.entity.FileInfo;
import org.modelmapper.AbstractConverter;

public class FileInfoConverter extends AbstractConverter<FileInfo, UserFile> {

    @Override
    protected UserFile convert(FileInfo source) {
        return new UserFile(
                source.getId(),
                source.getFilename(),
                source.getContentType());
    }

}

