package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.AppAccessMethodView;
import net.geant.nmaas.api.dto.applications.ServiceAccessMethodTypeDto;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import org.modelmapper.AbstractConverter;

public class AppAccessMethodConverter extends AbstractConverter<AppAccessMethod, AppAccessMethodView> {

    @Override
    protected AppAccessMethodView convert(AppAccessMethod source) {
        return new AppAccessMethodView(
                source.getId(),
                ServiceAccessMethodTypeDto.valueOf(source.getType().name()),
                source.getName(),
                source.getTag(),
                AppAccessMethodView.ConditionType.valueOf(source.getConditionType().name()),
                source.getDeployParameters()
        );
    }

}
