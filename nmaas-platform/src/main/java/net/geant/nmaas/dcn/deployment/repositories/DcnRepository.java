package net.geant.nmaas.dcn.deployment.repositories;

import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.dcn.deployment.DeploymentIdToDcnNameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DcnRepository {

    @Autowired
    private DeploymentIdToDcnNameMapper deploymentIdMapper;

    private Map<String, DcnInfo> networks = new HashMap<>();

    @EventListener
    public void notifyStateChange(DcnDeploymentStateChangeEvent event) throws DeploymentIdToDcnNameMapper.EntryNotFoundException, DcnNotFoundException {
        updateDcnState(deploymentIdMapper.dcnName(event.getDeploymentId()), event.getState());
    }

    private void updateDcnState(String dcnName, DcnDeploymentState state) throws DcnNotFoundException {
        loadNetwork(dcnName).updateState(state);
    }

    public void updateAnsiblePlaybookForClientSideRouter(String dcnName, AnsiblePlaybookVpnConfig ansiblePlaybookVpnConfig) throws DcnNotFoundException {
        loadNetwork(dcnName).setAnsiblePlaybookForClientSideRouter(ansiblePlaybookVpnConfig);
    }

    public void updateAnsiblePlaybookForCloudSideRouter(String dcnName, AnsiblePlaybookVpnConfig ansiblePlaybookVpnConfig) throws DcnNotFoundException {
        loadNetwork(dcnName).setAnsiblePlaybookForCloudSideRouter(ansiblePlaybookVpnConfig);
    }

    public void storeNetwork(DcnInfo dcnInfo) {
        if(dcnInfo != null && dcnInfo.getName() != null)
            networks.put(dcnInfo.getName(), dcnInfo);
    }

    public DcnInfo loadNetwork(String name) throws DcnNotFoundException {
        DcnInfo dcnInfo = networks.get(name);
        if (dcnInfo != null)
            return dcnInfo;
        else
            throw new DcnNotFoundException(
                    "DCN " + name + " not found in the repository. " +
                    "Existing networks are: " + networks.keySet().stream().collect(Collectors.joining(",")));
    }

    public DcnDeploymentState loadCurrentState(String dcnName) throws DcnNotFoundException {
        return loadNetwork(dcnName).getState();
    }

    public class DcnNotFoundException extends Exception {
        public DcnNotFoundException(String message) {
            super(message);
        }
    }
}
