package net.geant.nmaas.portal.api.market;

import lombok.NoArgsConstructor;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

@NoArgsConstructor
public class AppBaseController {

	@Autowired
	protected ModelMapper modelMapper;

	@Autowired
	protected ApplicationService applications;

	@Autowired
	protected UserService users;

    protected net.geant.nmaas.portal.persistent.entity.Application getApp(Long appId) {
		if(appId == null)
			throw new MissingElementException("Missing application id.");
		
		return applications.findApplication(appId).orElseThrow(() -> new MissingElementException("Application id=" + appId + " not found."));
	}

	protected net.geant.nmaas.portal.persistent.entity.User getUser(String username) {
		if(username == null)
			throw new MissingElementException("Missing username.");
		
		return users.findByUsername(username).orElseThrow(() -> new MissingElementException("Missing user " + username));
	}

	protected net.geant.nmaas.portal.persistent.entity.User getUser(Long userId) {
		if(userId == null)
			throw new MissingElementException("Missing username.");
		
		return users.findById(userId).orElseThrow(() -> new MissingElementException("Missing user id=" + userId));
	}

}