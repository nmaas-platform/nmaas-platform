package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.AppDescriptionDto;
import net.geant.nmaas.portal.persistence.entity.AppDescription;
import org.modelmapper.AbstractConverter;

public class AppDescriptionInverseConverter extends AbstractConverter<AppDescriptionDto, AppDescription> {

    @Override
    protected AppDescription convert(AppDescriptionDto source) {
        return new AppDescription(null, source.language(), source.briefDescription(), source.fullDescription());
    }

}
