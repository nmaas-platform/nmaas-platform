package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourcesLimitRepository extends JpaRepository<ResourcesLimit, Long> {

    boolean existsByLimitType(ResourcesLimitType limitType);
    boolean existsByDomain_Id(Long domainId);
    boolean existsByDomainGroup_Id(Long domainGroupId);

    List<ResourcesLimit> findByLimitType(ResourcesLimitType limitType);

    ResourcesLimit findByDomain_Codename(String codename);

    @Query("SELECT rl FROM ResourcesLimit rl " +
            "JOIN rl.domainGroup dg " +
            "JOIN dg.domains d " +
            "WHERE d.codename = :codename")
    List<ResourcesLimit> findForGroupsBasedOnDomain(@Param("codename") String codename);


}
