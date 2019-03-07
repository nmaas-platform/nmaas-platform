package net.geant.nmaas.orchestration;

import lombok.AllArgsConstructor;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentHistory;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DefaultAppDeploymentRepositoryManager implements AppDeploymentRepositoryManager {

    private AppDeploymentRepository repository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void store(AppDeployment appDeployment) {
        if(repository.findByDeploymentId(appDeployment.getDeploymentId()).isPresent()) {
            throw new InvalidDeploymentIdException("Deployment with id " + appDeployment.getDeploymentId() + " already exists in the repository. ");
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
    public AppDeployment load(Identifier deploymentId) {
        return repository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException(deploymentNotFoundMessage(deploymentId)));
    }

    @Override
    public Optional<AppDeployment> load(String deploymentName, String domain){
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateConfiguration(Identifier deploymentId, AppConfiguration configuration) {
        AppDeployment appDeployment = load(deploymentId);
        appDeployment.setConfiguration(configuration);
        repository.save(appDeployment);
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
    public List<AppDeploymentHistory> loadStateHistory(Identifier deploymentId){
        return load(deploymentId).getHistory();
    }

    @Override
    public void updateErrorMessage(Identifier deploymentId, String errorMessage) {
        AppDeployment appDeployment = load(deploymentId);
        appDeployment.setErrorMessage(errorMessage);
        repository.save(appDeployment);
    }

    private String deploymentNotFoundMessage(Identifier deploymentId) {
        return "Deployment with id " + deploymentId + " not found in the repository. ";
    }
}
