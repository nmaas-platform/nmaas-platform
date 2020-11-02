package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AppInstanceRepository extends JpaRepository<AppInstance, Long> {
	
	List<AppInstance> findAllByOwner(User user);
	List<AppInstance> findAllByDomain(Domain domain);
	List<AppInstance> findAllByOwnerAndDomain(User owner, Domain domain);
	
	Page<AppInstance> findAllByDomain(Domain domain, Pageable pageable);
	Page<AppInstance> findAllByOwner(User owner, Pageable pageable);
	Page<AppInstance> findAllByOwnerAndDomain(User owner, Domain domain, Pageable pageable);

	@Query("select count(ai.id) FROM AppInstance ai JOIN AppDeployment ad on ad.deploymentId = ai.internalId where ad.state = 'APPLICATION_DEPLOYMENT_VERIFIED'")
	int countAllRunning();

}
