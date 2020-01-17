package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * This class represents single access method to NMAAS service
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
@EqualsAndHashCode
public class ServiceAccessMethod {
    @Enumerated(EnumType.STRING)
    private ServiceAccessMethodType type;
    private String name;
    private String url;
}
