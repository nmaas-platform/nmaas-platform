package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.orchestration.Identifier;

import java.util.List;

public interface GitConfigHandler {

    void createUser(String userUsername, String userEmail, String userName, List<String> userSshKeys);

    void createRepository(Identifier deploymentId, String member);

    void commitConfigFiles(Identifier deploymentId, List<String> configIds);

    void removeConfigFiles(Identifier deploymentId);

}
