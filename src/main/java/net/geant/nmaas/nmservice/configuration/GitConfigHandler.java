package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.ConfigRepositoryAccessDetailsNotFoundException;
import net.geant.nmaas.orchestration.AppConfigRepositoryAccessDetails;
import net.geant.nmaas.orchestration.Identifier;

import java.util.List;

public interface GitConfigHandler {

    void createUser(String userUsername, String userEmail, String userName, List<String> userSshKeys);

    void createRepository(Identifier deploymentId, String member);

    void commitConfigFiles(Identifier deploymentId, List<String> configIds);

    void removeConfigFiles(Identifier deploymentId);

    /**
     *
     * @param deploymentId
     * @return
     * @throws ConfigRepositoryAccessDetailsNotFoundException
     */
    AppConfigRepositoryAccessDetails configRepositoryAccessDetails(Identifier deploymentId);

    void addMemberToProject(Integer gitLabProjectId, Integer gitLabUserId);
    void addMemberToProject(Integer gitLabProjectId, String username);

    void removeMemberFromProject(Integer gitLabProjectId, Integer gitLabUserId);
    void removeMemberFromProject(Integer gitLabProjectId, String username);

}
