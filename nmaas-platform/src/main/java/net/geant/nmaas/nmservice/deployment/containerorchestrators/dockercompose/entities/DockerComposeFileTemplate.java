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

    @Column(nullable = false)
    private Long applicationId;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private String composeFileTemplateContent;

    public DockerComposeFileTemplate() {
    }

    public DockerComposeFileTemplate(Long applicationId, String composeFileTemplateContent) {
        this.applicationId = applicationId;
        this.composeFileTemplateContent = composeFileTemplateContent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getComposeFileTemplateContent() {
        return composeFileTemplateContent;
    }

    public void setComposeFileTemplateContent(String composeFileTemplateContent) {
        this.composeFileTemplateContent = composeFileTemplateContent;
    }
}
