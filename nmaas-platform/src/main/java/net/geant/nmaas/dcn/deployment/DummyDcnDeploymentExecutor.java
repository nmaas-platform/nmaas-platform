package net.geant.nmaas.dcn.deployment;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.entities.DcnState;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Executor used when DCN is being configured by network operator.
 */
@Component
@Log4j2
public class DummyDcnDeploymentExecutor implements DcnDeploymentProvider {

    private DcnRepositoryManager dcnRepositoryManager;
    private ApplicationEventPublisher applicationEventPublisher;
    private DcnDeploymentType dcnDeploymentType;

    @Autowired
    public DummyDcnDeploymentExecutor(DcnRepositoryManager dcnRepositoryManager, ApplicationEventPublisher applicationEventPublisher) {
        this.dcnRepositoryManager = dcnRepositoryManager;
        this.applicationEventPublisher = applicationEventPublisher;
        this.dcnDeploymentType = DcnDeploymentType.NONE;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public DcnState checkState(String domain) {
        try {
            log.error("NONE");
            return DcnState.fromDcnDeploymentState(dcnRepositoryManager.loadCurrentState(domain));
        } catch (InvalidDomainException e) {
            return DcnState.NONE;
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequest(String domain, DcnSpec dcnSpec) {
        notifyStateChangeListeners(domain, DcnDeploymentState.REQUEST_VERIFIED);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployDcn(String domain) {
        notifyStateChangeListeners(domain, DcnDeploymentState.DEPLOYED);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyDcn(String domain) {
        notifyStateChangeListeners(domain, DcnDeploymentState.VERIFIED);
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void removeDcn(String domain) {
        notifyStateChangeListeners(domain, DcnDeploymentState.REMOVED);
    }

    @Override
    public DcnDeploymentType getDcnDeploymentType(){
        return this.dcnDeploymentType;
    }

    private void notifyStateChangeListeners(String domain, DcnDeploymentState state) {
        applicationEventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, domain, state));
    }

}
