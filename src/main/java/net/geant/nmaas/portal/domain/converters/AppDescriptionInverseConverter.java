package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.AppDescriptionView;
import net.geant.nmaas.portal.persistence.entity.AppDescription;
import org.modelmapper.AbstractConverter;

public class AppDescriptionInverseConverter extends AbstractConverter<AppDescriptionView, AppDescription> {

    @Override
    protected AppDescription convert(AppDescriptionView source) {
        return new AppDescription(null, source.language(), source.briefDescription(), source.fullDescription());
    }

}
