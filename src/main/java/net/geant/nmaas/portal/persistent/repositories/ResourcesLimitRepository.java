package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.ResourcesLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourcesLimitRepository extends JpaRepository<ResourcesLimit, Long> {

    boolean existsByIsGlobalTrue();
    boolean existsByDomain_Id(Long domainId);
    boolean existsByDomainGroup_Id(Long domainGroupId);

    boolean existsByIsGlobalTrueAndIdNot(Long id);
    boolean existsByDomain_IdAndIdNot(Long domainId, Long id);
    boolean existsByDomainGroup_IdAndIdNot(Long domainGroupId, Long id);

}