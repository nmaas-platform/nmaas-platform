package net.geant.nmaas.nmservice.configuration;

import lombok.Getter;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentOwner;

@Getter
public class NmServiceDeployment {

    private Identifier deploymentId;
    private Identifier descriptiveDeploymentId;
    private Identifier applicationId;
    private String domainName;
    private String ownerUsername;
    private String ownerSshKey;
    private boolean configFileRepositoryRequired;
    private AppConfiguration appConfiguration;

    public static NmServiceDeployment fromAppDeployment(AppDeployment appDeployment, AppDeploymentOwner appDeploymentOwner) {
        NmServiceDeployment nmServiceDeployment = new NmServiceDeployment();
        nmServiceDeployment.deploymentId = appDeployment.getDeploymentId();
        nmServiceDeployment.descriptiveDeploymentId = appDeployment.getDescriptiveDeploymentId();
        nmServiceDeployment.applicationId = appDeployment.getApplicationId();
        nmServiceDeployment.domainName = appDeployment.getDomain();
        nmServiceDeployment.ownerUsername = appDeploymentOwner.getUsername();
        nmServiceDeployment.ownerSshKey = appDeploymentOwner.getSshKey();
        nmServiceDeployment.configFileRepositoryRequired = appDeployment.isConfigFileRepositoryRequired();
        nmServiceDeployment.appConfiguration = appDeployment.getConfiguration();
        return nmServiceDeployment;
    }

}
