package net.geant.nmaas.nmservice.configuration.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NmServiceConfigurationTemplateView {

    private Long id;

    private Long applicationId;

    private String configFileName;

    private String configFileTemplateContent;
}
