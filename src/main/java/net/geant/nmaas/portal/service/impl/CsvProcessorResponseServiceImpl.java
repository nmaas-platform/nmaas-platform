package net.geant.nmaas.portal.service.impl;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.persistent.entity.CsvProcessorResponse;
import net.geant.nmaas.portal.persistent.repositories.CsvProcessorResponseRepository;
import net.geant.nmaas.portal.service.CsvProcessorResponseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CsvProcessorResponseServiceImpl implements CsvProcessorResponseService {

    private final CsvProcessorResponseRepository repository;

    @Override
    public CsvProcessorResponse save(CsvProcessorResponse csvResponse) {
        return repository.save(csvResponse);
    }

    @Override
    public List<CsvProcessorResponse> saveAll(List<CsvProcessorResponse> csvResponses) {
        return repository.saveAll(csvResponses);
    }
}
