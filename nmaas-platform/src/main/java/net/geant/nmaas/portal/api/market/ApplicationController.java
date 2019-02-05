package net.geant.nmaas.portal.api.market;

import java.util.List;
import java.util.stream.Collectors;

import net.geant.nmaas.portal.persistent.entity.Application;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.api.domain.ApplicationBriefView;
import net.geant.nmaas.portal.api.domain.Id;

@RestController
@RequestMapping("/api/apps")
public class ApplicationController extends AppBaseController {
	
	@GetMapping
	@Transactional
	public List<ApplicationBriefView> getApplications() {
		return applications.findAll().stream()
				.filter(app -> !app.isDeleted())
				.map(app -> modelMapper.map(app, ApplicationBriefView.class))
				.collect(Collectors.toList());
	}

	@GetMapping("/all")
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public List<ApplicationBriefView> getAllApplications(){
		return applications.findAll().stream()
				.map(app -> modelMapper.map(app, ApplicationBriefView.class))
				.collect(Collectors.toList());
	}
	
	@PostMapping
	@PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public Id addApplication(@RequestBody(required=true) ApplicationView appRequest) {
		Application app = applications.create(appRequest.getName());
		modelMapper.map(appRequest, app);
		applications.update(app);
		return new Id(app.getId());
	}

	@DeleteMapping(value="/{appId}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @Transactional
    public void deleteApplication(@PathVariable(value = "appId") long appId){
	    applications.delete(appId);
    }

	@GetMapping(value="/{appId}")
	@PreAuthorize("hasPermission(#appId, 'application', 'READ')")
	@Transactional
	public ApplicationView getApplication(@PathVariable(value = "appId", required=true) Long id) {
		Application app = getApp(id);
		return modelMapper.map(app, ApplicationView.class);
	}

}
