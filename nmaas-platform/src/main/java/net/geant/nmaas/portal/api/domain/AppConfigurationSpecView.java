package net.geant.nmaas.portal.api.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppConfigurationSpecView {

    private List<ConfigFileTemplateView> templates = new ArrayList<>();

    private boolean configFileRepositoryRequired = false;
}
