package net.geant.nmaas.dcn.deployment.repository;

import net.geant.nmaas.dcn.deployment.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.VpnConfig;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DcnRepository {

    private Map<String, DcnInfo> networks = new HashMap<>();

    public void updateDcnState(String dcnName, DcnDeploymentState state) throws DcnNotFoundException {
        loadNetwork(dcnName).updateState(state);
    }

    public void updateVpnConfig(String dcnName, VpnConfig vpnConfig) throws DcnNotFoundException {
        loadNetwork(dcnName).setVpnConfig(vpnConfig);
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

    public class DcnNotFoundException extends Exception {
        public DcnNotFoundException(String message) {
            super(message);
        }
    }
}
