package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartIngressVariable;
import net.geant.nmaas.orchestration.entities.AppAccessMethod;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents single access method to NMAAS service
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@EqualsAndHashCode
public class ServiceAccessMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ServiceAccessMethodType type;

    private String name;

    private String url;

    private String protocol;

    @ElementCollection
    @Fetch(FetchMode.SELECT)
    private Map<HelmChartIngressVariable, String> deployParameters;

    public ServiceAccessMethod(ServiceAccessMethodType type, String name, String url, String protocol, Map<HelmChartIngressVariable, String> deployParameters) {
        this.type = type;
        this.name = name;
        this.url = url;
        this.protocol = protocol;
        this.deployParameters = deployParameters;
    }

    public boolean isOfType(ServiceAccessMethodType type) {
        return this.getType().equals(type);
    }

    public static ServiceAccessMethod fromAppAccessMethod(AppAccessMethod appAccessMethod) {
        ServiceAccessMethod serviceAccessMethod = new ServiceAccessMethod();
        serviceAccessMethod.setType(appAccessMethod.getType());
        serviceAccessMethod.setName(appAccessMethod.getTag());
        serviceAccessMethod.setProtocol(appAccessMethod.getName());
        serviceAccessMethod.setUrl(null);
        serviceAccessMethod.deployParameters = new HashMap<>();
        if (appAccessMethod.getDeployParameters() != null) {
            appAccessMethod.getDeployParameters().forEach((key, value) -> {
                serviceAccessMethod.getDeployParameters().put(HelmChartIngressVariable.valueOf(key), value);
            });
        }
        return serviceAccessMethod;
    }

}