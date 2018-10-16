package net.geant.nmaas.externalservices.inventory.kubernetes.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.net.InetAddress;
import lombok.Getter;
import lombok.Setter;

/**
 * Set of properties describing a Kubernetes cluster Helm client details
 */
@Getter
@Setter
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

    /** Flag indicating if tls is enabled */
    private Boolean enableTls;
}
