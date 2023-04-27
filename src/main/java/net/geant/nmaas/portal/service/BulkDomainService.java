package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.CsvBean;
import net.geant.nmaas.portal.api.bulk.CsvProcessorResponseView;

import java.util.List;

public interface BulkDomainService {

    List<CsvProcessorResponseView> handleBulkCreation(List<CsvBean> domains);

}
