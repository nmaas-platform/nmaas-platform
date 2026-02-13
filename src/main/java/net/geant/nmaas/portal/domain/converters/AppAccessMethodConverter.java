package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.AppAccessMethodView;
import net.geant.nmaas.api.dto.applications.ServiceAccessMethodTypeDto;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import org.modelmapper.AbstractConverter;

import java.util.Objects;

public class AppAccessMethodConverter extends AbstractConverter<AppAccessMethod, AppAccessMethodView> {

    @Override
    protected AppAccessMethodView convert(AppAccessMethod source) {
        return new AppAccessMethodView(
                source.getId(),
                Objects.nonNull(source.getType()) ? ServiceAccessMethodTypeDto.valueOf(source.getType().name()) : null,
                source.getName(),
                source.getTag(),
                Objects.nonNull(source.getConditionType()) ? AppAccessMethodView.ConditionType.valueOf(source.getConditionType().name()) : null,
                source.getDeployParameters()
        );
    }

}
