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

    @Column(length = 14, nullable = false, unique = true)
    @Pattern(regexp = "[A-Za-z-]{1,14}")
    private String name;

    @Column(nullable = false, unique = true)
    @URL
    private String url;
}
