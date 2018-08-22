package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;
import net.geant.nmaas.dcn.deployment.entities.DcnCloudEndpointDetails;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DcnRepositoryManager {

    private DcnInfoRepository dcnInfoRepository;

    @Autowired
    public DcnRepositoryManager(DcnInfoRepository dcnInfoRepository) {
        this.dcnInfoRepository = dcnInfoRepository;
    }

    @EventListener
    public void notifyStateChange(DcnDeploymentStateChangeEvent event) throws InvalidDomainException {
        updateDcnState(event.getDomain(), event.getState());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateDcnState(String domain, DcnDeploymentState state) throws InvalidDomainException {
        DcnInfo dcnInfo = loadDcnOrThrowException(domain);
        dcnInfo.setState(state);
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateAnsiblePlaybookForClientSideRouter(String domain, AnsiblePlaybookVpnConfig ansiblePlaybookVpnConfig)
            throws InvalidDomainException {
        DcnInfo dcnInfo = loadDcnOrThrowException(domain);
        dcnInfo.setPlaybookForClientSideRouter(ansiblePlaybookVpnConfig);
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateAnsiblePlaybookForCloudSideRouter(String domain, AnsiblePlaybookVpnConfig ansiblePlaybookVpnConfig)
            throws InvalidDomainException {
        DcnInfo dcnInfo = loadDcnOrThrowException(domain);
        dcnInfo.setPlaybookForCloudSideRouter(ansiblePlaybookVpnConfig);
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateDcnCloudEndpointDetails(String domain, DcnCloudEndpointDetails dcnCloudEndpointDetails)
            throws InvalidDomainException {
        DcnInfo dcnInfo = loadDcnOrThrowException(domain);
        dcnInfo.setCloudEndpointDetails(dcnCloudEndpointDetails);
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeDcnInfo(DcnInfo dcnInfo) throws InvalidDomainException {
        if (exists(dcnInfo.getDomain()))
            throw new InvalidDomainException("DCN information for domain " + dcnInfo.getDomain() + " already stored in database");
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeDcnInfo(String domain) throws InvalidDomainException {
        DcnInfo dcnInfo = loadDcnOrThrowException(domain);
        dcnInfoRepository.delete(dcnInfo);
    }

    DcnInfo loadNetwork(String domain) throws InvalidDomainException {
        return dcnInfoRepository.findByDomain(domain).orElseThrow(() -> new InvalidDomainException(domain));
    }

    public List<DcnInfo> loadAllNetworks() {
        return dcnInfoRepository.findAll();
    }

    public DcnDeploymentState loadCurrentState(String domain) throws InvalidDomainException {
        return dcnInfoRepository.getStateByDomain(domain).orElseThrow(() -> new InvalidDomainException(domain));
    }

    private DcnInfo loadDcnOrThrowException(String domain) throws InvalidDomainException {
        return dcnInfoRepository.findByDomain(domain).orElseThrow(() -> new InvalidDomainException(domain));
    }

    public boolean exists(String domain) {
        return dcnInfoRepository.findByDomain(domain).isPresent();
    }

}