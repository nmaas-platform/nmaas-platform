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

    @Column(nullable = false)
    private Boolean main;

    @Column(nullable = false)
    private Integer size;

    @ElementCollection
    @Fetch(FetchMode.SELECT)
    private Map<HelmChartPersistenceVariable, String> deployParameters;

    public ServiceStorageVolume(Boolean main, Integer size, Map<HelmChartPersistenceVariable, String> deployParameters) {
        this.main = main;
        this.size = size;
        this.deployParameters = deployParameters;
    }

    public static ServiceStorageVolume fromAppStorageVolume(AppStorageVolume appStorageVolume) {
        ServiceStorageVolume serviceStorageVolume = new ServiceStorageVolume();
        serviceStorageVolume.setMain(appStorageVolume.getMain());
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
