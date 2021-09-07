package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HelmChartRepositoryView {

    @Size(min=1, max=14)
    @Pattern(regexp = "[A-Za-z-]{1,14}")
    private String name;
    @Size(min=1, max=255)
    @URL
    private String url;
}
