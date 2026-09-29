package net.geant.nmaas.portal.service.impl;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.api.exceptions.BundleNotFoundException;
import net.geant.nmaas.portal.api.exceptions.InvalidBundleException;
import net.geant.nmaas.portal.dto.BundleBasicDto;
import net.geant.nmaas.portal.dto.BundleDescriptionDto;
import net.geant.nmaas.portal.dto.BundleDto;
import net.geant.nmaas.portal.mappers.BundleMapper;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Bundle;
import net.geant.nmaas.portal.persistence.entity.BundleDescription;
import net.geant.nmaas.portal.persistence.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistence.repositories.BundleRepository;
import net.geant.nmaas.portal.service.BundleService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@AllArgsConstructor
public class BundleServiceImpl implements BundleService {

    private ApplicationBaseRepository applicationBaseRepository;
    private BundleRepository bundleRepository;
    private ModelMapper modelMapper;
    private BundleMapper mapper;

    @Override
    @Transactional
    public BundleDto create(BundleBasicDto request) {

        Set<ApplicationBase> apps = loadAndValidateApps(request.apps());
        Bundle bundle = new Bundle();
        bundle.setName(request.name());
        bundle.setCodename(request.codeName());
        bundle.setDescriptions(request.descriptions().stream().map(mapper::mapToDescription).toList());
        bundle.setApps(new HashSet<>(apps));
        Bundle result = bundleRepository.save(bundle);

        return mapper.mapToBundleDto(result);
    }

    @Override
    @Transactional
    public BundleDto update(Long id, BundleBasicDto request) {
        Bundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new BundleNotFoundException(id));
        if (bundleRepository.existsByCodenameAndIdNot(request.codeName(), id)) {
            throw new InvalidBundleException("Bundle with codeName '" + request.codeName() + "' already exists");
        }
        Set<ApplicationBase> apps = loadAndValidateApps(request.apps());
        bundle.setName(request.name());
        bundle.setCodename(request.codeName());
        updateDescriptions(bundle, request.descriptions());
        bundle.getApps().clear();
        bundle.getApps().addAll(apps);
        Bundle result = bundleRepository.save(bundle);

        return mapper.mapToBundleDto(result);
    }

    @Override
    @Transactional
    public BundleDto findById(Long id) {
        return mapper.mapToBundleDto(bundleRepository.getReferenceById(id));
    }

    @Override
    public Boolean isApplicationInBundle(Long appId) {
        return bundleRepository.existsByAppsId(appId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BundleBasicDto> findAll(Pageable pageable) {
        return bundleRepository.findAll(pageable)
                .map(mapper::mapToBundleBasicDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BundleBasicDto> findByName(String searchValue, Pageable pageable) {
        return bundleRepository.findAllByNameContainingIgnoreCase(searchValue, pageable)
                .map(mapper::mapToBundleBasicDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BundleBasicDto> findByApplication(Long appBaseId, Pageable pageable) {
        return bundleRepository.findAllByApps_Id(appBaseId, pageable)
                .map(mapper::mapToBundleBasicDto);
    }

    @Override
    @Transactional
    public List<BundleDto> findByApplication(Long appBaseId) {
        return bundleRepository.findAllByApps_Id(appBaseId)
                .stream().map(mapper::mapToBundleDto).toList();
    }

    private Set<ApplicationBase> loadAndValidateApps(Set<Long> appIds) {
        List<ApplicationBase> apps = applicationBaseRepository.findAllById(appIds);

        if (apps.size() != appIds.size()) {
            throw new InvalidBundleException("Some applications do not exist");
        }
        if (apps.size() < 2) {
            throw new InvalidBundleException("Bundle must contain at least 2 applications");
        }
        return new HashSet<>(apps);
    }

    private void updateDescriptions(Bundle bundle, List<BundleDescriptionDto> dtos) {
        Map<String, BundleDescriptionDto> byLanguage = new LinkedHashMap<>();
        for (BundleDescriptionDto dto : dtos) {
            if (byLanguage.put(dto.language(), dto) != null) {
                throw new InvalidBundleException("Duplicate description language: " + dto.language());
            }
        }

        bundle.getDescriptions().removeIf(d -> !byLanguage.containsKey(d.getLanguage()));

        for (BundleDescription existing : bundle.getDescriptions()) {
            BundleDescriptionDto dto = byLanguage.remove(existing.getLanguage());
            existing.setBriefDescription(dto.briefDescription());
            existing.setFullDescription(dto.fullDescription());
        }

        byLanguage.values().forEach(dto -> bundle.getDescriptions().add(mapper.mapToDescription(dto)));
    }
}
