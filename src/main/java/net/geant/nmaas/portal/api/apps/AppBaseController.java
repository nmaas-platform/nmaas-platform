package net.geant.nmaas.portal.api.apps;

import net.geant.nmaas.portal.api.BaseController;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.PortalException;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Principal;
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

	protected void applicationBaseOwnerCheck(ApplicationBase applicationBase, Principal principal) {
		boolean isSystemAdmin = this.getUser(principal.getName()).getRoles().stream()
				.anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_SYSTEM_ADMIN));
		boolean isOwner = applicationBase.getOwner().equals(principal.getName());
		if (!isOwner && !isSystemAdmin) {
			throw new PortalException("The user is not application owner");
		}
	}

	protected void applicationBaseOwnerCheck(String applicationBaseName, Principal principal) {
		ApplicationBase applicationBase = this.applicationBaseService.findByName(applicationBaseName);
		this.applicationBaseOwnerCheck(applicationBase, principal);
	}

	protected void applicationBaseOwnerCheck(Long id, Principal principal) {
		ApplicationBase applicationBase = this.applicationBaseService.getBaseApp(id);
		this.applicationBaseOwnerCheck(applicationBase, principal);
	}

}
