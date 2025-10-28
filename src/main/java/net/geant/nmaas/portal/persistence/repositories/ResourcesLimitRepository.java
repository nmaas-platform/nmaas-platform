package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourcesLimitRepository extends JpaRepository<ResourcesLimit, Long> {

    boolean existsByLimitType(ResourcesLimitType limitType);
    boolean existsByDomain_Id(Long domainId);
    boolean existsByDomainGroup_Id(Long domainGroupId);

}