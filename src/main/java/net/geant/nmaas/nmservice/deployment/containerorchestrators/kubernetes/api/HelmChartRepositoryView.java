package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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

    public HelmChartRepositoryView(String name, String url) {
        this.setName(name);
        this.setUrl(url);
    }

    public void setName(String name) {
        this.name = name == null ? null : name.substring(0, Math.min(14, name.length()));
    }
}
