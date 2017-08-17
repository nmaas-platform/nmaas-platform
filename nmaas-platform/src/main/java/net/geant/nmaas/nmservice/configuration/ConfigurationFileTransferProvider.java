package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.ConfigFileNotFoundException;
import net.geant.nmaas.nmservice.configuration.exceptions.FileTransferException;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;

import java.util.List;

/**
 * Defines a method to be used to transfer a list of configuration files for a given deployment to destination storage.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public interface ConfigurationFileTransferProvider {

    /**
     * Method to transfer a list of configuration files to destination from which they can be loaded by deployed service at startup.
     *
     * @param deploymentId unique identifier of service deployment
     * @param configIds list of identifiers of configuration files that should be loaded from database and transferred to destination
     * @throws InvalidDeploymentIdException if provided deployment identifier doesn't match any current deployments
     * @throws ConfigFileNotFoundException if any of the provided configuration file identifiers doesn't match an existing file
     * @throws FileTransferException if the actual delivery of the file failed for any reason
     */
    void transferConfigFiles(Identifier deploymentId, List<String> configIds) throws InvalidDeploymentIdException, ConfigFileNotFoundException, FileTransferException;

}
