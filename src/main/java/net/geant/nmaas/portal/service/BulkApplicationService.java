package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView;
import net.geant.nmaas.portal.api.bulk.CsvBean;

import java.util.List;

public interface BulkApplicationService {

    List<BulkDeploymentEntryView> handleBulkCreation(List<CsvBean> apps);

}
