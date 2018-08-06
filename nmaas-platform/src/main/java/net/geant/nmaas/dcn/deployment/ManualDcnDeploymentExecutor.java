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
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.utils.logging.LogLevel;
import net.geant.nmaas.utils.logging.Loggable;
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
    private DomainRepository domainRepository;

    @Autowired
    public ManualDcnDeploymentExecutor(DcnRepositoryManager dcnRepositoryManager, ApplicationEventPublisher applicationEventPublisher, DomainRepository domainRepository) {
        this.dcnRepositoryManager = dcnRepositoryManager;
        this.applicationEventPublisher = applicationEventPublisher;
        this.domainRepository = domainRepository;
    }

    @Override
    @Loggable(LogLevel.INFO)
    public DcnState checkState(String domain) {
        try {
            return checkDcnConfiguredFlag(domain)? DcnState.fromDcnDeploymentState(dcnRepositoryManager.loadCurrentState(domain)): DcnState.PROCESSED;
        } catch (InvalidDomainException e) {
            return DcnState.NONE;
        }
    }

    @Override
    @Loggable(LogLevel.INFO)
    public void verifyRequest(String domain, DcnSpec dcnSpec) throws DcnRequestVerificationException {
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
    public void deployDcn(String domain) throws CouldNotDeployDcnException {
        try {
            if (!checkDcnConfiguredFlag(domain)) {
                notifyStateChangeListeners(domain, DcnDeploymentState.WAITING_FOR_OPERATOR_CONFIRMATION);
            } else {
                notifyStateChangeListeners(domain, DcnDeploymentState.DEPLOYED);
            }
        } catch (InvalidDomainException e){
            throw new CouldNotDeployDcnException("Exception during DCN deploy " + e.getMessage());
        }
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

    private Domain getDomain(String domain) throws InvalidDomainException {
        return this.domainRepository.findByCodename(domain).orElseThrow(() -> new InvalidDomainException("Domain not found"));
    }

    private boolean checkDcnConfiguredFlag(String domain) throws InvalidDomainException{
        return getDomain(domain).isDcnConfigured();
    }

    private void storeDcnInfoIfNotExists(String domain, DcnSpec dcnSpec) throws InvalidDomainException {
        if (!dcnRepositoryManager.exists(domain)) {
            dcnRepositoryManager.storeDcnInfo(new DcnInfo(dcnSpec));
        }
    }

}
