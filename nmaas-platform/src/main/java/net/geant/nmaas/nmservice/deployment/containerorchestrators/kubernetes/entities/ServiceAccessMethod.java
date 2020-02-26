package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Setter
    private String url;

    public boolean isOfType(ServiceAccessMethodType type) {
        return this.getType().equals(type);
    }

}
