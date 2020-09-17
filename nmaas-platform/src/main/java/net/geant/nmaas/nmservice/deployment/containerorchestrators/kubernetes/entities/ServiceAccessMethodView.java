package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAccessMethodView {

    @Enumerated(EnumType.STRING)
    private ServiceAccessMethodType type;

    private String name;

    private String protocol;

    private String url;

    public static ServiceAccessMethodView fromServiceAccessMethod(ServiceAccessMethod accessMethod) {
        return new ServiceAccessMethodView(accessMethod.getType(), accessMethod.getName(), accessMethod.getProtocol(), accessMethod.getUrl());
    }

}
