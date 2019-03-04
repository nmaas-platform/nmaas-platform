package net.geant.nmaas.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;

public interface ApplicationInstanceService {

	AppInstance create(Long domainId, Long applicationId, String name);
	AppInstance create(Domain domain, Application application, String name);
	
	void delete(Long appInstanceId);

	void update(AppInstance appInstance);
	
	Optional<AppInstance> find(Long appInstanceId);
	
	List<AppInstance> findAll();
	Page<AppInstance> findAll(Pageable pageable);
	
	List<AppInstance> findAllByOwner(Long userId);
	List<AppInstance> findAllByOwner(User owner);
	List<AppInstance> findAllByOwner(Long userId, Long domainId);
	List<AppInstance> findAllByOwnerAndDomain(User owner, Domain domain);

	Page<AppInstance> findAllByOwner(Long userId, Pageable pageable);
	Page<AppInstance> findAllByOwner(User owner, Pageable pageable);
	Page<AppInstance> findAllByOwner(Long userId, Long domainId, Pageable pageable);
	Page<AppInstance> findAllByOwner(User owner, Domain domain, Pageable pageable);
	
	List<AppInstance> findAllByDomain(Long domainId);
	List<AppInstance> findAllByDomain(Domain domain);
	Page<AppInstance> findAllByDomain(Long domainId, Pageable pageable);
	Page<AppInstance> findAllByDomain(Domain domain, Pageable pageable);
	
	
}
