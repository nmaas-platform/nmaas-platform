package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistence.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationSubscriptionRepository extends JpaRepository<ApplicationSubscription, ApplicationSubscription.Id> {

    @Query("SELECT CASE WHEN COUNT(appSub) > 0 THEN true ELSE false END FROM ApplicationSubscription appSub WHERE appSub.id.domain = ?1 AND appSub.id.application = ?2")
    boolean existsByDomainAndApplication(Domain domain, ApplicationBase application);

    @Query("SELECT CASE WHEN COUNT(appSub) > 0 THEN true ELSE false END FROM ApplicationSubscription appSub WHERE appSub.id.domain.id = ?1 AND appSub.id.application.id = ?2")
    boolean existsByDomainAndApplicationId(Long domainId, Long applicationId);

    @Query("SELECT CASE WHEN COUNT(appSub) > 0 THEN true ELSE false END FROM ApplicationSubscription appSub WHERE appSub.id.domain = ?1 AND appSub.id.application = ?2 AND appSub.deleted = TRUE")
    boolean isDeleted(Domain domain, ApplicationBase application);

    @Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain = :domain AND appSub.id.application = :application")
    Optional<ApplicationSubscription> findByDomainAndApplication(@Param("domain") Domain domain, @Param("application") ApplicationBase application);

    @Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain.id = :domainId AND appSub.id.application.id = :applicationId")
    Optional<ApplicationSubscription> findByDomainAndApplicationId(@Param("domainId") Long domainId, @Param("applicationId") Long applicationId);

    @Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain = :domain AND appSub.deleted = FALSE")
    List<ApplicationSubscription> findAllByDomain(@Param("domain") Domain domain);

    @Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.domain.id = :domainId AND appSub.deleted = FALSE")
    List<ApplicationSubscription> findAllByDomain(@Param("domainId") Long domainId);

    /*
     * Get all by application
     */
    @Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.application = ?1")
    List<ApplicationSubscription> findAllByApplication(ApplicationBase application);

    @Query("SELECT appSub FROM ApplicationSubscription appSub WHERE appSub.id.application.id = ?1")
    List<ApplicationSubscription> findAllByApplication(Long applicationId);

    //TODO: try to fix to return projection after upgrading spring boot 2.x
    @Query("SELECT DISTINCT appSub.id.application FROM ApplicationSubscription appSub WHERE appSub.deleted=FALSE")
    List<ApplicationBase> findApplicationBriefAllBy();

    //TODO: try to fix to return projection after upgrading spring boot 2.x
    @Query("SELECT DISTINCT appSub.id.application FROM ApplicationSubscription appSub WHERE appSub.id.domain.id = :domainId and appSub.deleted=FALSE")
    List<ApplicationBase> findApplicationBriefAllByDomain(@Param("domainId") Long domainId);

}
