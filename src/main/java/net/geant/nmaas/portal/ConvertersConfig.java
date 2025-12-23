package net.geant.nmaas.portal;

import net.geant.nmaas.portal.domain.converters.ApplicationSubscriptionConverter;
import net.geant.nmaas.portal.domain.converters.ConfigurationConverter;
import net.geant.nmaas.portal.domain.converters.InetAddressConverter;
import net.geant.nmaas.portal.domain.converters.InetAddressInverseConverter;
import net.geant.nmaas.portal.domain.converters.RoleInverseConverter;
import net.geant.nmaas.portal.domain.converters.TagConverter;
import net.geant.nmaas.portal.domain.converters.TagInverseConverter;
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
        modelMapper.addConverter(new TagConverter(tagRepository));
        modelMapper.addConverter(new TagInverseConverter());
        modelMapper.addConverter(new RoleInverseConverter());
        modelMapper.addConverter(new InetAddressConverter());
        modelMapper.addConverter(new InetAddressInverseConverter());
        modelMapper.addConverter(new ApplicationSubscriptionConverter());
        modelMapper.addConverter(new UserConverter());
        modelMapper.addConverter(new ConfigurationConverter());
        return modelMapper;
    }

}