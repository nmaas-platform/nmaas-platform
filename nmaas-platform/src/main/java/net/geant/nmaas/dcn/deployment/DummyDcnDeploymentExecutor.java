package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.entities.DcnState;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotDeployDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotRemoveDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.CouldNotVerifyDcnException;
import net.geant.nmaas.dcn.deployment.exceptions.DcnRequestVerificationException;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Executor used when DCN is being configured by network operator.
 */
@Component
@Profile("dcn_none")
public class DummyDcnDeploymentExecutor implements DcnDeploymentProvider {

    private DcnRepositoryManager dcnRepositoryManager;
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public DummyDcnDeploymentExecutor(DcnRepositoryManager dcnRepositoryManager, ApplicationEventPublisher applicationEventPublisher) {
        this.dcnRepositoryManager = dcnRepositoryManager;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public DcnState checkState(String domain) {
        try {
            return DcnState.fromDcnDeploymentState(dcnRepositoryManager.loadCurrentState(domain));
        } catch (InvalidDomainException e) {
            return DcnState.NONE;
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequest(String domain, DcnSpec dcnSpec) throws DcnRequestVerificationException {
        notifyStateChangeListeners(domain, DcnDeploymentState.REQUEST_VERIFIED);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployDcn(String domain) throws CouldNotDeployDcnException {
        notifyStateChangeListeners(domain, DcnDeploymentState.DEPLOYED);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDcn(String domain) throws CouldNotVerifyDcnException {
        notifyStateChangeListeners(domain, DcnDeploymentState.VERIFIED);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeDcn(String domain) throws CouldNotRemoveDcnException {
        notifyStateChangeListeners(domain, DcnDeploymentState.REMOVED);
    }

    private void notifyStateChangeListeners(String domain, DcnDeploymentState state) {
        applicationEventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, domain, state));
    }

}
