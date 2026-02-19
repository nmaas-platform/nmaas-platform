package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.AppDescriptionView;
import net.geant.nmaas.portal.persistence.entity.AppDescription;
import org.modelmapper.AbstractConverter;

public class AppDescriptionConverter extends AbstractConverter<AppDescription, AppDescriptionView> {

    @Override
    protected AppDescriptionView convert(AppDescription source) {
        return new AppDescriptionView(source.getLanguage(), source.getBriefDescription(), source.getFullDescription());
    }

}
