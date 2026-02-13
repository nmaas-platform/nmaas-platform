package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
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

    @Query("SELECT ai FROM AppInstance ai JOIN AppDeployment ad ON ad.deploymentId = ai.internalId WHERE ai.domain.codename = :domain AND ad.state NOT IN" +
            "('APPLICATION_REMOVED'," +
            "'APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS'," +
            "'APPLICATION_CONFIGURATION_REMOVED'," +
            "'FAILED_APPLICATION_REMOVED')")
    List<AppInstance> findAllActiveInDomain(@Param(value = "domain") String domain);

    @Query("SELECT count(ai.id) FROM AppInstance ai JOIN AppDeployment ad ON ad.deploymentId = ai.internalId WHERE ai.domain.codename = :domain AND ad.state NOT IN" +
            "('APPLICATION_REMOVED'," +
            "'APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS'," +
            "'APPLICATION_CONFIGURATION_REMOVED'," +
            "'FAILED_APPLICATION_REMOVED')")
    int countAllActiveInDomain(@Param(value = "domain") String domain);

    List<AppInstance> findAllByOwnerAndDomain(User owner, Domain domain);

    List<AppInstance> findAllByApplication(Application application);

    @Query("select count(ai.id) FROM AppInstance ai JOIN AppDeployment ad on ad.deploymentId = ai.internalId where ad.state = 'APPLICATION_DEPLOYMENT_VERIFIED'")
    int countAllRunning();

    @Query("select count(ai.id) FROM AppInstance ai JOIN AppDeployment ad on ad.deploymentId = ai.internalId where ad.state = 'APPLICATION_DEPLOYMENT_VERIFIED' and ai.application.name = ?1")
    int countRunningByName(String name);

    @Modifying
    @Query("update AppInstance ai set ai.application = :application, ai.previousApplicationId = :previousApplicationId where ai.id = :id")
    void updateApplication(@Param(value = "id") Long id, @Param(value = "previousApplicationId") Long previousApplicationId, @Param(value = "application") Application application);

    @Query("SELECT COUNT(ai.id) FROM AppInstance ai JOIN AppDeployment ad ON ad.deploymentId = ai.internalId WHERE ai.name = :name AND ai.domain.codename = :domain AND ad.state NOT IN" +
            "('APPLICATION_REMOVED'," +
            "'APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS'," +
            "'APPLICATION_CONFIGURATION_REMOVED'," +
            "'FAILED_APPLICATION_REMOVED')")
    int isNameAvailableInDomain(@Param(value = "name") String name, @Param(value = "domain") String domain);

    @Query(value = "SELECT COUNT(ai.id) FROM app_instance ai WHERE ai.created_at >= :sinceTime AND ai.created_at < :endTime", nativeQuery = true)
    int countAllDeployedInTimePeriod(@Param("sinceTime") Long sinceTime, @Param("endTime") Long endTime);

    @Query("SELECT COUNT(ai.id) FROM AppInstance ai JOIN AppDeployment ad ON ad.deploymentId = ai.internalId WHERE ai.application.name = ?1")
    int countByName(String name);

    @Query(value = "SELECT * FROM app_instance ai WHERE ai.created_at >= :sinceTime AND ai.created_at <= :endTime", nativeQuery = true)
    List<AppInstance> findAllInTimePeriod(@Param("sinceTime") Long sinceTime, @Param("endTime") Long endTime);

    int countAllByOwner(User user);

    @Query("""
            SELECT a FROM AppInstance a
                        WHERE a.domain.deleted = false
            """)
    Page<AppInstance> findAllNotDeleted(Pageable pageable);

    Page<AppInstance> findAllByDomain(Domain domain, Pageable pageable);

    Page<AppInstance> findAllByOwner(User owner, Pageable pageable);

    Page<AppInstance> findAllByOwnerAndDomain(User owner,
                                              Domain domain,
                                              Pageable pageable);

    @Query("""
                SELECT a
                FROM AppInstance a
                JOIN AppDeployment l ON l.instanceId = a.id
                WHERE a.domain.deleted = false
                  AND (:search IS NULL OR :search = '' OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))
                  AND (
                       (:deployed = true
                                   AND l.state NOT IN (
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.FAILED_APPLICATION_REMOVED
                             )
                       )
                    OR (:deployed = false
                                AND  l.state IN (
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.FAILED_APPLICATION_REMOVED
                             )
                       )
                  )
            """)
    Page<AppInstance> findAllNotDeletedByDeploy(@Param("search") String search,
                                                Pageable pageable,
                                                boolean deployed);

    @Query("""
            SELECT a
            FROM AppInstance a 
            WHERE a.domain = :domain
            AND (:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<AppInstance> findAllByDomainAndSearch(@Param("domain") Domain domain,
                                               @Param("search") String search,
                                               Pageable pageable);

    @Query("""
            SELECT a
            FROM AppInstance a
            WHERE a.domain = :domain
            AND a.domain.deleted = false
            AND (:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<AppInstance> findAllNotDeletedByDomainAndSearch(@Param("domain") Domain domain,
                                                         @Param("search") String search,
                                                         Pageable pageable);

    @Query("""
            SELECT a
            FROM AppInstance a
            JOIN AppDeployment l ON l.instanceId = a.id
            WHERE a.domain = :domain
            AND a.domain.deleted = false
            AND (:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (
                       (:deployed = true
                             AND l.state NOT IN (
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.FAILED_APPLICATION_REMOVED
                             )
                       )
                    OR (:deployed = false
                             AND l.state IN (
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.FAILED_APPLICATION_REMOVED
                             )
                       )
                  )
            """)
    Page<AppInstance> findAllNotDeletedByDomainAndByDeployAndSearch(@Param("domain") Domain domain,
                                                                    @Param("search") String search,
                                                                    boolean deployed,
                                                                    Pageable pageable);

    @Query("""
            SELECT a
            FROM AppInstance a
            WHERE a.owner = :user
            AND a.domain.deleted = false
            AND (:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<AppInstance> findAllNotDeletedByOwnerAndSearch(@Param("user") User user,
                                                        @Param("search") String search,
                                                        Pageable pageable);

    @Query("""
            SELECT a
            FROM AppInstance a
            WHERE a.owner = :user
            AND a.domain.deleted = false
            AND (:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (
                       (:deployed = true  AND EXISTS (
                           SELECT 1
                           FROM net.geant.nmaas.orchestration.entities.AppDeployment l
                           WHERE l.instanceId = a.id
                             AND l.state NOT IN (
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.FAILED_APPLICATION_REMOVED
                             )
                       ))
                    OR (:deployed = false AND EXISTS (
                           SELECT 1
                           FROM net.geant.nmaas.orchestration.entities.AppDeployment l
                           WHERE l.instanceId = a.id
                             AND l.state IN (
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.FAILED_APPLICATION_REMOVED
                             )
                       ))
                  )
            """)
    Page<AppInstance> findAllNotDeletedByOwnerAndByDeployAndSearch(@Param("user") User user,
                                                                   @Param("search") String search,
                                                                   boolean deployed,
                                                                   Pageable pageable);

    @Query("""
            SELECT a
            FROM AppInstance a
            WHERE a.owner = :user
            AND a.domain = :domain
            AND a.domain.deleted = false
            AND (:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (
                       (:deployed = true  AND EXISTS (
                           SELECT 1
                           FROM net.geant.nmaas.orchestration.entities.AppDeployment l
                           WHERE l.instanceId = a.id
                             AND l.state NOT IN (
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.FAILED_APPLICATION_REMOVED
                             )
                       ))
                    OR (:deployed = false AND EXISTS (
                           SELECT 1
                           FROM net.geant.nmaas.orchestration.entities.AppDeployment l
                           WHERE l.instanceId = a.id
                             AND l.state IN (
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.APPLICATION_CONFIGURATION_REMOVED,
                               net.geant.nmaas.orchestration.entities.AppDeploymentState.FAILED_APPLICATION_REMOVED
                             )
                       ))
                  )
            """)
    Page<AppInstance> findAllNotDeletedByOwnerAndDomainAndByDeployAndSearch(@Param("user") User user,
                                                                            @Param("search") String search,
                                                                            @Param("domain") Domain domain,
                                                                            boolean deployed,
                                                                            Pageable pageable);

    @Query("""
            SELECT a
            FROM AppInstance a
            WHERE a.owner = :user
            AND (:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<AppInstance> findAllByOwnerAndSearch(@Param("user") User user,
                                              @Param("search") String search,
                                              Pageable pageable);

    @Query("""
            SELECT a
            FROM AppInstance a
            WHERE a.owner = :user
            AND a.domain = :domain
            AND (:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<AppInstance> findAllByOwnerAndDomainAndSearch(@Param("user") User user,
                                                       @Param("domain") Domain domain,
                                                       @Param("search") String search,
                                                       Pageable pageable);
}
