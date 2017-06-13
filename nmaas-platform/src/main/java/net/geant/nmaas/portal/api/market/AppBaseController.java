package net.geant.nmaas.portal.api.market;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;

public class AppBaseController {

	@Autowired
	protected ModelMapper modelMapper;
	
	@Autowired
	protected ApplicationRepository appRepo;

	@Autowired
	protected UserRepository userRepo;
	
	public AppBaseController() {
		super();
	}

	protected net.geant.nmaas.portal.persistent.entity.Application getApp(Long appId) throws MissingElementException {
		if(appId == null)
			throw new MissingElementException("Missing application id.");
		
		net.geant.nmaas.portal.persistent.entity.Application app = appRepo.findOne(appId);
		if(app == null)
			throw new MissingElementException("Application id=" + appId + " not found.");
		return app;
	}

	protected net.geant.nmaas.portal.persistent.entity.User getUser(String username) throws MissingElementException {
		if(username == null)
			throw new MissingElementException("Missing username.");
		
		Optional<User> user = userRepo.findByUsername(username);
		if(!user.isPresent())
			throw new MissingElementException("Missing user " + username);
		
		return user.get();
	}

	protected net.geant.nmaas.portal.persistent.entity.User getUser(Long userId) throws MissingElementException {
		if(userId == null)
			throw new MissingElementException("Missing username.");
		
		User user = userRepo.findOne(userId);
		if(user == null)
			throw new MissingElementException("Missing user id=" + userId);
		
		return user;
	}

	
}