package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.DomainGroup;
import net.geant.nmaas.portal.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DomainGroupRepository extends JpaRepository<DomainGroup, Long> {

    boolean existsByName(String name);

    boolean existsByCodename(String codename);

    Optional<DomainGroup> findByCodename(String codeName);

    @Query("SELECT DISTINCT dg.id FROM DomainGroup dg JOIN dg.domains d WHERE d.codename = :codename")
    List<String> findDomainGroupIdsByDomainCodename(@Param("codename") String codename);

    Page<DomainGroup> findAllByManagers(List<User> managers,
                                        Pageable pageable);
}
