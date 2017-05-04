package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;
import net.geant.nmaas.dcn.deployment.entities.DcnCloudEndpointDetails;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DcnRepositoryManager {

    @Autowired
    private DcnInfoRepository dcnInfoRepository;

    @EventListener
    public void notifyStateChange(DcnDeploymentStateChangeEvent event) throws InvalidClientIdException {
        updateDcnState(event.getClientId(), event.getState());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateDcnState(Identifier clientId, DcnDeploymentState state) throws InvalidClientIdException {
        DcnInfo dcnInfo = dcnInfoRepository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        dcnInfo.setState(state);
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAnsiblePlaybookForClientSideRouter(Identifier clientId, AnsiblePlaybookVpnConfig ansiblePlaybookVpnConfig)
            throws InvalidClientIdException {
        DcnInfo dcnInfo = dcnInfoRepository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        dcnInfo.setAnsiblePlaybookForClientSideRouter(ansiblePlaybookVpnConfig);
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAnsiblePlaybookForCloudSideRouter(Identifier clientId, AnsiblePlaybookVpnConfig ansiblePlaybookVpnConfig)
            throws InvalidClientIdException {
        DcnInfo dcnInfo = dcnInfoRepository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        dcnInfo.setAnsiblePlaybookForCloudSideRouter(ansiblePlaybookVpnConfig);
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDcnCloudEndpointDetails(Identifier clientId, DcnCloudEndpointDetails dcnCloudEndpointDetails)
            throws InvalidClientIdException {
        DcnInfo dcnInfo = dcnInfoRepository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        dcnInfo.setCloudEndpointDetails(dcnCloudEndpointDetails);
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeDcnInfo(DcnInfo dcnInfo) {
        dcnInfoRepository.save(dcnInfo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeDcnInfo(Identifier clientId) throws InvalidClientIdException {
        DcnInfo dcnInfo = dcnInfoRepository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
        dcnInfoRepository.delete(dcnInfo.getId());
    }

    public DcnInfo loadNetwork(Identifier clientId) throws InvalidClientIdException {
        return dcnInfoRepository.findByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
    }

    public DcnDeploymentState loadCurrentState(Identifier clientId) throws InvalidClientIdException {
        return dcnInfoRepository.getStateByClientId(clientId).orElseThrow(() -> new InvalidClientIdException(clientId));
    }

}