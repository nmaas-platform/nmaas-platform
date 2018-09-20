package net.geant.nmaas.portal;

import net.geant.nmaas.portal.api.domain.converters.ApplicationSubscriptionConverter;
import net.geant.nmaas.portal.api.domain.converters.InetAddressConverter;
import net.geant.nmaas.portal.api.domain.converters.InetAddressInverseConverter;
import net.geant.nmaas.portal.api.domain.converters.RoleInverseConverter;
import net.geant.nmaas.portal.api.domain.converters.TagConverter;
import net.geant.nmaas.portal.api.domain.converters.TagInverseConverter;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConvertersConfig {
	
	TagRepository tagRepo;

	@Autowired
	public ConvertersConfig(TagRepository tagRepo){
		this.tagRepo = tagRepo;
	}
	
	@Bean
	public ModelMapper modelMapper() {
	    ModelMapper modelMapper = new ModelMapper();
	    modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
	    modelMapper.addConverter(new TagConverter(tagRepo));
	    modelMapper.addConverter(new TagInverseConverter());
	    modelMapper.addConverter(new RoleInverseConverter());
	    modelMapper.addConverter(new InetAddressConverter());
	    modelMapper.addConverter(new InetAddressInverseConverter());
	    modelMapper.addConverter(new ApplicationSubscriptionConverter());
	    return modelMapper;
	}
	
	
}
