package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.Identifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DeploymentIdToApplicationIdMapper {

    private Map<Identifier, Identifier> mapping = new HashMap<>();

    public void storeMapping(Identifier deploymentId, Identifier applicationId) {
        mapping.put(deploymentId, applicationId);
    }

    public Identifier applicationId(Identifier deploymentId) throws DeploymentIdToApplicationIdMapper.EntryNotFoundException {
        if (!mapping.keySet().contains(deploymentId))
            throw new DeploymentIdToApplicationIdMapper.EntryNotFoundException("No application identifier mapped for deployment identifier " + deploymentId + ".");
        return mapping.get(deploymentId);
    }

    public class EntryNotFoundException extends Exception {
        public EntryNotFoundException(String message) {
            super(message);
        }
    }

}
