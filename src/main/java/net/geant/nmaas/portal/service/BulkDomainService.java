package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView;
import net.geant.nmaas.portal.api.bulk.CsvDomain;

import java.util.List;

public interface BulkDomainService {

    List<BulkDeploymentEntryView> handleBulkCreation(List<CsvDomain> domainSpecs);

}
