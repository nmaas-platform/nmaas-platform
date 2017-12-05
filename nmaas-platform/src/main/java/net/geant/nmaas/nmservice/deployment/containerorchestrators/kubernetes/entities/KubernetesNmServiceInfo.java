package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import net.geant.nmaas.nmservice.configuration.entities.GitLabProject;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.orchestration.entities.Identifier;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Network Management Service deployment information for application deployed on Kubernetes cluster.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
public class KubernetesNmServiceInfo extends NmServiceInfo {

    /**
     * Kubernetes template for this service
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private KubernetesTemplate kubernetesTemplate;

    /**
     * GitLab project information created to store configuration files for this service (deployment)
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private GitLabProject gitLabProject;

    public KubernetesNmServiceInfo () {
        super();
    }

    public KubernetesNmServiceInfo(Identifier deploymentId, Identifier applicationId, Identifier clientId, KubernetesTemplate kubernetesTemplate) {
        super(deploymentId, applicationId, clientId);
        this.kubernetesTemplate = kubernetesTemplate;
    }

    public KubernetesTemplate getKubernetesTemplate() {
        return kubernetesTemplate;
    }

    public void setKubernetesTemplate(KubernetesTemplate kubernetesTemplate) {
        this.kubernetesTemplate = kubernetesTemplate;
    }

    public GitLabProject getGitLabProject() {
        return gitLabProject;
    }

    public void setGitLabProject(GitLabProject gitLabProject) {
        this.gitLabProject = gitLabProject;
    }

}
