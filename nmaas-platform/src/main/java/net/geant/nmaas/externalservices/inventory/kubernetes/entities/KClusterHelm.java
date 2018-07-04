package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.*;
import java.net.InetAddress;

/**
 * Set of properties describing a Kubernetes cluster Helm client details
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="k_cluster_helm")
public class KClusterHelm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Address of the host of which helm commands should be executed */
    @Column(nullable = false)
    private InetAddress helmHostAddress;

    /** Username to be used to connect to host with SSH and execute helm commands */
    @Column(nullable = false)
    private String helmHostSshUsername;

    /** Flag indicating whether to use local chart tar.gz files (when set to true) rather than chart repositories */
    @Column(nullable = false)
    private Boolean useLocalChartArchives;

    /** Name of the remote repository from which charts should be downloaded */
    private String helmChartRepositoryName;

    /** Directory on the helm host in which all charts are stored */
    private String helmHostChartsDirectory;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InetAddress getHelmHostAddress() {
        return helmHostAddress;
    }

    public void setHelmHostAddress(InetAddress helmHostAddress) {
        this.helmHostAddress = helmHostAddress;
    }

    public String getHelmHostSshUsername() {
        return helmHostSshUsername;
    }

    public void setHelmHostSshUsername(String helmHostSshUsername) {
        this.helmHostSshUsername = helmHostSshUsername;
    }

    public Boolean getUseLocalChartArchives() {
        return useLocalChartArchives;
    }

    public void setUseLocalChartArchives(Boolean useLocalChartArchives) {
        this.useLocalChartArchives = useLocalChartArchives;
    }

    public String getHelmChartRepositoryName() {
        return helmChartRepositoryName;
    }

    public void setHelmChartRepositoryName(String helmChartRepositoryName) {
        this.helmChartRepositoryName = helmChartRepositoryName;
    }

    public String getHelmHostChartsDirectory() {
        return helmHostChartsDirectory;
    }

    public void setHelmHostChartsDirectory(String helmHostChartsDirectory) {
        this.helmHostChartsDirectory = helmHostChartsDirectory;
    }
}
