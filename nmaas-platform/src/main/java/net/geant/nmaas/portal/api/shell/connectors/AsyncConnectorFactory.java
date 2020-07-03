package net.geant.nmaas.portal.api.shell.connectors;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * This object is responsible for creating connectors to instances
 * currently utilizes SSH connections (no provider for default kubernetes connection currently)
 */
@Component
@AllArgsConstructor
public class AsyncConnectorFactory {

    public AsyncConnector prepareConnection(AppInstance appInstance) {
        // TODO implement
        // return new KubernetesConnector();
        try {
            return SshSessionConnectorDefaultData.getDefaultConnector();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot create connector");
        }
    }
}
