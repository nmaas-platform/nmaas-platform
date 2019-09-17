package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.portal.api.domain.ApplicationBriefView;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
public class TagController {

	TagRepository tagRepo;
	
	ModelMapper modelMapper;

	@Autowired
	public TagController(TagRepository tagRepo, ModelMapper modelMapper){
		this.tagRepo = tagRepo;
		this.modelMapper = modelMapper;
	}
	
	@GetMapping
	public Set<String> getAll() {
		return tagRepo.findAll().stream()
				.filter(tag -> tag.getApplications().stream().anyMatch(app -> app.getState().equals(ApplicationState.ACTIVE) || app.getState().equals(ApplicationState.DISABLED)))
				.map(tag -> modelMapper.map(tag, String.class))
				.collect(Collectors.toSet());
	}
	
	@GetMapping(value="/{tagName}")
	@Transactional
	public Set<ApplicationBriefView> getByTag(@PathVariable("tagName") String tagName) {
		Tag tag = tagRepo.findByName(tagName);
		if(tag != null)
			return tag.getApplications().stream().map(app -> modelMapper.map(app, ApplicationBriefView.class)).collect(Collectors.toSet());
		else
			return Collections.emptySet();			
	}
	
}
