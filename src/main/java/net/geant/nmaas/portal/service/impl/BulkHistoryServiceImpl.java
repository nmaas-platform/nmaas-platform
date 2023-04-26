package net.geant.nmaas.portal.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.api.domain.BulkDeploymentView;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentState;
import net.geant.nmaas.portal.persistent.entity.CsvProcessorResponse;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentRepository;
import net.geant.nmaas.portal.service.BulkHistoryService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class BulkHistoryServiceImpl implements BulkHistoryService {

    private final BulkDeploymentRepository repository;
    private final ModelMapper modelMapper;

    @Override
    public BulkDeployment createEntityFromCsvResponse(List<CsvProcessorResponse> csvResponses, UserViewMinimal creator) {
        BulkDeploymentView bulkDeploymentView = createBulkDeployment(csvResponses, creator);
        BulkDeployment entity = modelMapper.map(bulkDeploymentView, BulkDeployment.class);
        return repository.save(entity);
    }

    @Override
    public BulkDeployment create(BulkDeploymentView bulk) {
        BulkDeployment entity = modelMapper.map(bulk, BulkDeployment.class);
        return repository.save(entity);
    }


    @Override
    public List<BulkDeployment> findAll() {
        return repository.findAll();
    }

    @Override
    public List<BulkDeployment> findAllByType(BulkType type) {
        return findAll().stream()
                .filter(bulk -> bulk.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public BulkDeployment find(Long id) {
        return repository.findById(id).orElseThrow(RuntimeException::new);
    }

    private BulkDeploymentView createBulkDeployment(
            List<CsvProcessorResponse> csvResponses,
            UserViewMinimal creator
    ) {
        BulkDeploymentView bulkDeploymentView = new BulkDeploymentView();
        bulkDeploymentView.setType(BulkType.DOMAIN);
        bulkDeploymentView.setState(BulkDeploymentState.PROCESSING);
        bulkDeploymentView.setCreator(creator);
        bulkDeploymentView.setProcessorResponses(csvResponses);
        return bulkDeploymentView;
    }
}
