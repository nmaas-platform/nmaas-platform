package net.geant.nmaas.nmservice.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentOwner;

import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class NmServiceDeployment {

    private Identifier deploymentId;
    private Identifier descriptiveDeploymentId;
    private Identifier applicationId;
    private String domainName;
    private String ownerUsername;
    private String ownerEmail;
    private String ownerName;
    private List<String> ownerSshKeys;
    private boolean configFileRepositoryRequired;
    private boolean configUpdateEnabled;
    private AppConfiguration appConfiguration;

    public static NmServiceDeployment fromAppDeployment(AppDeployment appDeployment, AppDeploymentOwner appDeploymentOwner) {
        NmServiceDeployment nmServiceDeployment = new NmServiceDeployment();
        nmServiceDeployment.deploymentId = appDeployment.getDeploymentId();
        nmServiceDeployment.descriptiveDeploymentId = appDeployment.getDescriptiveDeploymentId();
        nmServiceDeployment.applicationId = appDeployment.getApplicationId();
        nmServiceDeployment.domainName = appDeployment.getDomain();
        nmServiceDeployment.ownerUsername = appDeploymentOwner.getUsername();
        nmServiceDeployment.ownerEmail = appDeploymentOwner.getEmail();
        nmServiceDeployment.ownerName = appDeploymentOwner.getName();
        nmServiceDeployment.ownerSshKeys = appDeploymentOwner.getSshKeys();
        nmServiceDeployment.configFileRepositoryRequired = appDeployment.isConfigFileRepositoryRequired();
        nmServiceDeployment.configUpdateEnabled = appDeployment.isConfigUpdateEnabled();
        if (Objects.isNull(appDeployment.getConfiguration())) {
            log.warn("Application configuration of deployment {} is null", appDeployment.getDescriptiveDeploymentId());
        }
        nmServiceDeployment.appConfiguration = appDeployment.getConfiguration();
        return nmServiceDeployment;
    }

}
