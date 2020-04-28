package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.orchestration.Identifier;
import org.gitlab4j.api.models.User;

import java.util.List;

public interface GitConfigHandler {

    void createRepository(Identifier deploymentId, Identifier descriptiveDeploymentId, User owner);

    void commitConfigFiles(Identifier deploymentId, List<String> configIds);

    void removeConfigFiles(Identifier deploymentId);

}
