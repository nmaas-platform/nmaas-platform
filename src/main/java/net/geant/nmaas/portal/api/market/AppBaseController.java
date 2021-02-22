package net.geant.nmaas.portal.api.market;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

@NoArgsConstructor
@AllArgsConstructor
public class AppBaseController {

	@Autowired
	protected ModelMapper modelMapper;

	@Autowired
	protected ApplicationService applicationService;

	@Autowired
	protected ApplicationBaseService appBaseService;

	@Autowired
	protected UserService userService;

    protected Application getApp(Long appId) {
		if(appId == null)
			throw new MissingElementException("Missing application id.");
		
		return applicationService.findApplication(appId).orElseThrow(() -> new MissingElementException("Application id=" + appId + " not found."));
	}

	protected ApplicationBase getBaseApp(Long appBaseId){
		if(appBaseId == null)
			throw new MissingElementException("Missing application id.");
    	return appBaseService.getBaseApp(appBaseId);
	}

	protected User getUser(String username) {
		if(username == null)
			throw new MissingElementException("Missing username.");
		
		return userService.findByUsername(username).orElseThrow(() -> new MissingElementException("Missing user " + username));
	}

	protected User getUser(Long userId) {
		if(userId == null)
			throw new MissingElementException("Missing username.");
		
		return userService.findById(userId).orElseThrow(() -> new MissingElementException("Missing user id=" + userId));
	}

}