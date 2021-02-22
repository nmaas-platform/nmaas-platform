package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
@Entity
@AllArgsConstructor
public class KubernetesChart implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String version;

    public KubernetesChart(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public static KubernetesChart copy(KubernetesChart toCopy) {
        return new KubernetesChart(toCopy.getName(), toCopy.getVersion());
    }
}
