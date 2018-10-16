package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.portal.api.domain.Application;
import net.geant.nmaas.portal.api.domain.ApplicationBrief;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apps")
public class ApplicationController extends AppBaseController {

	@GetMapping
	@Transactional
	public List<ApplicationBrief> getApplications() {
		return applications.findAll().stream().map(app -> modelMapper.map(app, ApplicationBrief.class)).collect(Collectors.toList());
	}

	@PostMapping
	@PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_TOOL_MANAGER')")
	@Transactional
	public Id addApplication(@RequestBody(required=true) Application appRequest) {
		net.geant.nmaas.portal.persistent.entity.Application app = applications.create(appRequest.getName());
		modelMapper.map(appRequest, app);
		applications.update(app);
		return new Id(app.getId());
	}

	@GetMapping(value="/{appId}")
	@PreAuthorize("hasPermission(#appId, 'application', 'READ')")
	@Transactional
	public Application getApplication(@PathVariable(value = "appId", required=true) Long id) throws MissingElementException {
		net.geant.nmaas.portal.persistent.entity.Application app = getApp(id); 
		return modelMapper.map(app, Application.class);
	}

}
