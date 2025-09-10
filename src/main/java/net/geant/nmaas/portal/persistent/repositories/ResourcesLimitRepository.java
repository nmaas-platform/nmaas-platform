package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistent.entity.ResourcesLimitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourcesLimitRepository extends JpaRepository<ResourcesLimit, Long> {

    boolean existsByLimitType(ResourcesLimitType limitType);
    boolean existsByDomain_Id(Long domainId);
    boolean existsByDomainGroup_Id(Long domainGroupId);

}