package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class HelmChartRepositoryEmbeddable {

    @Column(length = 14,  nullable = false, columnDefinition = "varchar(14) default 'nmaas'")
    @Pattern(regexp = "[A-Za-z-]{1,14}")
    private String name;

    @Column(length = 255, nullable = false, columnDefinition = "varchar(255) default 'https://artifactory.software.geant.org/artifactory/nmaas-helm'")
    @URL
    private String url;

    public static HelmChartRepositoryEmbeddable getDefault() {
        return new HelmChartRepositoryEmbeddable(
                "nmaas",
                "https://artifactory.software.geant.org/artifactory/nmaas-helm"
        );
    }
}
