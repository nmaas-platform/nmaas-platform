package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.AppDescriptionDto;
import net.geant.nmaas.portal.persistence.entity.AppDescription;
import org.modelmapper.AbstractConverter;

public class AppDescriptionConverter extends AbstractConverter<AppDescription, AppDescriptionDto> {

    @Override
    protected AppDescriptionDto convert(AppDescription source) {
        return new AppDescriptionDto(source.getLanguage(), source.getBriefDescription(), source.getFullDescription());
    }

}
