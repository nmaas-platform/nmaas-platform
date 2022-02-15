package net.geant.nmaas.orchestration;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.AllArgsConstructor;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentHistory;
import net.geant.nmaas.orchestration.entities.AppDeploymentOwner;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.projections.AppDeploymentCount;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.entity.SSHKeyEntity;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.SSHKeyRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DefaultAppDeploymentRepositoryManager implements AppDeploymentRepositoryManager {

    private AppDeploymentRepository repository;

    private UserRepository userRepository;

    private SSHKeyRepository sshKeyRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void store(AppDeployment appDeployment) {
        if(repository.findByDeploymentId(appDeployment.getDeploymentId()).isPresent()) {
            throw new InvalidDeploymentIdException("Deployment with id " + appDeployment.getDeploymentId() + " already exists in the repository.");
        }
        appDeployment.addChangeOfStateToHistory(null, appDeployment.getState());
        repository.save(appDeployment);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void update(AppDeployment appDeployment) {
        repository.save(appDeployment);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateState(Identifier deploymentId, AppDeploymentState currentState) {
        AppDeployment appDeployment = load(deploymentId);
        appDeployment.addChangeOfStateToHistory(appDeployment.getState(), currentState);
        appDeployment.setState(currentState);
        repository.save(appDeployment);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateApplicationId(Identifier deploymentId, Identifier applicationId) {
        AppDeployment appDeployment = load(deploymentId);
        appDeployment.setApplicationId(applicationId);
        repository.save(appDeployment);
    }

    @Override
    public AppDeployment load(Identifier deploymentId) {
        return repository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException(deploymentNotFoundMessage(deploymentId)));
    }

    @Override
    public List<AppDeployment> loadByState(AppDeploymentState state) {
        return repository.findByState(state);
    }

    @Override
    public AppDeploymentOwner loadOwner(Identifier deploymentId) {
        User owner = userRepository.findByUsername(load(deploymentId).getOwner())
                .orElseThrow(() -> new InvalidDeploymentIdException("Owner for " + deploymentId + " not found in the repository."));
        List<SSHKeyEntity> ownerSshKeys = sshKeyRepository.findAllByOwner(owner);
        AppDeploymentOwner appDeploymentOwner = new AppDeploymentOwner();
        appDeploymentOwner.setUsername(owner.getUsername());
        appDeploymentOwner.setEmail(owner.getEmail());
        if (StringUtils.isNotEmpty(owner.getFirstname()) && StringUtils.isNotEmpty(owner.getLastname())) {
            appDeploymentOwner.setName(owner.getFirstname() + " " + owner.getLastname());
        } else {
            appDeploymentOwner.setName(appDeploymentOwner.getUsername());
        }
        appDeploymentOwner.setSshKeys(ownerSshKeys.stream().map(SSHKeyEntity::getKey).collect(Collectors.toList()));
        return appDeploymentOwner;
    }

    @Override
    public Optional<AppDeployment> load(String deploymentName, String domain) {
        return repository.findByDeploymentNameAndDomain(deploymentName, domain);
    }

    @Override
    public List<AppDeployment> loadAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AppDeploymentState loadState(Identifier deploymentId) {
        return repository.getStateByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException(deploymentNotFoundMessage(deploymentId)));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String loadErrorMessage(Identifier deploymentId) {
        return repository.getErrorMessageByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException(deploymentNotFoundMessage(deploymentId)));
    }

    @Override
    public List<AppDeployment> loadAllWaitingForDcn(String domain) {
        return repository.findByDomainAndState(domain, AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED);
    }

    @Override
    public String loadDomain(Identifier deploymentId) {
        return repository.getDomainByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException(deploymentNotFoundMessage(deploymentId)));
    }

    @Override
    public String loadDomainName(Identifier deploymentId) {
        return repository.getDomainNameByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException(deploymentNotFoundMessage(deploymentId)));
    }

    @Override
    public Identifier loadApplicationId(Identifier deploymentId) {
        return repository.getApplicationIdByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException(deploymentNotFoundMessage(deploymentId)));
    }

    @Override
    public List<AppDeploymentHistory> loadStateHistory(Identifier deploymentId){
        return load(deploymentId).getHistory();
    }

    @Override
    public void updateErrorMessage(Identifier deploymentId, String errorMessage) {
        AppDeployment appDeployment = load(deploymentId);
        appDeployment.setErrorMessage(errorMessage);
        repository.save(appDeployment);
    }

    @Override
    public Map<String, Long> getDeploymentStatistics() {
        return this.repository.countAllRunningByAppName().stream().collect(Collectors.toMap(AppDeploymentCount::getApplicationName, AppDeploymentCount::getCount));
    }

    private String deploymentNotFoundMessage(Identifier deploymentId) {
        return "Deployment with id " + deploymentId + " not found in the repository. ";
    }
}
