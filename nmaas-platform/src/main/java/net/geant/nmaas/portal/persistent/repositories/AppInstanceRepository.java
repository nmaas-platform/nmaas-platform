package net.geant.nmaas.portal.persistent.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;

public interface AppInstanceRepository extends JpaRepository<AppInstance, Long> {
	
	List<AppInstance> findAllByOwner(User user);
	List<AppInstance> findAllByDomain(Domain domain);
	List<AppInstance> findAllByOwnerAndDomain(User owner, Domain domain);
	
	Page<AppInstance> findAllByDomain(Domain domain, Pageable pageable);
	Page<AppInstance> findAllByOwner(User owner, Pageable pageable);
	Page<AppInstance> findAllByOwnerAndDomain(User owner, Domain domain, Pageable pageable);
}
