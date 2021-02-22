package net.geant.nmaas.orchestration.entities;

import lombok.*;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethodType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class AppAccessMethod implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ServiceAccessMethodType type;

    private String name;

    private String tag;

    @ElementCollection
    @Fetch(FetchMode.SELECT)
    private Map<String, String> deployParameters;

    public AppAccessMethod(ServiceAccessMethodType type, String name, String tag, Map<String, String> deployParameters) {
        this.type = type;
        this.name = name;
        this.tag = tag;
        this.deployParameters = deployParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppAccessMethod that = (AppAccessMethod) o;
        // custom implementation of equals method - objects with ids equal to null, are assumed to be different, to be able to add multiple instances at once
        if(id == null && that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
