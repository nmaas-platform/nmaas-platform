package net.geant.nmaas.portal.api.apps;

import net.geant.nmaas.portal.api.BaseController;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationVersion;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public class AppBaseController extends BaseController {

	protected final ApplicationService applicationService;
	protected final ApplicationBaseService applicationBaseService;

	@Autowired
	public AppBaseController(ModelMapper modelMapper, UserService userService, ApplicationService applicationService, ApplicationBaseService applicationBaseService) {
		super(modelMapper, userService);
		this.applicationService = applicationService;
		this.applicationBaseService = applicationBaseService;
	}

	protected Application getApp(Long appId) {
		if (appId == null) {
			throw new MissingElementException("Missing application id.");
		}
		return applicationService.findApplication(appId).orElseThrow(() -> new MissingElementException("Application id=" + appId + " not found."));
	}

	protected Application getApp(String appName, String version) {
		if (appName == null) {
			throw new MissingElementException("Missing application id.");
		}
		return applicationService.findApplication(appName, version).orElseThrow(() -> new MissingElementException("Application name=" + appName + " version=" + version +" not found."));
	}

	protected ApplicationBase getBaseApp(Long appBaseId) {
		if (appBaseId == null) {
			throw new MissingElementException("Missing application id.");
		}
    	return applicationBaseService.getBaseApp(appBaseId);
	}

	protected Set<ApplicationVersion> getVersions(Long appBaseId) {
		if (appBaseId == null) {
			throw new MissingElementException("Missing application id.");
		}
		return applicationBaseService.getBaseApp(appBaseId).getVersions();
	}

}
