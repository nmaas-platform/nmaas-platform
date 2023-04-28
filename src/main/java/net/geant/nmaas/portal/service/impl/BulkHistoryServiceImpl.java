package net.geant.nmaas.portal.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView;
import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.api.domain.BulkDeploymentView;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentEntry;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentState;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentRepository;
import net.geant.nmaas.portal.service.BulkDeploymentEntryService;
import net.geant.nmaas.portal.service.BulkHistoryService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class BulkHistoryServiceImpl implements BulkHistoryService {

    private final BulkDeploymentRepository repository;
    private final BulkDeploymentEntryService bulkDeploymentEntryService;
    private final ModelMapper modelMapper;

    @Override
    public BulkDeployment createFromEntries(List<BulkDeploymentEntryView> entries, UserViewMinimal creator) {
        BulkDeploymentView bulkDeploymentView = createBulkDeployment(creator);
        List<BulkDeploymentEntry> bulkDeploymentEntries = bulkDeploymentEntryService.saveAll(entries.stream()
                .map(response -> modelMapper.map(response, BulkDeploymentEntry.class))
                .collect(Collectors.toList()));
        BulkDeployment entity = modelMapper.map(bulkDeploymentView, BulkDeployment.class);
        entity.setEntries(bulkDeploymentEntries);
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

    private BulkDeploymentView createBulkDeployment(UserViewMinimal creator) {
        BulkDeploymentView bulkDeploymentView = new BulkDeploymentView();
        bulkDeploymentView.setType(BulkType.DOMAIN);
        bulkDeploymentView.setState(BulkDeploymentState.COMPLETED);
        bulkDeploymentView.setCreator(creator);
        bulkDeploymentView.setCreationDate(OffsetDateTime.now());
        return bulkDeploymentView;
    }

}
