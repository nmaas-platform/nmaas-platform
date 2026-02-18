package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.api.dto.applications.AppConfigRepositoryAccessDetails;
import net.geant.nmaas.orchestration.Identifier;

import java.util.List;

public interface GitConfigHandler {

    void createUser(String userUsername, String userEmail, String userName, List<String> userSshKeys);

    void createRepository(Identifier deploymentId, String member);

    void commitConfigFiles(Identifier deploymentId, List<String> configIds);

    void removeConfigFiles(Identifier deploymentId);

    List<ConfigFile> getConfigFiles(Identifier deploymentId);

    AppConfigRepositoryAccessDetails configRepositoryAccessDetails(Identifier deploymentId);

    void addMemberToProject(Long gitLabProjectId, Long gitLabUserId);

    void addMemberToProject(Long gitLabProjectId, String username);

    void removeMemberFromProject(Long gitLabProjectId, Long gitLabUserId);

    void removeMemberFromProject(Long gitLabProjectId, String username);

}
