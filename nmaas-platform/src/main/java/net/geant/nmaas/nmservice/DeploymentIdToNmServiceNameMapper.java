package net.geant.nmaas.nmservice;

import net.geant.nmaas.deploymentorchestration.Identifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DeploymentIdToNmServiceNameMapper {

    private Map<Identifier, String> mapping = new HashMap<>();

    public void storeMapping(Identifier deploymentId, String nmServiceName) {
        mapping.put(deploymentId, nmServiceName);
    }

    public String nmServiceName(Identifier deploymentId) throws EntryNotFoundException {
        if (!mapping.keySet().contains(deploymentId))
            throw new EntryNotFoundException("No mapping for deployment id " + deploymentId + " exists.");
        return mapping.get(deploymentId);
    }

    public class EntryNotFoundException extends Exception {
        public EntryNotFoundException(String message) {
            super(message);
        }
    }
}
