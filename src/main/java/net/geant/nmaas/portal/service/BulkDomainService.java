package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.CsvBean;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView;

import java.util.List;

public interface BulkDomainService {

    List<BulkDeploymentEntryView> handleBulkCreation(List<CsvBean> domains);

}
