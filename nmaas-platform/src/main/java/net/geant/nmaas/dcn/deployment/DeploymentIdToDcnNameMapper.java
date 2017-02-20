package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.orchestration.Identifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DeploymentIdToDcnNameMapper {

    private Map<Identifier, String> mapping = new HashMap<>();

    public void storeMapping(Identifier deploymentId, String dcnName) {
        mapping.put(deploymentId, dcnName);
    }

    public String dcnName(Identifier deploymentId) throws EntryNotFoundException {
        if (!mapping.keySet().contains(deploymentId))
            throw new EntryNotFoundException("No mapping for deployment id " + deploymentId + " exists.");
        return mapping.get(deploymentId);
    }

    public Identifier deploymentId(String dcnName) throws EntryNotFoundException {
        return mapping.keySet().stream()
                .filter((id) -> mapping.get(id) == dcnName)
                .findFirst()
                .orElseThrow(() -> new EntryNotFoundException("No mapping for DCN name " + dcnName + " exists."));
    }

    public class EntryNotFoundException extends Exception {
        public EntryNotFoundException(String message) {
            super(message);
        }
    }
}
