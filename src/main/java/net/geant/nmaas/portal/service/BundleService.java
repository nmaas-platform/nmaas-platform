package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.dto.BundleBasicDto;
import net.geant.nmaas.portal.dto.BundleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BundleService {

    BundleDto create(BundleBasicDto bundle);

    BundleDto update(Long id, BundleBasicDto bundle);

    BundleDto findById(Long id);

    Boolean isApplicationInBundle(Long appId);

    Page<BundleBasicDto> findAll(Pageable pageable);

    Page<BundleBasicDto> findByName(String searchValue, Pageable pageable);

    Page<BundleBasicDto> findByApplication(Long appBaseId, Pageable pageable);

    List<BundleDto> findByApplication(Long appBaseId);
}
