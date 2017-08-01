package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_compose_file_template")
public class DockerComposeFileTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private String composeFileTemplateContent;

    public DockerComposeFileTemplate() {
    }

    public DockerComposeFileTemplate(String composeFileTemplateContent) {
        this.composeFileTemplateContent = composeFileTemplateContent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComposeFileTemplateContent() {
        return composeFileTemplateContent;
    }

    public void setComposeFileTemplateContent(String composeFileTemplateContent) {
        this.composeFileTemplateContent = composeFileTemplateContent;
    }
}
