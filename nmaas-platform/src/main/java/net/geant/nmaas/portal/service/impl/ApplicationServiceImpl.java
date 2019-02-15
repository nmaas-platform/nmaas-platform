package net.geant.nmaas.portal.service.impl;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.ApplicationService;

@AllArgsConstructor
@Service
public class ApplicationServiceImpl implements ApplicationService {

	private ApplicationRepository appRepo;

	@Override
	public Application create(String name, String version) {
		checkParam(name, version);
		return appRepo.save(new Application(name, version));
	}

	@Override
	public Application update(Application app) {
		checkParam(app);
		return appRepo.save(app);
	}

	@Override
	public void delete(Long id) {
		checkParam(id);
		appRepo.findById(id).ifPresent((app) -> { app.setState(ApplicationState.DELETED); appRepo.save(app); });
	}

	@Override
	public Optional<Application> findApplication(Long applicationId) {
		if (applicationId != null)
			return appRepo.findById(applicationId);
		else
			throw new IllegalArgumentException("applicationId is null");
	}

	@Override
	public Page<Application> findAll(Pageable pageable) {
		return appRepo.findAll(pageable);
	}

	@Override
	public List<Application> findAll() {
		return appRepo.findAll();
	}

	@Override
	public void setMissingProperties(ApplicationView app){
		setMissingDescriptions(app);
	}

	private void checkParam(String name, String version) {
		if(name == null)
			throw new IllegalArgumentException("name is null");
		if(version == null)
		    throw new IllegalArgumentException("version is null");
		if(appRepo.existsByNameAndVersion(name, version))
		    throw new IllegalStateException("Application " + name + " in version " + version + " already exists.");
	}
	
	private void checkParam(Long id) {
		if(id == null)
			throw new IllegalArgumentException("id is null");
	}
	
	private void checkParam(Application app) {
		if(app == null)
			throw new IllegalArgumentException("app is null");
		if(!appRepo.existsByNameAndVersion(app.getName(), app.getVersion()))
		    throw new IllegalStateException("Application doesn't exist");
	}

	private void setMissingDescriptions(ApplicationView app){
		AppDescriptionView appDescription = app.getDescriptions().stream()
				.filter(description -> description.getLanguage().equals("en"))
				.findFirst().orElseThrow(() -> new IllegalStateException("English description is missing"));
		app.getDescriptions()
				.forEach(description ->{
					if(StringUtils.isEmpty(description.getBriefDescription())){
						description.setBriefDescription(appDescription.getBriefDescription());
					}
					if(StringUtils.isEmpty(description.getFullDescription())){
						description.setFullDescription(appDescription.getFullDescription());
					}
				});
	}
}
