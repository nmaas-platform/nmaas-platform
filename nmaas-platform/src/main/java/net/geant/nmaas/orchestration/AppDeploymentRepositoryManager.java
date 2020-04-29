package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentHistory;
import net.geant.nmaas.orchestration.entities.AppDeploymentOwner;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;

import java.util.List;
import java.util.Optional;

public interface AppDeploymentRepositoryManager {

    void store(AppDeployment appDeployment);

    void update(AppDeployment appDeployment);

    void updateState(Identifier deploymentId, AppDeploymentState currentState);

    AppDeployment load(Identifier deploymentId);

    AppDeploymentOwner loadOwner(Identifier deploymentId);

    Optional<AppDeployment> load(String deploymentName, String domain);

    List<AppDeployment> loadAll();

    AppDeploymentState loadState(Identifier deploymentId);

    List<AppDeploymentHistory> loadStateHistory(Identifier deploymentId);

    List<AppDeployment> loadAllWaitingForDcn(String domain);

    String loadDomain(Identifier deploymentId);

    String loadDomainName(Identifier deploymentId);

    void updateErrorMessage(Identifier deploymentId, String errorMessage);

    String loadErrorMessage(Identifier deploymentId);

}
