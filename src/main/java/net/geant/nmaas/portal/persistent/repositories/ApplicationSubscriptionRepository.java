package net.geant.nmaas.portal.persistent.repositories;

import java.util.List;
import java.util.Optional;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription.Id;
import net.geant.nmaas.portal.persistent.entity.Domain;

public interface ApplicationSubscriptionRepository extends JpaRepository<ApplicationSubscription, ApplicationSubscription.Id>{
	
	/*
	 * Exists 
	 */
	
	@Query("SELECT CASE WHEN COUNT(appSub) > 0 THEN true ELSE false END FROM ApplicationSubscription appSub WHERE appSub.id.domain = ?1 AND appSub.id.application = ?2")
	boolean existsByDomainAndApplication(Domain domain, ApplicationBase application);

	@Query("SELECT CASE WHEN COUNT(appSub) > 0 THEN true ELSE false END FROM ApplicationSubscription appSub WHERE appSub.id.domain.id = ?1 AND appSub.id.application.id = ?2")
	boolean existsByDomainAndApplicationId(Long domainId, Long applicationId);

	/*
	 * Is deleted
	 */
	
	@Query("SELECT CASE WHEN COUNT(appSub) > 0 THEN true ELSE false END FROM ApplicationSubscription appSub WHERE appSub.id.domain = ?1 AND appSub.id.application = ?2 AND appSub.deleted = TRUE")
	boolean isDeleted(Domain domain, ApplicationBase application);
	
	@Query("SELECT CASE WHEN COUNT(appSub) > 0 THEN true ELSE false END FROM ApplicationSubscription appSub WHERE appSub.id.domain.id = ?1 AND appSub.id.application.id = ?2 AND appSub.deleted = TRUE")
	boolean isDeleted(Long domainId, Long applicationId);
	
	@Query("SELECT CASE WHEN COUNT(appSub) > 0 THEN true ELSE false END FROM ApplicationSubscription appSub WHERE appSub.id = ?1 AND appSub.deleted = TRUE")
	boolean isDeleted(Id id);
	
	/*
	 * Get one
	 */
	
	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain = :domain AND appSub.id.application = :application")
	Optional<ApplicationSubscription> findByDomainAndApplication(@Param("domain") Domain domain, @Param("application") ApplicationBase application);
	
	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain.id = :domainId AND appSub.id.application.id = :applicationId")
	Optional<ApplicationSubscription> findByDomainAndApplicationId(@Param("domainId") Long domainId, @Param("applicationId") Long applicationId);

	/*
	 * Get all by domain
	 */
	
	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain = :domain AND appSub.deleted = FALSE")
	List<ApplicationSubscription> findAllByDomain(@Param("domain") Domain domain);

	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain = :domain AND appSub.deleted = FALSE")
	Page<ApplicationSubscription> findAllByDomain(@Param("domain") Domain domain, Pageable pageable);

	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain.id = :domainId AND appSub.deleted = FALSE")
	List<ApplicationSubscription> findAllByDomain(@Param("domainId") Long domainId);
	
	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain.id = :domainId AND appSub.deleted = FALSE")
	Page<ApplicationSubscription> findAllByDomain(@Param("domainId") Long domainId, Pageable pageable);

	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain = :domain AND appSub.active = :active AND appSub.deleted = FALSE")
	List<ApplicationSubscription> findAllByDomain(@Param("domain") Domain domain, @Param("active") boolean active);
	
	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain = :domain AND appSub.active = :active AND appSub.deleted = FALSE")
	Page<ApplicationSubscription> findAllByDomain(@Param("domain") Domain domain, @Param("active") boolean active, Pageable pageable);

	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain.id = :domainId AND appSub.active = :active AND appSub.deleted = FALSE")
	List<ApplicationSubscription> findAllByDomain(@Param("domainId") Long domainId, @Param("active") boolean active);

	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain.id = :domainId AND appSub.active = :active AND appSub.deleted = FALSE")
	Page<ApplicationSubscription> findAllByDomain(@Param("domainId") Long domainId, @Param("active") boolean active, Pageable pageable);
	
	List<ApplicationSubscription> findAllByIdDomainAndDeletedFalse(Domain domain);
	List<ApplicationSubscription> findAllByIdDomainAndActiveAndDeletedFalse(Domain domain, boolean active);
	Page<ApplicationSubscription> findAllByIdDomainAndDeletedFalse(Domain domain, Pageable pageable);
	Page<ApplicationSubscription> findAllByIdDomainAndActiveAndDeletedFalse(Domain domain, boolean active, Pageable pageable);
	
	
	/*
	 * Get all by application
	 */

	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.application = ?1")
	List<ApplicationSubscription> findAllByApplication(ApplicationBase application);

	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.application = ?1")
	Page<ApplicationSubscription> findAllByApplication(ApplicationBase application, Pageable pageable);

	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.application.id = ?1")
	List<ApplicationSubscription> findAllByApplication(Long applicationId);	
	
	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.application.id = ?1")
	Page<ApplicationSubscription> findAllByApplication(Long applicationId, Pageable pageable);

	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.application = :application AND appSub.active = :active")
	List<ApplicationSubscription> findAllByApplication(@Param("application") ApplicationBase application, @Param("active") boolean active);

	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.application = :application AND appSub.active = :active")
	Page<ApplicationSubscription> findAllByApplication(@Param("application") ApplicationBase application, @Param("active") boolean active, Pageable pageable);

	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.application.id = :applicationId AND appSub.active = :active")
	List<ApplicationSubscription> findAllByApplication(@Param("applicationId") Long applicationId, @Param("active") boolean active);

	@Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.application.id = :applicationId AND appSub.active = :active")
	Page<ApplicationSubscription> findAllByApplication(@Param("applicationId") Long applicationId, @Param("active") boolean active, Pageable pageable);

	List<ApplicationSubscription> findAllByIdApplication(ApplicationBase application);
	Page<ApplicationSubscription> findAllByIdApplication(ApplicationBase application, Pageable pageable);

	List<ApplicationSubscription> findAllByIdApplicationAndActive(ApplicationBase application, boolean active);
	Page<ApplicationSubscription> findAllByIdApplicationAndActive(ApplicationBase application, boolean active, Pageable pageable);
	
	//TODO: try to fix to return projection after upgrading spring boot 2.x
	@Query("SELECT DISTINCT appSub.id.application FROM ApplicationSubscription appSub WHERE appSub.deleted=FALSE")
	List<ApplicationBase> findApplicationBriefAllBy();

	//TODO: try to fix to return projection after upgrading spring boot 2.x
	@Query("SELECT DISTINCT appSub.id.application FROM ApplicationSubscription appSub WHERE appSub.id.domain.id = :domainId and appSub.deleted=FALSE")
	List<ApplicationBase> findApplicationBriefAllByDomain(@Param("domainId") Long domainId);

	
}
