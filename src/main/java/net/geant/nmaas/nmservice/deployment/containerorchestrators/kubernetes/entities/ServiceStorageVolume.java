package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm.HelmChartPersistenceVariable;
import net.geant.nmaas.orchestration.entities.AppStorageVolume;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EqualsAndHashCode
public class ServiceStorageVolume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ServiceStorageVolumeType type;

    @Column(nullable = false)
    private Integer size;

    @ElementCollection
    @Fetch(FetchMode.SELECT)
    private Map<HelmChartPersistenceVariable, String> deployParameters;

    public ServiceStorageVolume(ServiceStorageVolumeType type, Integer size, Map<HelmChartPersistenceVariable, String> deployParameters) {
        this.type = type;
        this.size = size;
        this.deployParameters = deployParameters;
    }

    public static ServiceStorageVolume fromAppStorageVolume(AppStorageVolume appStorageVolume) {
        ServiceStorageVolume serviceStorageVolume = new ServiceStorageVolume();
        serviceStorageVolume.setType(appStorageVolume.getType());
        serviceStorageVolume.setSize(appStorageVolume.getDefaultStorageSpace());
        serviceStorageVolume.deployParameters = new HashMap<>();
        if (appStorageVolume.getDeployParameters() != null) {
            appStorageVolume.getDeployParameters().forEach((key, value) -> {
                serviceStorageVolume.getDeployParameters().put(HelmChartPersistenceVariable.valueOf(key), value);
            });
        }
        return serviceStorageVolume;
    }

}
