package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.Bundle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BundleRepository extends JpaRepository<Bundle, Long> {

    Page<Bundle> findAllByApps_Id(Long appsId, Pageable pageable);

    List<Bundle> findAllByApps_Id(Long appsId);

    Page<Bundle> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByCodenameAndIdNot(String codename, Long id);

    boolean existsByAppsId(Long appId);
}
