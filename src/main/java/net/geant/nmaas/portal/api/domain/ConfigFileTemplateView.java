package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConfigFileTemplateView {

    private Long id;

    private Long applicationId;

    private String configFileName;

    private String configFileDirectory;

    private String configFileTemplateContent;
}
