package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppAccessMethodView {

    private Long id;

    private ServiceAccessMethodType type;

    // Name of the access method displayed in the UI
    private String name;

    // Tag string for this access method to be used differently depending on the type
    private String tag;

    private Map<String, String> deployParameters = new HashMap<>();

}


