package net.geant.nmaas.orchestration.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
@EqualsAndHashCode
public class AppAccessMethod {

    @Enumerated(EnumType.STRING)
    private ServiceAccessMethodType type;
    private String name;
    private String tag;

}
