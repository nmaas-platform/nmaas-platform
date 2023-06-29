package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppAccessMethodView {

    private Long id;

    private ServiceAccessMethodType type;

    // Name of the access method displayed in the UI
    private String name;

    // Tag string for this access method to be used differently depending on the type
    private String tag;

    @Builder.Default
    private AppAccessMethod.ConditionType conditionType = AppAccessMethod.ConditionType.NONE;

    private String condition;

    @Builder.Default
    private Map<String, String> deployParameters = new HashMap<>();

}


