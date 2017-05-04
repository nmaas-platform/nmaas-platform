package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class AppDeploymentRepositoryManager {

    @Autowired
    private AppDeploymentRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateState(Identifier deploymentId, AppDeploymentState currentState) throws InvalidDeploymentIdException {
        AppDeployment appDeployment = repository.findByDeploymentId(deploymentId).orElseThrow(() -> new InvalidDeploymentIdException(deploymentId));
        appDeployment.setState(currentState);
        repository.save(appDeployment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AppDeploymentState loadState(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.getStateByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException("Deployment with id " + deploymentId + " not found in the repository. "));
    }

    public List<AppDeployment> loadAll() {
        return repository.findAll();
    }

    public List<AppDeployment> getAllWaitingForDcn(Identifier clientId) {
        return repository.findByClientIdAndState(clientId, AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED);
    }

    public Identifier getClientIdByDeploymentId(Identifier deploymentId) throws InvalidDeploymentIdException {
        return repository.getClientIdByDeploymentId(deploymentId)
                .orElseThrow(() -> new InvalidDeploymentIdException("Deployment with id " + deploymentId + " not found in the repository. "));
    }
}
