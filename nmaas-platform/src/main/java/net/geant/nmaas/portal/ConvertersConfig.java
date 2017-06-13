package net.geant.nmaas.portal;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.geant.nmaas.portal.api.domain.converters.RoleInverseConverter;
import net.geant.nmaas.portal.api.domain.converters.TagConverter;
import net.geant.nmaas.portal.api.domain.converters.TagInverseConverter;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;

@Configuration
public class ConvertersConfig {
	
	@Autowired
	TagRepository tagRepo;
	
	@Bean
	public ModelMapper modelMapper() {
	    ModelMapper modelMapper = new ModelMapper();
	    modelMapper.addConverter(new TagConverter(tagRepo));
	    modelMapper.addConverter(new TagInverseConverter());
	    modelMapper.addConverter(new RoleInverseConverter());
	    return modelMapper;
	}
	
	
}
