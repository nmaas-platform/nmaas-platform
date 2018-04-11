package net.geant.nmaas.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.projections.ApplicationBriefProjection;

public interface ApplicationService {
	
	Application create(String name);
	Application update(Application app);
	void delete(Long id);
	
	Optional<ApplicationBriefProjection> findApplicationBrief(Long applicationId);
	Optional<Application> findApplication(Long applicationId);
	
	Page<Application> findAll(Pageable pageable);
	List<Application> findAll();
	
	List<ApplicationBriefProjection> findAllBrief();
	List<ApplicationBriefProjection> findAllBrief(List<Long> appIds);
	Page<ApplicationBriefProjection> findAllBrief(Pageable pageable);
	
	
}
