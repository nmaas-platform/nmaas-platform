package net.geant.nmaas.portal.api.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppConfigurationSpecView {

    private Long id;

    @NotNull
    private List<ConfigFileTemplateView> templates = new ArrayList<>();

    private boolean configFileRepositoryRequired = false;

    private boolean configUpdateEnabled = false;

    /**
     * NMAAS-967
     * propagate information about terms acceptance in DTO
     */
    private boolean termsAcceptanceRequired = false;
}
