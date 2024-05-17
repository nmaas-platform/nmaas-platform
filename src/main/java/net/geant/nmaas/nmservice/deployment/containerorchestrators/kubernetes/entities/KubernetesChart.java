package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Override
    public String toString() {
        return "KubernetesChart{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

}
