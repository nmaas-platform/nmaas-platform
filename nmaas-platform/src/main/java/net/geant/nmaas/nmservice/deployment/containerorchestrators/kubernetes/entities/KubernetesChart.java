package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
public class KubernetesChart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String version;

    public KubernetesChart() {
    }

    public KubernetesChart(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public static KubernetesChart copy(KubernetesChart toCopy) {
        return new KubernetesChart(toCopy.getName(), toCopy.getVersion());
    }
}
