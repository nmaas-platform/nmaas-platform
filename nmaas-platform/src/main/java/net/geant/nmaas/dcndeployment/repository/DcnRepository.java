package net.geant.nmaas.dcndeployment.repository;

import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
@Singleton
public class DcnRepository {

    private Map<String, DcnInfo> networks = new HashMap<>();

    public void updateDcnState(String dcnName, DcnInfo.DcnState state) throws DcnNotFoundException {
        loadNetwork(dcnName).updateState(state);
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
