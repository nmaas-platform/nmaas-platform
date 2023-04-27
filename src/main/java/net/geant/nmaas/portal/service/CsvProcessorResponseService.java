package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistent.entity.CsvProcessorResponse;

import java.util.List;

public interface CsvProcessorResponseService {

    CsvProcessorResponse save(CsvProcessorResponse csvResponse);
    List<CsvProcessorResponse> saveAll(List<CsvProcessorResponse> csvResponses);
}
