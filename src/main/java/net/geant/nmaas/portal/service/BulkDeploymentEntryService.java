package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistent.entity.BulkDeploymentEntry;

import java.util.List;

public interface BulkDeploymentEntryService {

    BulkDeploymentEntry save(BulkDeploymentEntry entry);
    List<BulkDeploymentEntry> saveAll(List<BulkDeploymentEntry> entries);

}
