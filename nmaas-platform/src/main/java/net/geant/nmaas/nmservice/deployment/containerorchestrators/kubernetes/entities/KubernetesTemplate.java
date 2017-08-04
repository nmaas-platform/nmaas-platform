package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
public class KubernetesTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The name of the helm chart archive to use
     */
    @Column(nullable=false)
    private String archive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArchive() {
        return archive;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }
}
