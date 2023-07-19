package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView;
import net.geant.nmaas.portal.api.bulk.CsvApplication;

import java.util.List;

public interface BulkApplicationService {

    List<BulkDeploymentEntryView> handleBulkCreation(String owner, String applicationName, List<CsvApplication> appInstanceSpecs);

}
