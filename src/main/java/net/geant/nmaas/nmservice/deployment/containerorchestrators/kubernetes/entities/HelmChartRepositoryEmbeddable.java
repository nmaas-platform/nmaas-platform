package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class HelmChartRepositoryEmbeddable {

    @Column(length = 14)
    @Pattern(regexp = "[A-Za-z-]{1,14}")
    private String name;

    @URL
    private String url;

    public HelmChartRepositoryEmbeddable(String name, String url) {
        this.setName(name);
        this.setUrl(url);
    }

    public void setName(String name) {
        this.name = name == null ? null : name.substring(0, Math.min(14, name.length()));
    }
}
