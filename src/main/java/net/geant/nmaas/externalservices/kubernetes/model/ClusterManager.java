package net.geant.nmaas.externalservices.kubernetes.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.Domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ClusterManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String codename;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private OffsetDateTime creationDate;

    @Column(nullable = false)
    private OffsetDateTime modificationDate;

    @Column(nullable = false)
    private String clusterConfigFile;

    @Column(nullable = false)
    private String pathConfigFile;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ingress_id", referencedColumnName = "id")
    private KClusterIngress ingress;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "deployment_id", referencedColumnName = "id")
    private KClusterDeployment deployment;

    @ManyToMany()
    @JoinTable(
        name = "clusters_domains",
        joinColumns = @JoinColumn(name = "cluster_id"),
        inverseJoinColumns = @JoinColumn(name = "domain_id")
    )
    private List<Domain> domains = new ArrayList<>();

    public List<Domain> getDomains() {
        return domains != null ? domains : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "ClusterManager{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", codename='" + codename + '\'' +
                ", description='" + description + '\'' +
                ", creationDate=" + creationDate +
                ", modificationDate=" + modificationDate +
                ", domain= " + domains +
                ", clusterConfigFile='" + clusterConfigFile + '\'' +
                ", pathConfigFile='" + pathConfigFile + '\'' +
                ", ingress=" + (ingress != null ? ingress.getId() : "null") +
                ", deployment=" + (deployment != null ? deployment.getId() : "null") +
                '}';
    }

}
