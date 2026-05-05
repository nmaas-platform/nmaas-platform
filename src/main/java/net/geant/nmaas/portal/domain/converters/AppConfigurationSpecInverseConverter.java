package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.AppConfigurationSpecDto;
import net.geant.nmaas.nmservice.configuration.entities.AppConfigurationSpec;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;

import java.util.Objects;

public class AppConfigurationSpecInverseConverter extends AbstractConverter<AppConfigurationSpecDto, AppConfigurationSpec> {

    ModelMapper modelMapper = new ModelMapper();

    @Override
    protected AppConfigurationSpec convert(AppConfigurationSpecDto source) {
        return new AppConfigurationSpec(
                source.id(),
                source.configFileRepositoryRequired(),
                Objects.nonNull(source.templates()) ?
                        source.templates().stream().map(t -> modelMapper.map(t, ConfigFileTemplate.class)).toList() : null,
                source.configUpdateEnabled(),
                source.termsAcceptanceRequired());
    }
}
