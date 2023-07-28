package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentViewS;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;

import java.util.List;

public interface BulkDomainService {

    BulkDeploymentViewS handleBulkCreation(List<CsvDomain> domainSpecs, UserViewMinimal creator);

}
