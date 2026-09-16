package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.bulks.BulkDeploymentBaseDto;
import net.geant.nmaas.api.dto.users.UserInfoDto;
import net.geant.nmaas.portal.api.bulk.CsvDomain;

import java.util.List;

public interface BulkDomainService {

    BulkDeploymentBaseDto handleBulkCreation(List<CsvDomain> domainSpecs, UserInfoDto creator);

}
