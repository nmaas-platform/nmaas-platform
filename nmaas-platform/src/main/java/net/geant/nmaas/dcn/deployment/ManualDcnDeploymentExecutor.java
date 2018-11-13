package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
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
    public void verifyRequest(String domain, DcnSpec dcnSpec) {
        try {
            storeDcnInfoIfNotExists(domain, dcnSpec);
            notifyStateChangeListeners(domain, DcnDeploymentState.REQUEST_VERIFIED);
        } catch(InvalidDomainException e){
            notifyStateChangeListeners(domain, DcnDeploymentState.REQUEST_VERIFICATION_FAILED);
            throw new DcnRequestVerificationException("Exception during DCN request verification -> " + e.getMessage());
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void deployDcn(String domain) {
        try {
            // needs to wait for DCN state change in database
            Thread.sleep(200);
            if(dcnRepositoryManager.loadCurrentState(domain) == DcnDeploymentState.REQUEST_VERIFIED){
                notifyStateChangeListeners(domain, DcnDeploymentState.WAITING_FOR_OPERATOR_CONFIRMATION);
            }else{
                throw new CouldNotDeployDcnException("Exception during DCN deploy. Trying to deploy DCN with state: " + dcnRepositoryManager.loadCurrentState(domain).toString());
            }
        } catch (InvalidDomainException e){
            throw new CouldNotDeployDcnException("Exception during DCN deploy " + e.getMessage());
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
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

    private void notifyStateChangeListeners(String domain, DcnDeploymentState state) {
        applicationEventPublisher.publishEvent(new DcnDeploymentStateChangeEvent(this, domain, state));
    }

    private void storeDcnInfoIfNotExists(String domain, DcnSpec dcnSpec) {
        if (!dcnRepositoryManager.exists(domain)) {
            dcnRepositoryManager.storeDcnInfo(new DcnInfo(dcnSpec));
        }
    }

}
