package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.bulk.CsvBean;
import net.geant.nmaas.portal.api.bulk.CsvProcessorResponse;

import java.util.List;

public interface BulkDomainService {

    List<CsvProcessorResponse> handleBulkCreation(List<CsvBean> domains);

}
