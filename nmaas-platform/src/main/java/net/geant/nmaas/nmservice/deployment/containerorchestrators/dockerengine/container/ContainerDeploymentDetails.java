package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.container;

import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceDeploymentDetails;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class ContainerDeploymentDetails implements NmServiceDeploymentDetails {

    private String attachedVolumeName;

    public ContainerDeploymentDetails(String attachedVolumeName) {
        this.attachedVolumeName = attachedVolumeName;
    }

    public String getAttachedVolumeName() {
        return attachedVolumeName;
    }
}
