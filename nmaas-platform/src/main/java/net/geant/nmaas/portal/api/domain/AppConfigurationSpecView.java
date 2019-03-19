package net.geant.nmaas.portal.api.domain;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppConfigurationSpecView {

    private List<ConfigFileTemplateView> templates;

    private boolean configFileRepositoryRequired;
}
