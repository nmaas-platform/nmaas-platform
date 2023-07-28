package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.CsvApplication;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentViewS;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;

import java.util.List;

public interface BulkApplicationService {

    BulkDeploymentViewS handleBulkDeployment(String applicationName, List<CsvApplication> appInstanceSpecs, UserViewMinimal creator);

}
