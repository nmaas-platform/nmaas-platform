package net.geant.nmaas.portal;

import net.geant.nmaas.portal.domain.converters.AppAccessMethodConverter;
import net.geant.nmaas.portal.domain.converters.AppConfigurationSpecConverter;
import net.geant.nmaas.portal.domain.converters.AppConfigurationSpecInverseConverter;
import net.geant.nmaas.portal.domain.converters.AppDescriptionConverter;
import net.geant.nmaas.portal.domain.converters.AppDescriptionInverseConverter;
import net.geant.nmaas.portal.domain.converters.ApplicationSubscriptionConverter;
import net.geant.nmaas.portal.domain.converters.CommentConverter;
import net.geant.nmaas.portal.domain.converters.ConfigurationConverter;
import net.geant.nmaas.portal.domain.converters.CustomerNetworkConverter;
import net.geant.nmaas.portal.domain.converters.FileInfoConverter;
import net.geant.nmaas.portal.domain.converters.InetAddressConverter;
import net.geant.nmaas.portal.domain.converters.InetAddressInverseConverter;
import net.geant.nmaas.portal.domain.converters.ResourceLimitConverter;
import net.geant.nmaas.portal.domain.converters.ResourceLimitInverseConverter;
import net.geant.nmaas.portal.domain.converters.RoleInverseConverter;
import net.geant.nmaas.portal.domain.converters.TagConverter;
import net.geant.nmaas.portal.domain.converters.TagInverseConverter;
import net.geant.nmaas.portal.domain.converters.TagStringConverter;
import net.geant.nmaas.portal.domain.converters.TagStringInverseConverter;
import net.geant.nmaas.portal.domain.converters.UserConverter;
import net.geant.nmaas.portal.persistence.repositories.TagRepository;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConvertersConfig {

    private final TagRepository tagRepository;

    @Autowired
    public ConvertersConfig(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.addConverter(new TagStringConverter(tagRepository));
        modelMapper.addConverter(new TagStringInverseConverter());
        modelMapper.addConverter(new TagConverter());
        modelMapper.addConverter(new TagInverseConverter());
        modelMapper.addConverter(new RoleInverseConverter());
        modelMapper.addConverter(new InetAddressConverter());
        modelMapper.addConverter(new InetAddressInverseConverter());
        modelMapper.addConverter(new ApplicationSubscriptionConverter());
        modelMapper.addConverter(new UserConverter());
        modelMapper.addConverter(new ConfigurationConverter());
        modelMapper.addConverter(new ResourceLimitConverter());
        modelMapper.addConverter(new ResourceLimitInverseConverter());
        modelMapper.addConverter(new FileInfoConverter());
        modelMapper.addConverter(new AppAccessMethodConverter());
        modelMapper.addConverter(new AppDescriptionConverter());
        modelMapper.addConverter(new AppDescriptionInverseConverter());
        modelMapper.addConverter(new CommentConverter());
        modelMapper.addConverter(new CustomerNetworkConverter());
        modelMapper.addConverter(new AppConfigurationSpecConverter());
        modelMapper.addConverter(new AppConfigurationSpecInverseConverter());
        return modelMapper;
    }

}