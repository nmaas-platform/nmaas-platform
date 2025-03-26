package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppInstanceRepository extends JpaRepository<AppInstance, Long> {

	Optional<AppInstance> findByInternalId(Identifier internalId);

	List<AppInstance> findAllByOwner(User user);
	List<AppInstance> findAllByDomain(Domain domain);
	List<AppInstance> findAllByOwnerAndDomain(User owner, Domain domain);

	Page<AppInstance> findAllByDomain(Domain domain, Pageable pageable);
	Page<AppInstance> findAllByOwner(User owner, Pageable pageable);
	Page<AppInstance> findAllByOwnerAndDomain(User owner, Domain domain, Pageable pageable);

	List<AppInstance> findAllByApplication(Application application);

	@Query("select count(ai.id) FROM AppInstance ai JOIN AppDeployment ad on ad.deploymentId = ai.internalId where ad.state = 'APPLICATION_DEPLOYMENT_VERIFIED'")
	int countAllRunning();

	@Query("select count(ai.id) FROM AppInstance ai JOIN AppDeployment ad on ad.deploymentId = ai.internalId where ad.state = 'APPLICATION_DEPLOYMENT_VERIFIED' and ai.application.name = ?1")
	int countRunningByName(String name);

	@Modifying
	@Query("update AppInstance ai set ai.application = :application, ai.previousApplicationId = :previousApplicationId where ai.id = :id")
	void updateApplication(@Param(value = "id") long id, @Param(value = "previousApplicationId") long previousApplicationId, @Param(value = "application") Application application);

	@Query("SELECT COUNT(ai.id) FROM AppInstance ai JOIN AppDeployment ad ON ad.deploymentId = ai.internalId WHERE ai.name = :name AND ai.domain.codename = :domain AND ad.state NOT IN" +
			"('APPLICATION_REMOVED'," +
			"'APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS'," +
			"'APPLICATION_CONFIGURATION_REMOVED'," +
			"'FAILED_APPLICATION_REMOVED')" )
	int isNameAvailableInDomain(@Param(value = "name") String name, @Param(value = "domain") String domain);

	@Query("select count(ai.id) FROM AppInstance ai  where ai.createdAt >= :sinceTime")
	int countAllDeployedSinceTime(@Param("sinceTime") long sinceTime);

	@Query("select count(ai.id) FROM AppInstance ai JOIN AppDeployment ad on ad.deploymentId = ai.internalId where ai.application.name = ?1")
	int countByName(String name);

	@Query("select ai FROM AppInstance ai where ai.createdAt >= :sinceTime")
	List<AppInstance> findAllInTimePeriod(@Param("sinceTime") long sinceTime);

	int countAllByOwner(User user);

}
