package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView;
import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.api.domain.BulkDeploymentView;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;

import java.util.List;

public interface BulkHistoryService {

    BulkDeployment create(BulkDeploymentView bulk);

    List<BulkDeployment> findAll();

    List<BulkDeployment> findAllByType(BulkType type);

    BulkDeployment find(Long id);

    BulkDeployment createFromEntries(List<BulkDeploymentEntryView> entries, UserViewMinimal creator);

}