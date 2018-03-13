package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="docker_compose_file_template")
public class DockerComposeFileTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<DcnAttachedContainer> dcnAttachedContainers = new ArrayList<DcnAttachedContainer>();

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private String composeFileTemplateContent;

    public DockerComposeFileTemplate() { }

    public DockerComposeFileTemplate(String composeFileTemplateContent) {
        this.composeFileTemplateContent = composeFileTemplateContent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<DcnAttachedContainer> getDcnAttachedContainers() {
        return dcnAttachedContainers;
    }

    public void setDcnAttachedContainers(List<DcnAttachedContainer> dcnAttachedContainers) {
        this.dcnAttachedContainers = dcnAttachedContainers;
    }

    public String getComposeFileTemplateContent() {
        return composeFileTemplateContent;
    }

    public void setComposeFileTemplateContent(String composeFileTemplateContent) {
        this.composeFileTemplateContent = composeFileTemplateContent;
    }

    public static DockerComposeFileTemplate copy(DockerComposeFileTemplate toCopy) {
        return new DockerComposeFileTemplate(toCopy.getComposeFileTemplateContent());
    }

}
