package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourcesLimitRepository extends JpaRepository<ResourcesLimit, Long> {

    boolean existsByLimitType(ResourcesLimitType limitType);

    boolean existsByDomain_Id(Long domainId);

    boolean existsByDomainGroup_Id(Long domainGroupId);

    List<ResourcesLimit> findByLimitType(ResourcesLimitType limitType);

    Optional<ResourcesLimit> findByDomain_Codename(String codename);

    Optional<ResourcesLimit> findByDomain_Id(Long domainId);

    @Query("SELECT rl FROM ResourcesLimit rl " +
            "JOIN rl.domainGroup dg " +
            "JOIN dg.domains d " +
            "WHERE d.codename = :codename")
    List<ResourcesLimit> findForGroupsBasedOnDomain(@Param("codename") String codename);

    Optional<ResourcesLimit> findOneByLimitType(ResourcesLimitType resourcesLimitType);
}
