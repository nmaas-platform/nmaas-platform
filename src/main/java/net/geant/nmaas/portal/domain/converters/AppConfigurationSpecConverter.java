package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.AppConfigurationSpecView;
import net.geant.nmaas.api.dto.applications.ConfigFileTemplateView;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;

import java.util.Objects;

public class AppConfigurationSpecConverter extends AbstractConverter<AppConfigurationSpec, AppConfigurationSpecView> {

    ModelMapper modelMapper = new ModelMapper();

    @Override
    protected AppConfigurationSpecView convert(AppConfigurationSpec source) {
        return new AppConfigurationSpecView(
                source.getId(),
                Objects.nonNull(source.getTemplates()) ?
                        source.getTemplates().stream().map(t -> modelMapper.map(t, ConfigFileTemplateView.class)).toList() : null,
                source.isConfigFileRepositoryRequired(),
                source.isConfigUpdateEnabled(),
                source.isTermsAcceptanceRequired());
    }
}
