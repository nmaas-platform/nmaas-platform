package net.geant.nmaas.portal;

import net.geant.nmaas.portal.api.domain.converters.*;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConvertersConfig {
	
	private TagRepository tagRepo;

	private ApplicationBaseRepository appBaseRepository;

	@Autowired
	public ConvertersConfig(TagRepository tagRepo, ApplicationBaseRepository appBaseRepository){
		this.tagRepo = tagRepo;
		this.appBaseRepository = appBaseRepository;
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
	    modelMapper.addConverter(new UserConverter());
	    modelMapper.addConverter(new ApplicationBaseToAppBriefViewConverter());
	    modelMapper.addConverter(new ApplicationToApplicationViewConverter(appBaseRepository));
	    modelMapper.addConverter(new ApplicationViewToApplicationBaseConverter(tagRepo));
	    modelMapper.addConverter(new ApplicationViewToApplicationConverter());
	    modelMapper.addConverter(new ApplicationToApplicationBriefViewConverter(appBaseRepository));
	    return modelMapper;
	}
	
	
}
