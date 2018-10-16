package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
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

    public DockerComposeFileTemplate(String composeFileTemplateContent) {
        this.composeFileTemplateContent = composeFileTemplateContent;
    }

    public static DockerComposeFileTemplate copy(DockerComposeFileTemplate toCopy) {
        return new DockerComposeFileTemplate(toCopy.getComposeFileTemplateContent());
    }

}
