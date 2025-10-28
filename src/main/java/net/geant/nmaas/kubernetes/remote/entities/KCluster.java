package net.geant.nmaas.kubernetes.remote.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistence.entity.Domain;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "k_cluster")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class KCluster {

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

    @Column(nullable = false, length = 8000)
    private String clusterConfigFile;

    @Column(nullable = false)
    private String pathConfigFile;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ingress_id", referencedColumnName = "id")
    private KClusterIngress ingress;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "deployment_id", referencedColumnName = "id")
    private KClusterDeployment deployment;

    @Builder.Default
    @ManyToMany()
    @JoinTable(
            name = "k_clusters_domains",
            joinColumns = @JoinColumn(name = "k_cluster_id"),
            inverseJoinColumns = @JoinColumn(name = "domain_id")
    )
    private List<Domain> domains = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private KClusterState state;

    @Column(nullable = false)
    private OffsetDateTime currentStateSince;

    @Column(nullable = false)
    private String contactEmail;

    public List<Domain> getDomains() {
        return domains != null ? domains : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "KCluster{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", codename='" + codename + '\'' +
                ", description='" + description + '\'' +
                ", creationDate=" + creationDate +
                ", modificationDate=" + modificationDate +
                ", domain= " + domains +
                ", clusterConfigFile='" + clusterConfigFile + '\'' +
                ", pathConfigFile='" + pathConfigFile + '\'' +
                ", state='" + state + '\'' +
                ", email='" + contactEmail + '\'' +
                ", ingress=" + (ingress != null ? ingress.getId() : "null") +
                ", deployment=" + (deployment != null ? deployment.getId() : "null") +
                '}';
    }

}
