package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.orchestration.Identifier;

import java.util.List;

public interface GitConfigHandler {

    void createRepository(Identifier deploymentId, Identifier descriptiveDeploymentId, List<String> sshKeys);

    void commitConfigFiles(Identifier deploymentId, List<String> configIds);

    void removeConfigFiles(Identifier deploymentId);

}
