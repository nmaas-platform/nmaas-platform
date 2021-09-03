package net.geant.nmaas.portal.api.domain;

import lombok.*;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * DTO for {@link net.geant.nmaas.portal.persistent.entity.Application}
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationView {

    private Long id;

    @Pattern(regexp = "^[a-zA-Z0-9- ]+$")
    private String name;
    @NotNull
    @NotEmpty
    private String version;
    @Valid
    @NotNull
    private ConfigWizardTemplateView configWizardTemplate;
    private ConfigWizardTemplateView configUpdateWizardTemplate;
    @Valid
    @NotNull
    private AppDeploymentSpecView appDeploymentSpec;
    @Valid
    @NotNull
    private AppConfigurationSpecView appConfigurationSpec;

    private ApplicationState state;
}
