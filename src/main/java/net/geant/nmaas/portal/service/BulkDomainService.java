package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.users.UserInfoDto;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.api.bulk.model.BulkDeploymentViewS;

import java.util.List;

public interface BulkDomainService {

    BulkDeploymentViewS handleBulkCreation(List<CsvDomain> domainSpecs, UserInfoDto creator);

}
