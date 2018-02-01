package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.entities.DcnState;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Executor used when DCN is being configured by network operator.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("dcn_manual")
public class ManualDcnDeploymentExecutor implements DcnDeploymentProvider {

    private DcnRepositoryManager dcnRepositoryManager;
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public ManualDcnDeploymentExecutor(DcnRepositoryManager dcnRepositoryManager, ApplicationEventPublisher applicationEventPublisher) {
        this.dcnRepositoryManager = dcnRepositoryManager;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public DcnState checkState(Identifier clientId) {
        try {
            return DcnState.fromDcnDeploymentState(dcnRepositoryManager.loadCurrentState(clientId));
        } catch (InvalidClientIdException e) {
            return DcnState.NONE;
        }
    }

    @Override
    public void verifyRequest(Identifier clientId, DcnSpec dcnSpec) throws DcnRequestVerificationException {
        notifyStateChangeListeners(clientId, DcnDeploymentState.REQUEST_VERIFIED);
    }

    @Override
    public void deployDcn(Identifier clientId) throws CouldNotDeployDcnException {
        notifyStateChangeListeners(clientId, DcnDeploymentState.DEPLOYED);
    }

    @Override
    public void verifyDcn(Identifier clientId) throws CouldNotVerifyDcnException {
        notifyStateChangeListeners(clientId, DcnDeploymentState.VERIFIED);
    }

    @Override
    public void removeDcn(Identifier clientId) throws CouldNotRemoveDcnException {
        notifyStateChangeListeners(clientId, DcnDeploymentState.REMOVED);
    }

    private void notifyStateChangeListeners(Identifier clientId, DcnDeploymentState state) {
        applicationEventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, clientId, state));
    }

}
