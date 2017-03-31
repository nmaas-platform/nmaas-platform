package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.portal.api.domain.Application;
import net.geant.nmaas.portal.api.domain.ApplicationBrief;
import net.geant.nmaas.portal.api.domain.ApplicationComplete;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/portal/api/apps")
public class ApplicationController extends AppBaseController {
	
	@RequestMapping(method=RequestMethod.GET)
	@Transactional
	public List<ApplicationBrief> getApplications() {
		return appRepo.findAll().stream().map(app -> modelMapper.map(app, ApplicationBrief.class)).collect(Collectors.toList());
	}
	
	@RequestMapping(method=RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MANAGER')")
	@Transactional
	public Id addApplication(@RequestBody(required=true) ApplicationComplete appRequest) {
		net.geant.nmaas.portal.persistent.entity.Application app;
		app = modelMapper.map(appRequest, net.geant.nmaas.portal.persistent.entity.Application.class);
		appRepo.save(app);
		return new Id(app.getId());
	}
	
	@RequestMapping(value="/{appId}", method=RequestMethod.GET)
	@Transactional
	public Application getApplication(@PathVariable(value = "appId", required=true) Long id) throws MissingElementException {
		net.geant.nmaas.portal.persistent.entity.Application app = getApp(id); 
		return modelMapper.map(app, Application.class);
	}
	
}
