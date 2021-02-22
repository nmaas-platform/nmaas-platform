package net.geant.nmaas.dcn.deployment;

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

@Component
public class DcnRepositoryManager {

    private DcnInfoRepository dcnInfoRepository;

    @Autowired
    public DcnRepositoryManager(DcnInfoRepository dcnInfoRepository) {
        this.dcnInfoRepository = dcnInfoRepository;
    }

    @EventListener
    public void notifyStateChange(DcnDeploymentStateChangeEvent event) {
        updateDcnState(event.getDomain(), event.getState());
    }

    private void updateDcnState(String domain, DcnDeploymentState state) {
        DcnInfo dcnInfo = loadDcnOrThrowException(domain);
        dcnInfo.setState(state);
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeDcnInfo(DcnInfo dcnInfo) {
        if (exists(dcnInfo.getDomain()))
            throw new InvalidDomainException("DCN information for domain " + dcnInfo.getDomain() + " already stored in database");
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDcnDeploymentType(String domain, DcnDeploymentType dcnDeploymentType){
        DcnInfo dcnInfo = loadDcnOrThrowException(domain);
        dcnInfo.setDcnDeploymentType(dcnDeploymentType);
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeDcnInfo(String domain) {
        DcnInfo dcnInfo = loadDcnOrThrowException(domain);
        dcnInfoRepository.delete(dcnInfo);
    }

    protected DcnInfo loadNetwork(String domain) {
        return dcnInfoRepository.findByDomain(domain).orElseThrow(() -> new InvalidDomainException(domain));
    }

    public List<DcnInfo> loadAllNetworks() {
        return dcnInfoRepository.findAll();
    }

    public DcnDeploymentState loadCurrentState(String domain) {
        return dcnInfoRepository.getStateByDomain(domain).orElseThrow(() -> new InvalidDomainException(domain));
    }

    public DcnDeploymentType loadType(String domain){
        return this.loadDcnOrThrowException(domain).getDcnDeploymentType();
    }

    private DcnInfo loadDcnOrThrowException(String domain) {
        return dcnInfoRepository.findByDomain(domain).orElseThrow(() -> new InvalidDomainException(domain));
    }

    public boolean exists(String domain) {
        return dcnInfoRepository.findByDomain(domain).isPresent();
    }

}