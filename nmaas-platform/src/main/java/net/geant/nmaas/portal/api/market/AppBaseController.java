package net.geant.nmaas.portal.api.market;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;

public class AppBaseController {

	@Autowired
	protected ModelMapper modelMapper;
	@Autowired
	protected ApplicationRepository appRepo;

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

}