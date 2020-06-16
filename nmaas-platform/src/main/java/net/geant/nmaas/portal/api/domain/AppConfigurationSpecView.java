package net.geant.nmaas.portal.api.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppConfigurationSpecView {

    private Long id;

    private List<ConfigFileTemplateView> templates = new ArrayList<>();

    private boolean configFileRepositoryRequired = false;

    private boolean configUpdateEnabled = false;
}
