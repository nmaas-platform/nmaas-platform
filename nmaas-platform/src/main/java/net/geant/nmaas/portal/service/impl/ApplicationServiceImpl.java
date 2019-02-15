package net.geant.nmaas.portal.service.impl;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
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
	public Application create(String name) {
		checkParam(name);
		return appRepo.save(new Application(name));
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

	private void checkParam(String name) {
		if(name == null)
			throw new IllegalArgumentException("name is null");
	}
	
	private void checkParam(Long id) {
		if(id == null)
			throw new IllegalArgumentException("id is null");
	}
	
	private void checkParam(Application app) {
		if(app == null)
			throw new IllegalArgumentException("app is null");
	}
}
