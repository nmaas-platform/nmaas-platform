package net.geant.nmaas.portal.api.market;

import java.util.Arrays;

import net.geant.nmaas.portal.api.domain.ApplicationBaseView;
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
				.filter(tag -> tag.getApplications().stream()
						.anyMatch(app -> app.getVersions().stream()
								.anyMatch(version -> Arrays.asList(ApplicationState.ACTIVE, ApplicationState.DISABLED).contains(version.getState()))
						)
				)
				.map(Tag::getName)
				.collect(Collectors.toSet());
	}
	
	@GetMapping(value="/{tagName}")
	@Transactional
	public Set<ApplicationBaseView> getByTag(@PathVariable("tagName") String tagName) {
		return tagRepo.findByName(tagName)
				.map(value -> value.getApplications().stream()
						.map(app -> modelMapper.map(app, ApplicationBaseView.class))
						.collect(Collectors.toSet()))
				.orElse(Collections.emptySet());
	}
	
}
