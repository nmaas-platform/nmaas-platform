package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentEntry;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentEntryRepository;
import net.geant.nmaas.portal.service.BulkDeploymentEntryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BulkDeploymentEntryServiceImpl implements BulkDeploymentEntryService {

    private final BulkDeploymentEntryRepository repository;

    @Override
    public BulkDeploymentEntry save(BulkDeploymentEntry entry) {
        return repository.save(entry);
    }

    @Override
    public List<BulkDeploymentEntry> saveAll(List<BulkDeploymentEntry> entries) {
        return repository.saveAll(entries);
    }

}
