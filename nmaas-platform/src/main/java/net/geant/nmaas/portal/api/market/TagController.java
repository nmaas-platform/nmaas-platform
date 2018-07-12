package net.geant.nmaas.portal.api.market;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.portal.api.domain.ApplicationBrief;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;

@RestController
@RequestMapping("/api/tags")
public class TagController {

	@Autowired
	TagRepository tagRepo;
	
	@Autowired
	ModelMapper modelMapper;
	
	
	@RequestMapping(method=RequestMethod.GET)
	public Set<String> getAll() {
		return tagRepo.findAll().stream().map(tag -> modelMapper.map(tag, String.class)).collect(Collectors.toSet());
	}
	
	@RequestMapping(value="/{tagName}", method=RequestMethod.GET)
	public Set<ApplicationBrief> getByTag(@PathVariable("tagName") String tagName) {
		Tag tag = tagRepo.findByName(tagName);
		if(tag != null)
			return tag.getApplications().stream().map(app -> modelMapper.map(app, ApplicationBrief.class)).collect(Collectors.toSet());
		else
			return Collections.emptySet();			
	}
	
}
